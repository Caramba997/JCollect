package jcollect.directives;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import jcollect.predicates.MethodDeclPredicate;
import jcollect.predicates.MethodExprPredicate;
import jcollect.types.CallProperties;
import jcollect.types.Misuse;
import jcollect.util.TreeTraversal;

/**
 * Checks the given method call on a variable of the given types use a non negative index and gives warnings if the index may be out of bounds
 * @author Finn Carstensen
 */
public class IllegalIndex implements Directive {
	
	private final String NAME = "IllegalIndex";
	
	private String[] type;
	private String method;
	private int argPos;
	private int totalArgs;

	/**
	 * Creates an illegal index directive
	 * @param type The types (APIs) 
	 * @param method The API method
	 * @param argPos The position of the index argument, zero based
	 * @param totalArgs The total number of arguments in the API method, used to differentiate between overloaded methods
	 */
	public IllegalIndex(String[] type, String method, int argPos, int totalArgs) {
		this.type = type;
		this.method = method;
		this.argPos = argPos;
		this.totalArgs = totalArgs;
	}
	
	@Override
	public List<Misuse> checkDirective(CompilationUnit cu) {
		List<Misuse> misuses = new ArrayList<Misuse>();
		List<String> vars = TreeTraversal.findVariablesWithType(cu, type);
		List<MethodCallExpr> occurences = cu.findAll(MethodCallExpr.class, new MethodExprPredicate<MethodCallExpr>(vars, method));
		for (MethodCallExpr occ: occurences) {
			CallProperties callProps = new CallProperties(TreeTraversal.getMethodCallExprVar(occ).getNameAsString(), occ.getNameAsString(), occ.getBegin().get().line);
			if (occ.getArguments().size() == totalArgs) {
				Expression exp = occ.getArgument(argPos);
				traceParameterValue(cu, exp, callProps, misuses);
			}
		}
		return misuses;
	}
	
	private void traceParameterValue(CompilationUnit cu, Expression exp, CallProperties callProps, List<Misuse> misuses) {
		if (exp.isIntegerLiteralExpr()) {
			if (Integer.valueOf(((IntegerLiteralExpr) exp).getValue()) == 0) {
				if (!callProps.method.contains("add") && !hasEmptyCheck(exp, callProps.variable)) {
					misuses.add(new Misuse(NAME, callProps.line, "You call \"" + callProps.method + "\" on \"" + callProps.variable + "\" with the index \"0\". You should make sure this collection is not empty!", Misuse.IMPORTANCE_WARNING));
				}
			}
			else if (!hasIndexCheck(exp, callProps.variable)) {
				misuses.add(new Misuse(NAME, callProps.line, "The index \"" + exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" may be out of bounds. You should make sure this index is legal!", Misuse.IMPORTANCE_WARNING));
			}
		}
		else if (exp.isUnaryExpr()) {
			if (isNegativeIndex((UnaryExpr) exp)) {
				misuses.add(new Misuse(NAME, callProps.line, "The index \"" + exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" is negative and will cause an error!", Misuse.IMPORTANCE_MISUSE));
			}
		}
		else if (exp.isNameExpr()) {
			if (isIllegalIndex((NameExpr) exp)) {
				misuses.add(new Misuse(NAME, callProps.line, "The index \"" +  exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" is negative and will cause an error!", Misuse.IMPORTANCE_MISUSE));
			}
			else if (!hasIndexCheck(exp, callProps.variable)) {
				checkVariableValue(cu, (NameExpr) exp, callProps, misuses);
			}
		}
		else if (exp.isMethodCallExpr()) {
			checkMethodReturn(cu, (MethodCallExpr) exp, callProps, misuses);
		}
	}

	/**
	 * Checks if a method returns an illegal index.
	 * @param cu The CompilationUnit
	 * @param exp The MethodCallExpr
	 * @param callProps Properties of the initial method call
	 * @param misuses The list of misuses
	 */
	private void checkMethodReturn(CompilationUnit cu, MethodCallExpr exp, CallProperties callProps, List<Misuse> misuses) {
		if (!exp.getScope().isPresent()) {
			String methodName = exp.getNameAsString();
			Optional<MethodDeclaration> methodDeclOpt = cu.findFirst(MethodDeclaration.class, new MethodDeclPredicate<>(methodName));
			if (methodDeclOpt.isPresent()) {
				MethodDeclaration methodDecl = methodDeclOpt.get();
				if (isInt(methodDecl.getTypeAsString())) {
					List<ReturnStmt> returns = methodDecl.findAll(ReturnStmt.class);
					for (ReturnStmt returnStmt: returns) {
						Optional<Expression> exprOpt = returnStmt.getExpression();
						if (exprOpt.isPresent()) {
							Expression expr = exprOpt.get();
							if (expr.isIntegerLiteralExpr()) {
								if (Integer.valueOf(((IntegerLiteralExpr) expr).getValue()) == 0) {
									if (!callProps.method.contains("add") && !hasEmptyCheck(expr, callProps.variable)) {
										misuses.add(new Misuse(NAME, callProps.line, "You index in \"" + callProps.method + "\" on \"" + callProps.variable + "\" may be \"0\". You should make sure this collection is not empty!", Misuse.IMPORTANCE_WARNING));
									}
								}
								else if (!hasIndexCheck(expr, callProps.variable)) {
									misuses.add(new Misuse(NAME, callProps.line, "The index \"" + exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" may be out of bounds. You should make sure this index is legal!", Misuse.IMPORTANCE_WARNING));
								}
							}
							else if (expr.isUnaryExpr()) {
								if (isNegativeIndex((UnaryExpr) expr)) {
									misuses.add(new Misuse(NAME, callProps.line, "The index \"" + exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" may be negative and cause an error!", Misuse.IMPORTANCE_WARNING));
								}
							}
							else if (expr.isNameExpr()) {
								if (isIllegalIndex((NameExpr) expr)) {
									misuses.add(new Misuse(NAME, callProps.line, "The index \"" +  exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" may be negative and cause an error!", Misuse.IMPORTANCE_WARNING));
								}
								else if (!hasIndexCheck(expr, callProps.variable)) {
									checkVariableValue(cu, (NameExpr) expr, callProps, misuses);
								}
							}
							else if (expr.isMethodCallExpr()) {
								checkMethodReturn(cu, (MethodCallExpr) expr, callProps, misuses);
							}
						}
					}
				}
			}
		}
		else {
			misuses.add(new Misuse(NAME, callProps.line, "The index \"" + exp + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" may be out of bounds. You should make sure this index is legal!", Misuse.IMPORTANCE_WARNING));
		}
	}

	/**
	 * Checks if a UnaryExpr is a negative integer
	 * @param exp The UnaryExpr
	 * @return true, if exp is negative
	 */
	private boolean isNegativeIndex(UnaryExpr exp) {
		try {
			if (Integer.valueOf(exp.toString()) < 0) {
				return true;
			}
		}
		catch (NumberFormatException e) {
			return false;
		}
		return false;
	}

	/**
	 * Checks if the index variable is unchecked or assigned to a negative index or a value that may be illegal in the application context.
	 * Adds a new warning to the misuses if the value may be illegal.
	 * @param name NameExpr of the index variable
	 * @param varName Name of the variable on which the method is called
	 * @param misuses The list of misuses
	 * @param line The line of the method call
	 * @param method The method of the method call
	 * @return true, if the index is negative
	 */
	private void checkVariableValue(CompilationUnit cu, NameExpr name, CallProperties callProps, List<Misuse> misuses) {
		if (isInt(TreeTraversal.getDeclarationType(name))) {
			Expression assignment = TreeTraversal.findNearestAssignment(name);
			if (assignment != null) {
				if (assignment.isUnaryExpr()) {
					if (isNegativeIndex((UnaryExpr) assignment)) {
						misuses.add(new Misuse(NAME, callProps.line, "The index \"" + assignment + "\" in \"" + callProps.method + "\" on \"" + callProps.variable + "\" is negative and will cause an error!", Misuse.IMPORTANCE_MISUSE));
					}
				}
				else if (assignment.isNameExpr()) {
					checkVariableValue(cu, (NameExpr) assignment, callProps, misuses);
				}
				else if (assignment.isMethodCallExpr()) {
					checkMethodReturn(cu, (MethodCallExpr) assignment, callProps, misuses);
				}
			}
		}
	}
	
	/**
	 * Checks if the index variable is unchecked or assigned to a negative index or a value that may be illegal in the application context.
	 * Adds a new warning to the misuses if the value may be illegal.
	 * @param name NameExpr of the index variable
	 * @return true, if the index is negative
	 */
	private boolean isIllegalIndex(NameExpr name) {
		Expression assignment = TreeTraversal.findNearestAssignment(name);
		if (assignment != null) {
			if (assignment.isUnaryExpr()) {
				return isNegativeIndex((UnaryExpr) assignment);
			}
			else if (assignment.isNameExpr()) {
				return isIllegalIndex((NameExpr) assignment);
			}
		}
		return false;
	}
	
	/**
	 * Checks if the value of the given variable is checked in an if statement, while- or forloop
	 * @param name The name of the variable to be checked
	 * @param varName The name of the variable on which the method is called
	 * @return true, if it is checked
	 */
	private boolean hasIndexCheck(Expression name, String variable) {
		List<IfStmt> ifStmts = TreeTraversal.findParents(name, IfStmt.class);
		List<String> conditions = new LinkedList<>();
		for (IfStmt ifStmt: ifStmts) {
			conditions.add(ifStmt.getCondition().toString());
		}
		List<WhileStmt> whileStmts = TreeTraversal.findParents(name, WhileStmt.class);
		for (WhileStmt whileStmt: whileStmts) {
			conditions.add(whileStmt.getCondition().toString());
		}
		List<ForStmt> forStmts = TreeTraversal.findParents(name, ForStmt.class);
		for (ForStmt forStmt: forStmts) {
			conditions.add(forStmt.getCompare().get().toString());
		}
		for (String condition: conditions) {
			String unified = condition.replace(" ", "");
			if (unified.contains(variable + ".size()>" + name) || unified.contains(name + "<" + variable + ".size()>")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the value of the given variable is checked for emptyness in an if statement, while- or forloop
	 * @param name The name of the variable to be checked
	 * @param variable The name of the variable on which the method is called
	 * @return true, if it is checked
	 */
	private boolean hasEmptyCheck(Expression name, String variable) {
		List<IfStmt> ifStmts = TreeTraversal.findParents(name, IfStmt.class);
		List<String> conditions = new LinkedList<>();
		for (IfStmt ifStmt: ifStmts) {
			conditions.add(ifStmt.getCondition().toString());
		}
		List<WhileStmt> whileStmts = TreeTraversal.findParents(name, WhileStmt.class);
		for (WhileStmt whileStmt: whileStmts) {
			conditions.add(whileStmt.getCondition().toString());
		}
		List<ForStmt> forStmts = TreeTraversal.findParents(name, ForStmt.class);
		for (ForStmt forStmt: forStmts) {
			conditions.add(forStmt.getCompare().get().toString());
		}
		for (String condition: conditions) {
			String unified = condition.replace(" ", "");
			if (unified.contains(variable + ".size()>0") || unified.contains("0<" + variable + ".size()") || unified.contains("!" + variable + ".isEmpty()") || unified.contains(variable + ".isEmpty()==false")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the given type is int
	 * @param type Type to be checked
	 * @return true, if the given type is int
	 */
	private boolean isInt(String type) {
		if (type != null) {
			return type.equals("int") || type.equals("Integer");
		}
		return false;
	}

}