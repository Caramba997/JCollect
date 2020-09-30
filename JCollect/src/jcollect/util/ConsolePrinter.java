package jcollect.util;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * A class to handle outputs to the custom JCollect console
 * @author Finn Carstensen
 */
public class ConsolePrinter {

	private static MessageConsoleStream out = null;
	
	/**
	 * Initializes the output stream to the console
	 */
	public static void setConsoleStream() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		MessageConsole console = null;
		IConsole[] consoles = plugin.getConsoleManager().getConsoles();
		for (IConsole iConsole : consoles) {
		    if (iConsole instanceof MessageConsole && iConsole.getName().equals("JCollectConsole")) {
		        console = (MessageConsole) iConsole;
		        break;
		    }
		}
		if (console != null) {
			out = console.newMessageStream();
		}
	}
	
	/**
	 * Prints a line to the console if the console is initialized
	 * @param s The String to be printed
	 */
	public static void println(String s) {
		if (out != null) {
			out.println(s);
		}
	}
	
	/**
	 * Prints an empty line to the console if the console is initialized
	 */
	public static void println() {
		if (out != null) {
			out.println();
		}
	}
	
	/**
	 * Prints a String to the console if the console is initialized
	 * @param s The String to be printed
	 */
	public static void print(String s) {
		if (out != null) {
			out.print(s);
		}
	}
	
}
