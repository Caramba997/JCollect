package jcollect.detection;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * A class to search for imports in a CompilationUnit
 * @author Finn Carstensen
 */
public class ImportChecker {
	
	/**
	 * Searches for imports in a CompilationUnit
	 * @param cu The CompilationUnit to be checked
	 * @return A List of all imports found
	 */
	public static List<String> checkImports(CompilationUnit cu) {
		List<String> list = new ArrayList<>();
		ImportCollector ic = new ImportCollector();
		ic.visit(cu, list);
		return list;
	}
	
	/**
	 * A VoidVisitor to search for ImportDeclarations
	 * @author Finn Carstensen
	 */
	private static class ImportCollector extends VoidVisitorAdapter<List<String>> {
		@Override
		public void visit(ImportDeclaration id, List<String> collector) {
			super.visit(id, collector);
			collector.add(id.getNameAsString());
		}
	}
	
}
