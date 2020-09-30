package jcollect.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import jcollect.types.Misuse;
import jcollect.util.ConsolePrinter;

/**
 * The custom JCollect view that shows a table with the found misuses
 * @author Finn Carstensen
 */
public class JCollectView extends ViewPart {
	private Table table;
	private Map<Integer, IFile> entries;
	private int currentSize;
	public static JCollectView view;
	
	/**
	 * Initializes the view
	 */
	public JCollectView() {
		super();
		view = this;
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {
		entries = new HashMap<>();
		currentSize = 0;
		table = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);
		String[] titles = {"File", "Line", "Type", "Name", "Description"};
		List<TableColumn> columns = new ArrayList<TableColumn>();
		for (int i = 0; i < 5; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			columns.add(column);
		}
		parent.addControlListener(new ControlAdapter() {
			
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width - 2 * table.getBorderWidth();
				if (preferredSize.y > area.height + table.getHeaderHeight()) {
					Point vBarSize = table.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				Point oldSize = table.getSize();
				if (oldSize.x > area.width) {
					for (int i = 0; i < 5; i++) {
						switch (i) {
						case 0: columns.get(i).setWidth(width * 3 / 20); break;
						case 1: columns.get(i).setWidth(width / 20); break;
						case 2: columns.get(i).setWidth(width / 10); break;
						case 3: columns.get(i).setWidth(width / 5); break;
						case 4: columns.get(i).setWidth(width * 10 / 20); break;
						}
					}
					table.setSize(area.width, area.height);
				}
				else {
					table.setSize(area.width, area.height);
					for (int i = 0; i < 5; i++) {
						switch (i) {
						case 0: columns.get(i).setWidth(width * 3 / 20); break;
						case 1: columns.get(i).setWidth(width / 20); break;
						case 2: columns.get(i).setWidth(width / 10); break;
						case 3: columns.get(i).setWidth(width / 5); break;
						case 4: columns.get(i).setWidth(width * 10 / 20); break;
						}
					}
				}
			}
			
		});
		table.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					int selectedEntry = table.getSelectionIndex();
					IFile file = entries.getOrDefault(selectedEntry, null);
					if (file != null) {
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IEditorPart editorpart = page.getActiveEditor();
						if (editorpart != null && editorpart instanceof ITextEditor) {
							ITextEditor editor = (ITextEditor) editorpart;
							IDocumentProvider provider = editor.getDocumentProvider();
							IDocument document = provider.getDocument(editor.getEditorInput());
							try {
								int line = Integer.valueOf(table.getSelection()[0].getText(1)) - 1;
							    int length = document.getLineLength(line);
							    int start = document.getLineOffset(line);
							    editor.selectAndReveal(start, length);
							}
							catch (BadLocationException e) {
								ConsolePrinter.println("[ERROR] Could not highlight line");
							}
						}
					}
				} catch (PartInitException e) {
					ConsolePrinter.println("[ERROR] Could not open editor");
				}
			}
			
		});
	}
	
	/**
	 * Adds a new misuse entry to the table
	 * @param m The misuse to be displayed
	 */
	private void addItem(Misuse m, String file) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(0, file);
		item.setText(1, String.valueOf(m.line));
		item.setText(2, m.importance);
		item.setText(3, m.type);
		item.setText(4, m.description);
	}

	/**
	 * Sets the given misuses as the new content for the table and creates listener for click events on the entries
	 * @param misuses List of misuses
	 * @param file The file where the misuses were found
	 */
	public void setMisuses(List<Misuse> misuses, IFile file, boolean clear) {
		if (clear) {
			table.removeAll();
			entries.clear();
			currentSize = 0;
		}
		for (Misuse misuse: misuses) {
			addItem(misuse, file.getName());
			entries.put(currentSize++, file);
		}
	}
}