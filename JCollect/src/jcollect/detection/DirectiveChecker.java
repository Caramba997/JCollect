package jcollect.detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import jcollect.directives.Directive;
import jcollect.directives.IllegalIndex;
import jcollect.directives.NullArgument;
import jcollect.directives.UnsupportedOperation;
import jcollect.types.Misuse;

/**
 * A class to check if a CompilationUnit satisfies a specified set of directives
 * @author Finn Carstensen
 */
public class DirectiveChecker {

	public static String[] LIST_APIS = {"java.util.List", "java.util.AbstractList", "java.util.AbstractSequentialList", "java.util.ArrayList", "java.util.CopyAndWriteArrayList", "java.util.LinkedList", "java.util.AttributeList", "java.util.RoleList", "java.util.RoleUnresolvedList", "java.util.Stack", "java.util.Vector"};
	public static String[] LIST_TYPES = {"List", "AbstractList", "AbstractSequentialList", "ArrayList", "CopyAndWriteArrayList", "LinkedList", "AttributeList", "RoleList", "RoleUnresolvedList", "Stack", "Vector"};
	public static String[] LIST_UNSUPPORTED_OPERATIONS = {"add", "remove", "addAll", "removeAll", "retainAll", "replaceAll", "sort", "clear", "set"};
	
	/**
	 * Checks if a CompilationUnit satisfies a specified set of directives. The "imports" are used to determine which directives need to be checked.
	 * @param cu The CompilationUnit
	 * @param imports APIs used in the CompilationUnit
	 * @return A list of misuses found in the CompilationUnit
	 */
	public static List<Misuse> checkDirectives(CompilationUnit cu, List<String> imports) {
		List<Directive> directives = new ArrayList<Directive>();
		List<String> listApis = Arrays.asList(LIST_APIS);
		if (listsHaveOneMatch(listApis, imports)) {
			directives.add(new IllegalIndex(LIST_TYPES, "addAll", 0, 2));
			directives.add(new IllegalIndex(LIST_TYPES, "get", 0, 1));
			directives.add(new IllegalIndex(LIST_TYPES, "set", 0, 2));
			directives.add(new IllegalIndex(LIST_TYPES, "add", 0, 2));
			directives.add(new IllegalIndex(LIST_TYPES, "remove", 0, 1));
			directives.add(new IllegalIndex(LIST_TYPES, "listIterator", 0, 1));
			directives.add(new NullArgument(LIST_TYPES, "toArray", 0, 1));
			directives.add(new NullArgument(LIST_TYPES, "containsAll", 0, 1));
			directives.add(new NullArgument(LIST_TYPES, "addAll", 0, 1));
			directives.add(new NullArgument(LIST_TYPES, "addAll", 1, 2));
			directives.add(new NullArgument(LIST_TYPES, "removeAll", 0, 1));
			directives.add(new NullArgument(LIST_TYPES, "retainAll", 0, 1));
			directives.add(new UnsupportedOperation(LIST_TYPES, LIST_UNSUPPORTED_OPERATIONS));
		}
		List<Misuse> result = new ArrayList<Misuse>();
		for (Directive dir: directives) {
			result.addAll(dir.checkDirective(cu));
		}
		return result;
	}
	
	/**
	 * Checks if there is at least one element that is present in both lists
	 * @param first First list
	 * @param second Second list
	 * @return true, if there is at least one element that is present in both lists
	 */
	public static boolean listsHaveOneMatch(List<String> first, List<String> second) {
		for (String s: first) {
			if (second.contains(s)) {
				return true;
			}
		}
		return false;
	}

}
