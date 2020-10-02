package jcollect.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.TryStmt;

/**
 * A class with some algorithms for tree walking
 * @author Finn Carstensen
 */
public class TreeTraversal {

	/**
	 * Finds the closest parent of the given node type
	 * @param <T> The node type class of the parent
	 * @param node The node
	 * @param parentClass The node type of the parent
	 * @return The parent if found, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Node> T findClosestParent(Node node, Class<T> parentClass) {
		Node current = node;
		do {
			Optional<Node> opt = current.getParentNode();
			if (opt.isPresent()) {
				current = opt.get();
			}
			else current = null;
		} while (current != null && !parentClass.isInstance(current));
		if (parentClass.isInstance(current)) {
			return (T) current;
		}
		return null;
	}
	
	/**
	 * Finds all parents with the given node type
	 * @param <T> The node type class of the parent
	 * @param node The node
	 * @param parentClass The node type of the parent
	 * @return List of parents, may be empty
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Node> List<T> findParents(Node node, Class<T> parentClass) {
		Node current = node;
		List<T> result = new ArrayList<>();
		do {
			Optional<Node> opt = current.getParentNode();
			if (opt.isPresent()) {
				current = opt.get();
				if (parentClass.isInstance(current)) {
					result.add((T) current);
				}
			}
			else current = null;
		} while (current != null);
		return result;
	}
	
	/**
	 * Finds all method call children of the given node that have the given variable and method
	 * @param parentNode The parent node
	 * @param variableName The variable on which the method is called
	 * @param method The method
	 * @return List of method calls, may be empty
	 */
	public static List<MethodCallExpr> findMethodCallChildren(Node parentNode, String variableName, String method) {
		List<MethodCallExpr> methodCalls = parentNode.findAll(MethodCallExpr.class);
		List<MethodCallExpr> result = new ArrayList<MethodCallExpr>();
		for (MethodCallExpr call: methodCalls) {
			if (call.getNameAsString().equals(method) && findClosestNameExpr(call, variableName) != null) {
				result.add(call);
			}
		}
		return result;
	}
	
	/**
	 * Finds the closes child with the given class using a breadth first search
	 * @param <T> The class extending Node
	 * @param node The parent node
	 * @param childClass The class of the child to be searched for
	 * @return The closest child of present, null otherwise
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Node> T findClosestChild(Node node, Class<T> childClass) {
		List<Node> queue = new ArrayList<Node>(node.getChildNodes());
		while (!queue.isEmpty()) {
			Node current = queue.get(0);
			queue.remove(0);
			if (childClass.isInstance(current)) {
				return (T) current;
			}
			else {
				queue.addAll(current.getChildNodes());
			}
		}
		return null;
	}
	
	/**
	 * Finds the closest name expression
	 * @param node The parent node
	 * @param name The name of the closest name expression
	 * @return The closest name expression if present, null otherwise
	 */
	public static Node findClosestNameExpr(Node node, String name) {
		List<Node> queue = new ArrayList<Node>(node.getChildNodes());
		while (!queue.isEmpty()) {
			Node current = queue.get(0);
			queue.remove(0);
			if (current instanceof NameExpr && ((NameExpr) current).getNameAsString().equals(name)) {
				return current;
			}
			else {
				queue.addAll(current.getChildNodes());
			}
		}
		return null;
	}
	
	/**
	 * Finds the variable on which a method call is made
	 * @param call The method call expression
	 * @return The variable if present, null otherwise
	 */
	public static NameExpr getMethodCallExprVar(MethodCallExpr call) {
		List<Node> children = call.getChildNodes();
		for (Node child: children) {
			if (child instanceof NameExpr) {
				return (NameExpr) child;
			}
		}
		return null;
	}

	/**
	 * Finds all variables with one of the given types in the given compilation unit
	 * @param cu The compilation unit
	 * @param types The types
	 * @return List of variables, may be empty
	 */
	public static List<String> findVariablesWithType(CompilationUnit cu, String[] types) {
		List<VariableDeclarationExpr> variableDeclarations = cu.findAll(VariableDeclarationExpr.class);
		List<String> result = new ArrayList<String>();
		for (VariableDeclarationExpr declaration: variableDeclarations) {
			NodeList<VariableDeclarator> vars = declaration.getVariables();
			for (VariableDeclarator var: vars) {
				for (String t: types) {
					if (var.getTypeAsString().contains(t)) {
						result.add(var.getNameAsString());
						break;
					}
				}
			}
		}
		List<FieldDeclaration> fieldDeclarations = cu.findAll(FieldDeclaration.class);
		for (FieldDeclaration declaration: fieldDeclarations) {
			NodeList<VariableDeclarator> vars = declaration.getVariables();
			for (VariableDeclarator var: vars) {
				for (String t: types) {
					if (var.getTypeAsString().contains(t)) {
						result.add(var.getNameAsString());
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Searches for the nearest assignment of the given variable by walking the whole code above the given node
	 * @param name The variable name
	 * @return The nearest assignment if present, null otherwise
	 */
	public static Expression findNearestAssignment(NameExpr name) {
		Optional<Node> parentOpt = name.getParentNode();
		Node oldChild = name;
		boolean topOfTreeOrFound = false;
		do {
			if (parentOpt.isPresent()) {
				Node parent = parentOpt.get();
				List<Node> queue = getAllNodesBefore(parent, oldChild);
				while (queue.size() > 0) {
					Node current = queue.get(0);
					queue.remove(0);
					if (current instanceof ExpressionStmt) {
						AssignExpr assignment = findClosestChild(current, AssignExpr.class);
						if (assignment != null && assignment.getTarget().toString().equals(name.getNameAsString())) {
							return assignment.getValue();
						}
						else {
							VariableDeclarationExpr declaration = findClosestChild(current, VariableDeclarationExpr.class);
							if (declaration != null) {
								for (VariableDeclarator dec: declaration.getVariables()) {
									if (dec.getNameAsString().equals(name.getNameAsString())) {
										Optional<Expression> opt = dec.getInitializer();
										if (opt.isPresent()) {
											return opt.get();
										}
									}
								}
							}
						}
					}
				}
				oldChild = parent;
				parentOpt = parent.getParentNode();
			}
			else {
				topOfTreeOrFound = true;
			}
		} while (!topOfTreeOrFound);
		return null;
	}
	
	/**
	 * Gets all children (transitive) of the given node but stops if the stopNode is found
	 * @param node The parent node
	 * @param stopNode The node where the search shall be stopped
	 * @return List of children, may be empty
	 */
	public static List<Node> getAllNodesBefore(Node node, Node stopNode) {
		List<Node> queue = new LinkedList<>();
		queue.add(0, node);
		List<Node> children = node.getChildNodes();
		for (Node child: children) {
			if (!child.equals(stopNode)) {
				queue.add(0, child);
				List<Node> newchilds = child.getChildNodes();
				if (newchilds.size() > 0) {
					for (Node newchild: newchilds) {
						queue.addAll(0, getAllNodesBefore(newchild, stopNode));
					}
				}
			}
			else break;
		}
		return queue;
	}
	
	/**
	 * Finds the declaration type of a variable
	 * @param name The variable
	 * @return The type of the variable if found, null otherwise
	 */
	public static String getDeclarationType(NameExpr name) {
		Optional<Node> parentOpt = name.getParentNode();
		boolean topOfTreeOrFound = false;
		do {
			List<Node> queue = new LinkedList<>();
			if (parentOpt.isPresent()) {
				Node parent = parentOpt.get();
				queue.add(0, parent);
				List<Node> children = parent.getChildNodes();
				for (Node child: children) {
					if (!child.equals((Node) name)) {
						queue.add(0, child);
					}
					else break;
				}
				while (queue.size() > 0) {
					Node current = queue.get(0);
					queue.remove(0);
					if (current instanceof ExpressionStmt) {
						VariableDeclarationExpr declaration = findClosestChild(current, VariableDeclarationExpr.class);
						if (declaration != null) {
							for (VariableDeclarator dec: declaration.getVariables()) {
								if (dec.getNameAsString().equals(name.getNameAsString())) {
									return dec.getTypeAsString();
								}
							}
						}
					}
				}
				parentOpt = parent.getParentNode();
			}
			else {
				topOfTreeOrFound = true;
			}
		} while (!topOfTreeOrFound);
		return null;
	}
	
	/**
	 * Checks for existance of exception handling
	 * @param expr The Expression
	 * @return true, if the expression is surrounded by a try-catch-block
	 */
	public static boolean hasTryCatch(Expression expr, String exceptionType) {
		Node node = TreeTraversal.findClosestParent(expr, TryStmt.class);
		if (node != null) {
			TryStmt tryStmt = (TryStmt) node;
			NodeList<CatchClause> clauses = tryStmt.getCatchClauses();
			for (CatchClause clause: clauses) {
				String exception = clause.getParameter().getType().toString();
				if (exception.equals(exceptionType) || exception.equals("RuntimeException") || exception.equals("Exception")) {
					return true;
				}
			}
		}
		return false;
	}
	
}
