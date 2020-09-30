package jcollect.handlers;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import jcollect.util.ConsolePrinter;

/**
 * Creates a new console for the Eclipse Console view
 * @author Finn Carstensen
 */
public class ConsoleFactory implements IConsoleFactory {

	public static final MessageConsole jcollectconsole = new MessageConsole("JCollectConsole", null);
	
	@Override
	public void openConsole() {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	    consoleManager.addConsoles( new IConsole[] { jcollectconsole } );
	    consoleManager.showConsoleView( jcollectconsole );
		ConsolePrinter.setConsoleStream();
	}

}
