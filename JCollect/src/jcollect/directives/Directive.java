package jcollect.directives;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import jcollect.types.Misuse;

/**
 * The Interface for any directive that can be checked
 * @author Finn Carstensen
 */
public interface Directive {
	
	public List<Misuse> checkDirective(CompilationUnit cu);
	
}