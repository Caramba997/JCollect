package dev;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.YamlPrinter;

public class ASTDebugger {
	
	public static void main(String[] args) {
		File file = new File("C:/Users/Finn/git/JCollect/JCollect/src/dev/examples/ASTTest.java");
		try {
			CompilationUnit cu = StaticJavaParser.parse(file);
			YamlPrinter printer = new YamlPrinter(true);
			System.out.println(printer.output(cu));
			/*DotPrinter printer = new DotPrinter(true);
			List<MethodCallExpr> methods = cu.findAll(MethodCallExpr.class);
			for (MethodCallExpr node: methods) {
				System.out.println(printer.output(node));
			}*/
		}
		catch (ParseProblemException e) {
			System.out.println("Error: ParseProblem occured");
		}
		catch (FileNotFoundException e) {
			System.out.println("Error: No such file found. You need to select a file");
		}
	}
	
}
