package jcollect.directives;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

import jcollect.predicates.MethodsPredicate;
import jcollect.types.Misuse;
import jcollect.util.TreeTraversal;

/**
 * Checks if a modifying operation is called on an unmodifiable collection of the given types
 * @author Finn Carstensen
 */
public class UnsupportedOperation implements Directive {

	private final String NAME = "UnsupportedOperation";
	private final String IMPORTANCE = Misuse.IMPORTANCE_MISUSE;
	
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
			int line = occ.getBegin().get().line;
			String callVariable = TreeTraversal.getMethodCallExprVar(occ).getNameAsString();
			String callMethod = occ.getNameAsString();
			Expression assignment = TreeTraversal.findNearestAssignment(TreeTraversal.getMethodCallExprVar(occ));
			if (assignedToUnmodifiable(assignment)) {
				misuses.add(new Misuse(NAME, line, "The method \"" + callMethod + "\" on the unmodifiable collection \"" + callVariable + "\" is not supported and will cause an error!", IMPORTANCE));
			}
		}
		return misuses;
	}
	
	/**
	 * Checks if a variable is assigned to an unmodifiable collection
	 * @param assignment
	 * @return
	 */
	private boolean assignedToUnmodifiable(Expression assignment) {
		if (assignment != null) {
			if (assignment.isNameExpr()) {
				return assignedToUnmodifiable(TreeTraversal.findNearestAssignment((NameExpr) assignment));
			}
			else if (assignment.isMethodCallExpr()) {
				MethodCallExpr call = (MethodCallExpr) assignment;
				if (call.getNameAsString().contains("unmodifiable")) {
					return true;
				}
			}
		}
		return false;
	}

}
