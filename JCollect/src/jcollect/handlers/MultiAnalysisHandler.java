package jcollect.handlers;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
public class MultiAnalysisHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = getSelectedResource();
		List<IFile> selectedFiles = getSelection(selection);
		if (!selectedFiles.isEmpty()) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("fc.view");
			} catch (PartInitException e) {
				ConsolePrinter.println("[ERROR] Could not open result view");
			}
			boolean first = true;
			IResource resource = Platform.getAdapterManager().getAdapter(selection.getFirstElement(), IResource.class);
			if (resource.exists() && resource instanceof IContainer) {
				ConsolePrinter.println("[EXECUTING] Analysing files contained in <" + ((IContainer) resource).getName() + ">");
			}
			else {
				ConsolePrinter.println("[EXECUTING] Analysing all selected files" );
			}
			for (IFile file: selectedFiles) {
				new DetectionMain(file, first);
				first = false;
			}
			ConsolePrinter.println("[ENDED]");
			ConsolePrinter.println("--------------------");
		}
		else {
			ConsolePrinter.println("[ERROR] No files selected. You need to select at least one Java file");
		}
		return null;
	}
	
	/**
	 * Determines the current user selection in the package explorer and returns an IFile if a file is selected
	 * @return The selected file
	 */
	private List<IFile> getSelection(IStructuredSelection selection) {
		if (selection != null) {
            Iterator<?> iterator = selection.iterator();
            List<IFile> files = new LinkedList<>();
            while (iterator.hasNext()) {
	            Object obj = iterator.next();
	            IResource file = (IResource) Platform.getAdapterManager().getAdapter(obj, IResource.class);
	            if (file == null) {
	                if (obj instanceof IAdaptable) {
	                    file = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
	                }
	            }
	            if (file != null) {
	            	if (file instanceof IFile) {
	            		IFile ifile = (IFile) file;
	            		if (ifile.getName().contains(".java")) {
	            			files.add(ifile);
	            		}
	            	}
	            	else if (file instanceof IContainer) {
	            		files.addAll(getFilesInContainer((IContainer) file));
	            	}
	            }
            }
            return files;
		}
	    return null;
	}
	
	private IStructuredSelection getSelectedResource() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	        ISelection selection = window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
	        if (selection instanceof IStructuredSelection) {
	            return (IStructuredSelection) selection;
	        }
	    }
	    return null;
	}

	private Collection<IFile> getFilesInContainer(IContainer container) {
		List<IFile> files = new LinkedList<>();
		try {
			IResource[] resources = container.members();
			for (IResource res: resources) {
				if (res instanceof IFile) {
					IFile ifile = (IFile) res;
            		if (ifile.getName().contains(".java")) {
            			files.add(ifile);
            		}
            	}
            	else if (res instanceof IContainer) {
            		files.addAll(getFilesInContainer((IContainer) res));
            	}
			}
		} catch (CoreException e) {
			ConsolePrinter.println("[ERROR] Could not determine folder/project members");
		}
		return files;
	}

}
