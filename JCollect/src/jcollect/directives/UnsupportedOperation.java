package jcollect.directives;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;

import jcollect.predicates.MethodDeclPredicate;
import jcollect.predicates.MethodsPredicate;
import jcollect.types.CallProperties;
import jcollect.types.Misuse;
import jcollect.util.TreeTraversal;

/**
 * Checks if a modifying operation is called on an unmodifiable collection of the given types
 * @author Finn Carstensen
 */
public class UnsupportedOperation implements Directive {

	private final String NAME = "UnsupportedOperation";
	
	private String[] type;
	private String[] methods;
	
	/**
	 * Create a new unsupported operation directive
	 * @param types The APIs that allow modifiable collections but have modifying methods
	 * @param methods The modifying API methods
	 */
	public UnsupportedOperation(String[] types, String[] methods) {
		this.type = types;
		this.methods = methods;
	}
	
	@Override
	public List<Misuse> checkDirective(CompilationUnit cu) {
		List<Misuse> misuses = new ArrayList<Misuse>();
		List<String> vars = TreeTraversal.findVariablesWithType(cu, type);
		List<MethodCallExpr> occurences = cu.findAll(MethodCallExpr.class, new MethodsPredicate<MethodCallExpr>(vars, methods));
		for (MethodCallExpr occ: occurences) {
			if (!TreeTraversal.hasTryCatch(occ, "UnsupportedOperationException")) {
				Optional<Position> opt = occ.getBegin();
				int line = 1;
				if (opt.isPresent()) {
					line = opt.get().line;
				}
				String callVariable = TreeTraversal.getMethodCallExprVar(occ).getNameAsString();
				String callMethod = occ.getNameAsString();
				Expression assignment = TreeTraversal.findNearestAssignment(TreeTraversal.getMethodCallExprVar(occ));
				isUnmodifiable(assignment, cu, new CallProperties(callVariable, callMethod, line), misuses);
			}
		}
		return misuses;
	}
	
	/**
	 * Checks if a variable is assigned to an unmodifiable collection
	 * @param assignment
	 * @return
	 */
	private void isUnmodifiable(Expression assignment, CompilationUnit cu, CallProperties props, List<Misuse> misuses) {
		if (assignment != null) {
			if (assignment.isNameExpr()) {
				isUnmodifiable(TreeTraversal.findNearestAssignment((NameExpr) assignment), cu, props, misuses);
			}
			else if (assignment.isMethodCallExpr()) {
				MethodCallExpr call = (MethodCallExpr) assignment;
				if (call.getNameAsString().contains("unmodifiable")) {
					misuses.add(new Misuse(NAME, props.line, "The method \"" + props.method + "\" on the unmodifiable collection \"" + props.variable + "\" is not supported and will cause an error!", Misuse.IMPORTANCE_MISUSE));
				}
				else if (methodReturnsUnmodifiable(cu, call)) {
					misuses.add(new Misuse(NAME, props.line, "The method \"" + props.method + "\" on a potentially unmodifiable collection \"" + props.variable + "\" may be not supported and cause an error!", Misuse.IMPORTANCE_WARNING));
				}
			}
		}
	}

	/**
	 * Checks if a method possibly returns an unmodifiable collection
	 * @param cu The CompilationUnit
	 * @param exp The MethodCallExpression
	 * @return true, if the method may return an unmodifiable collection
	 */
	private boolean methodReturnsUnmodifiable(CompilationUnit cu, MethodCallExpr exp) {
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
						if (expr.isNameExpr()) {
							if (isNameVarUnmodifiable(cu, (NameExpr) expr)) {
								return true;
							}
						}
						else if (expr.isMethodCallExpr()) {
							MethodCallExpr call = (MethodCallExpr) expr;
							if (call.getNameAsString().contains("unmodifiable")) {
								return true;
							}
							else {
								return methodReturnsUnmodifiable(cu, (MethodCallExpr) expr);
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a variable holds or possibly holds an unmodifiable collection
	 * @param cu The CompilationUnit
	 * @param expr The variable name
	 * @return true, if the variable may hold an unmodifiable collection
	 */
	private boolean isNameVarUnmodifiable(CompilationUnit cu, NameExpr expr) {
		Expression assignment = TreeTraversal.findNearestAssignment(expr);
		if (assignment != null) {
			if (assignment.isNameExpr()) {
				return isNameVarUnmodifiable(cu, (NameExpr) assignment);
			}
			else if (assignment.isMethodCallExpr()) {
				MethodCallExpr call = (MethodCallExpr) assignment;
				if (call.getNameAsString().contains("unmodifiable")) {
					return true;
				}
				else {
					return methodReturnsUnmodifiable(cu, call);
				}
			}
		}
		return false;
	}

}
