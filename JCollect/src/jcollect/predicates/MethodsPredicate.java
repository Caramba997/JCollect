package jcollect.predicates;

import java.util.List;
import java.util.function.Predicate;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import jcollect.util.TreeTraversal;

/**
 * A predicate to filter method call expressions be one of the given methods on one of the given variables
 * @author Finn Carstensen
 * @param <E> MethodCallExpr
 */
public class MethodsPredicate<T> implements Predicate<MethodCallExpr> {
	
	private List<String> vars;
	private String[] methods;
	
	/**
	 * Creates a new MethodExprPredicate
	 * @param vars A list of variables for which the presence of method calls shall be checked
	 * @param method The method to be present
	 */
	public MethodsPredicate(List<String> vars, String[] methods) {
		this.vars = vars;
		this.methods = methods;
	}
	
	@Override
	public boolean test(MethodCallExpr call) {
		NameExpr varName = TreeTraversal.getMethodCallExprVar(call);
		if (varName != null && vars.contains(varName.getNameAsString())) {
			for (String method: methods) {
				if (method.equals(call.getNameAsString())) {
					return true;
				}
			}
		}
		return false;
	}
	
}
