package jcollect.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import jcollect.detection.DetectionMain;
import jcollect.util.ConsolePrinter;

/**
 * The handler class for a JCollect command executed from the package explorer in the Eclipse IDE
 * @author Finn Carstensen
 */
public class AnalysisHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IFile selectedFile = getSelection();
		if (selectedFile != null) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("fc.view");
			} catch (PartInitException e) {
				ConsolePrinter.println("[Error] Could not open result view");
			}
			new DetectionMain(selectedFile);
		}
		else {
			ConsolePrinter.println("[ERROR] No file selected. You need to select a Java file");
		}
		return null;
	}
	
	/**
	 * Determines the current user selection in the package explorer and returns an IFile if a file is selected
	 * @return The selected file
	 */
	private IFile getSelection() {
	    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	        ISelection selection = window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
	        if (selection instanceof IStructuredSelection) {
	            IStructuredSelection sSelection = (IStructuredSelection) selection;
	            Object obj = sSelection.getFirstElement();
	            IResource file = (IResource) Platform.getAdapterManager().getAdapter(obj, IResource.class);
	            if (file == null) {
	                if (obj instanceof IAdaptable) {
	                    file = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
	                }
	            }
	            if (file != null) {
	            	if (file instanceof IFile) {
	            		return (IFile) file;
	            	}
	            }
	        }
	    }
	    return null;
	}
	
	/*private void setUpConsole() {
	    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    String id = IConsoleConstants.ID_CONSOLE_VIEW;
	    try {
	    	IConsoleView view = (IConsoleView) page.showView(id);
	    	view.display(ConsoleFactory.jcollectconsole);
	    }
	    catch (PartInitException e) {
	    	e.printStackTrace();
	    }
	}*/

}
