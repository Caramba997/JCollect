package dev;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import jcollect.detection.DirectiveChecker;
import jcollect.detection.ImportChecker;
import jcollect.directives.Directive;
import jcollect.directives.IllegalIndex;
import jcollect.directives.NullArgument;
import jcollect.directives.UnsupportedOperation;
import jcollect.types.Misuse;

/**
 * This class is just used for debugging the misuse finding.
 * @author Finn Carstensen
 */
public class Debugger {

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		File file = new File("C:/Users/Finn/Desktop/Uni/Eclipse/Plugin/JCollect/src/dev/ClassWithMisuses.java");
		try {
			System.out.println("Starting to parse file: " + file.getAbsolutePath());
			CompilationUnit cu = StaticJavaParser.parse(file);
			System.out.println("[FINISHED] Parsing successful");
			/*YamlPrinter printer = new YamlPrinter(true);
			System.out.println(printer.output(cu));*/
			List<String> imports = ImportChecker.checkImports(cu);
			System.out.print("Starting to check the API usage of the imported Collections:");
			for (String s: imports) {
				System.out.print(" <" + s + ">");
			}
			System.out.println();
			List<Misuse> misuses = checkDirectives(cu, imports);
			long endTime = System.nanoTime();
			long timeTaken = (endTime - startTime) / 1000000;
			if (misuses.size() > 0) {
				System.out.println("[FINISHED] Time: " + timeTaken + "ms, " + misuses.size() + " Misuse(s) found:");
			}
			else {
				System.out.println("[FINISHED] Time: " + timeTaken + "ms, No misuses found");
			}
			for (Misuse m: misuses) {
				System.out.println("  " + m);
			}
		}
		catch (ParseProblemException e) {
			System.out.println("Error: ParseProblem occured");
		}
		catch (FileNotFoundException e) {
			System.out.println("Error: No such file found. You need to select a file");
		}
	}
	
	private static List<Misuse> checkDirectives(CompilationUnit cu, List<String> imports) {
		List<Directive> directives = new ArrayList<Directive>();
		List<String> listApis = Arrays.asList(DirectiveChecker.LIST_APIS);
		if (DirectiveChecker.listsHaveOneMatch(listApis, imports)) {
			directives.add(new IllegalIndex(DirectiveChecker.LIST_TYPES, "addAll", 0, 2));
			directives.add(new IllegalIndex(DirectiveChecker.LIST_TYPES, "get", 0, 1));
			directives.add(new IllegalIndex(DirectiveChecker.LIST_TYPES, "set", 0, 2));
			directives.add(new IllegalIndex(DirectiveChecker.LIST_TYPES, "add", 0, 2));
			directives.add(new IllegalIndex(DirectiveChecker.LIST_TYPES, "remove", 0, 1));
			directives.add(new IllegalIndex(DirectiveChecker.LIST_TYPES, "listIterator", 0, 1));
			directives.add(new NullArgument(DirectiveChecker.LIST_TYPES, "toArray", 0, 1));
			directives.add(new NullArgument(DirectiveChecker.LIST_TYPES, "containsAll", 0, 1));
			directives.add(new NullArgument(DirectiveChecker.LIST_TYPES, "addAll", 0, 1));
			directives.add(new NullArgument(DirectiveChecker.LIST_TYPES, "addAll", 1, 2));
			directives.add(new NullArgument(DirectiveChecker.LIST_TYPES, "removeAll", 0, 1));
			directives.add(new NullArgument(DirectiveChecker.LIST_TYPES, "retainAll", 0, 1));
			directives.add(new UnsupportedOperation(DirectiveChecker.LIST_TYPES, DirectiveChecker.LIST_UNSUPPORTED_OPERATIONS));
		}
		List<Misuse> result = new ArrayList<Misuse>();
		for (Directive dir: directives) {
			result.addAll(dir.checkDirective(cu));
		}
		return result;
	}

}
