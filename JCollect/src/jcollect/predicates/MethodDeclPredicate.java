package jcollect.predicates;

import java.util.function.Predicate;

import com.github.javaparser.ast.body.MethodDeclaration;

public class MethodDeclPredicate<E> implements Predicate<MethodDeclaration> {

	private String method;
	
	public MethodDeclPredicate(String method) {
		this.method = method;
	}
	
	@Override
	public boolean test(MethodDeclaration decl) {
		if (decl.getNameAsString().equals(method)) {
			return true;
		}
		return false;
	}

}
