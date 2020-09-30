package jcollect.predicates;

import java.util.List;
import java.util.function.Predicate;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import jcollect.util.TreeTraversal;

/**
 * A predicate to filter method call expressions be the given method on one of the given variables
 * @author Finn Carstensen
 * @param <E> MethodCallExpr
 */
public class MethodExprPredicate<E> implements Predicate<MethodCallExpr> {

	private List<String> vars;
	private String method;
	
	/**
	 * Creates a new MethodExprPredicate
	 * @param vars A list of variables for which the presence of method calls shall be checked
	 * @param method The method to be present
	 */
	public MethodExprPredicate(List<String> vars, String method) {
		this.vars = vars;
		this.method = method;
	}
	
	@Override
	public boolean test(MethodCallExpr call) {
		NameExpr varName = TreeTraversal.getMethodCallExprVar(call);
		if (varName != null && vars.contains(varName.getNameAsString())) {
			return method.equals(call.getNameAsString());
		}
		return false;
	}
	
}
