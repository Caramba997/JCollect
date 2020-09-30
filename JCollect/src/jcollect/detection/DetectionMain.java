package jcollect.detection;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import jcollect.types.Misuse;
import jcollect.util.ConsolePrinter;
import jcollect.views.JCollectView;

/**
 * Main routine for the process of finding misuses in a file
 * 
 * @author Finn Carstensen
 */
public class DetectionMain {

	/**
	 * Create the process to find misuses in the given file. At first, the imports of a class are checked to determine which APIs are used.
	 * The found APIs are passed to the next step.
	 * @param ifile An IFile which will be checked for misuses
	 */
	public DetectionMain(IFile ifile, boolean first) {
		long startTime = System.nanoTime();
		File file = Platform.getLocation().append(ifile.getFullPath()).toFile();
		ConsolePrinter.println("Starting to parse file <" + file.getAbsolutePath() + "> ...");
		try {
			CompilationUnit cu = StaticJavaParser.parse(file);
			ConsolePrinter.println("[SUCCESS] Parsing successful");
			List<String> imports = ImportChecker.checkImports(cu);
			ConsolePrinter.print("Starting to check the API usage of the imported Collections:");
			for (String s: imports) {
				ConsolePrinter.print(" <" + s + ">");
			}
			ConsolePrinter.println();
			List<Misuse> misuses = DirectiveChecker.checkDirectives(cu, imports);
			long endTime = System.nanoTime();
			long timeTaken = (endTime - startTime) / 1000000;
			if (misuses.size() > 0) {
				ConsolePrinter.println("[FINISHED] Time: " + timeTaken + "ms, " + misuses.size() + " Misuse(s) found:");
			}
			else {
				ConsolePrinter.println("[FINISHED] Time: " + timeTaken + "ms, No misuses found");
			}
			for (Misuse m: misuses) {
				ConsolePrinter.println("  " + m);
			}
			if (JCollectView.view != null) {
				JCollectView.view.setMisuses(misuses, ifile, first);
			}
			if (first) {
				try {
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), ifile, true);
				} catch (PartInitException e) {
					ConsolePrinter.println("[ERROR] Could not open editor");
				}
			}
			createEditorMarkers(misuses, ifile);
		}
		catch (ParseProblemException e) {
			ConsolePrinter.println("[ERROR] ParseProblem occured");
		}
		catch (FileNotFoundException e) {
			ConsolePrinter.println("[ERROR] No such file found. You need to select a file");
		}
	}
	
	/**
	 * Creates markers in the editor to show the lines with errors
	 * @param misuses A list of misuses
	 * @param file The file where the misuses were found
	 */
	private void createEditorMarkers(List<Misuse> misuses, IFile file) {
		try {
			file.deleteMarkers("jcollect.warningmarker", true, IResource.DEPTH_INFINITE);
			file.deleteMarkers("jcollect.misusemarker", true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			ConsolePrinter.println("[ERROR] Could not delete old markers");
		}
		for (Misuse misuse: misuses) {
			IMarker marker = null;
			/*IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    IFileEditorInput input = (IFileEditorInput)editor.getEditorInput() ;
		    IFile file = input.getFile();
		    IResource res = (IResource) file;*/
			try {
				if (misuse.importance.equals(Misuse.IMPORTANCE_MISUSE)) {
					marker = file.createMarker("jcollect.misusemarker");
			        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				}
				else if (misuse.importance.equals(Misuse.IMPORTANCE_WARNING)) {
					marker = file.createMarker("jcollect.warningmarker");
			        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				}
				marker.setAttribute(IMarker.MESSAGE, misuse.description);
		        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
		        marker.setAttribute(IMarker.TEXT, "JCollect");
		        marker.setAttribute(IMarker.LINE_NUMBER, misuse.line);
			} catch (CoreException e1) {
				ConsolePrinter.println("[ERROR] Could not create marker");
			}
		}
	}
	
}
