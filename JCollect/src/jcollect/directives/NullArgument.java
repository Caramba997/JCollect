package jcollect.directives;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import jcollect.predicates.MethodDeclPredicate;
import jcollect.predicates.MethodExprPredicate;
import jcollect.types.Misuse;
import jcollect.util.TreeTraversal;

/**
 * Checks if the given method call on a variable of the given types uses a null value in an argument and gives warnings if the value might be null
 * @author Finn Carstensen
 */
public class NullArgument implements Directive {

	private final String NAME = "NullArgument";
	
	private String[] type;
	private String method;
	private int argPos;
	private int totalArgs;
	
	/**
	 * Creates a null argument directive
	 * @param type The types (APIs) 
	 * @param method The API method
	 * @param argPos The position of the index argument, zero based
	 * @param totalArgs The total number of arguments in the API method, used to differentiate between overloaded methods
	 */
	public NullArgument(String[] type, String method, int argPos, int totalArgs) {
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
			boolean isMisuse = false;
			String callVariable = TreeTraversal.getMethodCallExprVar(occ).getNameAsString();
			String callMethod = occ.getNameAsString();
			if (occ.getArguments().size() == totalArgs) {
				Expression exp = occ.getArgument(argPos);
				int line = occ.getBegin().get().line;
				if (exp.isNullLiteralExpr()) {
					isMisuse = true;
				}
				else if (exp.isNameExpr()) {
					isMisuse = isNameVarNull(cu, (NameExpr) exp, misuses, line, callVariable, callMethod);
				}
				else if (exp.isMethodCallExpr()) {
					if (methodReturnsNull(cu, (MethodCallExpr) exp)) {
						misuses.add(new Misuse(NAME, line, "An argument in \"" + callMethod + "\" on \"" + callVariable + "\" may be null. You should make sure it is not!", Misuse.IMPORTANCE_WARNING));
					}
				}
				if (isMisuse) {
					misuses.add(new Misuse(NAME, line, "An argument in \"" + callMethod + "\" on \"" + callVariable + "\" is null and will cause an error!", Misuse.IMPORTANCE_MISUSE));
				}
			}
		}
		return misuses;
	}

	/**
	 * Checks if the result of a method call may be null
	 * @param cu The CompilationUnit
	 * @param exp The MethodCallExpr
	 * @return True, if the method is defined in cu and has a "return null" statement or if the method is not defined in cu.
	 */
	private boolean methodReturnsNull(CompilationUnit cu, MethodCallExpr exp) {
		if (!exp.getScope().isPresent()) {
			String methodName = exp.getNameAsString();
			Optional<MethodDeclaration> methodDeclOpt = cu.findFirst(MethodDeclaration.class, new MethodDeclPredicate<>(methodName));
			if (methodDeclOpt.isPresent()) {
				MethodDeclaration methodDecl = methodDeclOpt.get();
				List<ReturnStmt> returns = methodDecl.findAll(ReturnStmt.class);
				for (ReturnStmt returnStmt: returns) {
					Optional<Expression> exprOpt = returnStmt.getExpression();
					if (exprOpt.isPresent()) {
						Expression expr = exprOpt.get();
						if (expr.isNullLiteralExpr()) {
							return true;
						}
						else if (expr.isNameExpr()) {
							if (isNameVarNull(cu, (NameExpr) expr)) {
								return true;
							}
						}
						else if (expr.isMethodCallExpr()) {
							return methodReturnsNull(cu, (MethodCallExpr) expr);
						}
					}
				}
			}
		}
		else {
			return true;
		}
		return false;
	}

	/**
	 * Checks if a variable is assigned to null or to anything that might be null and is unchecked. Adds a warning to the misuses if it might be null.
	 * @param name The variable to be checked
	 * @param misuses The list of misuses
	 * @param line The line of the method call
	 * @param variable The variable of the method call
	 * @param method The method of the method call
	 * @return true, if the variable is assigned to null
	 */
	private boolean isNameVarNull(CompilationUnit cu, NameExpr name, List<Misuse> misuses, int line, String variable, String method) {
		if (!hasNameVarNullCheck(name)) {
			Expression assignment = TreeTraversal.findNearestAssignment(name);
			if (assignment != null) {
				if (assignment.isNullLiteralExpr()) {
					return true;
				}
				else if (assignment.isNameExpr()) {
					return isNameVarNull(cu, (NameExpr) assignment, misuses, line, variable, method);
				}
				else if (!isPrimitiveType(TreeTraversal.getDeclarationType(name)) && assignment.isMethodCallExpr()) {
					if (methodReturnsNull(cu, (MethodCallExpr) assignment)) {
						misuses.add(new Misuse(NAME, line, "An argument in \"" + method + "\" on \"" + variable + "\" may be null. You should make sure it is not!", Misuse.IMPORTANCE_WARNING));
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if a variable is assigned to null or to anything that might be null and is unchecked. Adds a warning to the misuses if it might be null.
	 * @param cu The CompilationUnit
	 * @param name The variable to be checked
	 * @return true, if the variable is assigned to null
	 */
	private boolean isNameVarNull(CompilationUnit cu, NameExpr name) {
		Expression assignment = TreeTraversal.findNearestAssignment(name);
		if (assignment != null) {
			if (assignment.isNullLiteralExpr()) {
				return true;
			}
			else if (assignment.isNameExpr()) {
				return isNameVarNull(cu, (NameExpr) assignment);
			}
			else if (!isPrimitiveType(TreeTraversal.getDeclarationType(name)) && assignment.isMethodCallExpr()) {
				return methodReturnsNull(cu, (MethodCallExpr) assignment);
			}
		}
		return false;
	}
	
	/**
	 * Checks if a variable is checked not to be null in an if statement, while- or forloop
	 * @param name The variable to be checked
	 * @return true, if the variable is checked
	 */
	private boolean hasNameVarNullCheck(NameExpr name) {
		List<IfStmt> ifStmts = TreeTraversal.findParents(name, IfStmt.class);
		for (IfStmt ifStmt: ifStmts) {
			String condition = ifStmt.getCondition().toString();
			if (condition.contains(name.getNameAsString() + " != null")) {
				return true;
			}
		}
		List<WhileStmt> whileStmts = TreeTraversal.findParents(name, WhileStmt.class);
		for (WhileStmt whileStmt: whileStmts) {
			String condition = whileStmt.getCondition().toString();
			if (condition.contains(name.getNameAsString() + " != null")) {
				return true;
			}
		}
		List<ForStmt> forStmts = TreeTraversal.findParents(name, ForStmt.class);
		for (ForStmt forStmt: forStmts) {
			String condition = forStmt.getCompare().get().toString();
			if (condition.contains(name.getNameAsString() + " != null")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given type is a primitive type
	 * @param type Type to be checked
	 * @return true, if the type is primitive
	 */
	private boolean isPrimitiveType(String type) {
		return type.equals("int") || type.equals("boolean") || type.equals("char") ||type.equals("float") || type.equals("long") || type.equals("byte") || type.equals("short") || type.equals("double");
	}

}
