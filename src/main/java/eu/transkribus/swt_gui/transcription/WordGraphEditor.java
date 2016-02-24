package eu.transkribus.swt_gui.transcription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.Fonts;
import eu.transkribus.util.Utils;

public class WordGraphEditor extends Composite {
	public static enum EditType { /*RELOAD,*/ REPLACE, DELETE, ADD };
	public static class WordGraphEditData {
		public WordGraphEditData(EditType editType, boolean editAtCursor) {
			super();
			this.editType = editType;
			this.editAtCursor = editAtCursor;
		}
		public EditType editType;
		public boolean editAtCursor;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(WordGraphEditor.class);
	
	private ATranscriptionWidget trWidget;
	
	private TableViewer tableViewer;
	private Table table;
	
//	private static ArrayList<String[]> testData = new ArrayList<String[]>();
	int nColumnsInData=0;
	int editIndex=-1;
	TreeSet<Integer> editedIndices = new TreeSet<Integer>();
	
	
	String lineText = "";
	String[][] wgMatrix = new String[][]{};
	
	boolean editing=false;
	
	public class WgEditorColumnLabelProvider extends ColumnLabelProvider {
		int index;
		
		public WgEditorColumnLabelProvider(int index) {
			this.index = index;	
		}
		
		Font font = Fonts.createFont("Arial", 14, SWT.NORMAL);
		Font boldFont = Fonts.createFont("Arial", 14, SWT.BOLD);
	      @Override
	      public String getText(Object element) {
	    	List<String> words = (List<String>) element;
	        if (index < words.size())
	        	return words.get(index);
	        else
	        	return "";
	      }
	     
//	  	public void update(ViewerCell cell) {
//			Object element = cell.getElement();
//			cell.setText(getText(element));
//			Image image = getImage(element);
//			cell.setImage(image);
//			cell.setBackground(getBackground(element));
//			cell.setForeground(getForeground(element));
//			cell.setFont(getFont(element));
//		}
	      
	  	@Override
		public Font getFont(Object element) {
	  		if (rowIndex(element) == 0) {
	  			return boldFont;
	  		} else
	  			return font;
	  	}
	     
	    @Override
	  	public Color getBackground(Object element) {
	    	if (rowIndex(element) == 0) {
	    		return Colors.getSystemColor(SWT.COLOR_GRAY);
	    	}
	    	return null;
		}
	      
	  	@Override
		public Color getForeground(Object element) {
	    	if (rowIndex(element) == 0) {
	    		
	    		// highlight currently select word from transcription editor:
	    		StyledText text = trWidget.getText();
	    		int co = text.getCaretOffset();
	    		int coLine = co - text.getOffsetAtLine(text.getLineAtOffset(co));
	    		String lineText = text.getLine(text.getLineAtOffset(co));
	    		int wordIndex = Utils.getWordIndexFromCursor(lineText, coLine);
	    		
	    		if (wordIndex == index) {
	    			return Colors.getSystemColor(SWT.COLOR_DARK_RED);
	    		}
	    	}
	  	
	  		return null;
		}			      
	};
	
	public class WgEditorEditingSupport extends EditingSupport {
		int index;
		
		public WgEditorEditingSupport(int index, TableViewer viewer) {
			super(viewer);
			this.index = index;
		}

		TextCellEditor e = new TextCellEditor(getTableViewer().getTable());
		
		TableViewer getTableViewer() { return (TableViewer) getViewer(); }

		@Override
		protected CellEditor getCellEditor(Object element) {
			return e;
		}

		@Override
		protected boolean canEdit(Object element) {
//			logger.debug("canEdit, rowIndex = "+rowIndex(element));
			return (rowIndex(element) == 0 && index<nColumnsInData);
		}

		@Override
		protected Object getValue(Object element) {
			logger.debug("getValue called: "+element);
	    	List<String> words = (List<String>) element;
	        if (index < words.size())
	        	return words.get(index);
	        else
	        	return "";
		}

		@Override
		protected void setValue(Object element, Object value) {
			String newWord = (String) value;
			logger.debug("setValue called: "+element+ " newWord: '"+newWord+"'");
	    	List<String> words = (List<String>) element;
			
			String newSentence = "";
			for (int i=0; i<words.size(); ++i) {
				if (i == index) {
					if (!newWord.isEmpty())
						newSentence += newWord+" ";
				} else
					newSentence += words.get(i)+" ";
			}
			newSentence = newSentence.trim();
			
			sendEditEvent(index, newWord, newWord.isEmpty() ? EditType.DELETE : EditType.REPLACE, false);
		}
	};
	
	public WordGraphEditor(Composite parent, int style, ATranscriptionWidget trWidget) {
		super(parent, style);
		this.trWidget = trWidget;
		init();
	}
	
	private void init() {
		setLayout(new GridLayout(1, false));
//		setLayout(new FillLayout());

		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.setContentProvider(new ArrayContentProvider());
		
//		AutoResizeTableLayout layout = new AutoResizeTableLayout(tableViewer.getTable());
//		tableViewer.getTable().setLayout(layout);
	
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(false);
//		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setToolTipText("Single click to replace word of line in the same column, double click to replace word under cursor in transcription widget");
//		initEditBehaviour(0);
			
		table.addListener(SWT.MouseDown, new Listener() {
			boolean doubleClick=false;
			
			@Override
			public void handleEvent(final Event event) {
//				logger.debug("click count = "+event.count);
				if (event.count <= 1) {
					doubleClick = false;
					Display.getDefault().timerExec(Display.getDefault().getDoubleClickTime(),
//					Display.getDefault().timerExec(100,
	                        new Runnable() {  
	                            public void run() {  
	                                if (!doubleClick) {
	                                    handleMouseClicks(false, event);  
	                                }
	                            }  
	                        });
					
				} else if (event.count == 2) {
					doubleClick = true;
					handleMouseClicks(true, event);
				} else
					return;
			}
		});
		
		if (false)
		table.addListener(SWT.PaintItem, new Listener() {

			@Override public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int ci = event.index;
				int ri = table.indexOf(item);
				
				// draw line after first row:
				if (ri == 0) {
					Rectangle b = item.getBounds(ci);
//					logger.trace("bounds = "+b+"text = '"+item.getText(ci)+"' x,y,w,h = "+event.x+","+event.y+","+event.width+","+event.height);
					
					int lw = 2;
					event.gc.setLineWidth(lw);
					event.gc.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
					event.gc.drawLine(b.x, b.y+b.height-lw, b.x+b.width, b.y+b.height);
				}
			}
			
		});
	}
	
	private void handleMouseClicks(boolean isDoubleClick, Event event) {
		Point pt = new Point(event.x, event.y);
		ViewerCell cell = tableViewer.getCell(pt);
		if (cell == null)
			return;
		
		int row = table.getSelectionIndex();
		int col = cell.getColumnIndex();
		
		if ( (isDoubleClick || row > 0) && !cell.getText().isEmpty() ) {
			System.out.println("Item " + row + "-" + col);
			editIndex = col;
			editedIndices.add(col);
			
			EditType type = EditType.REPLACE;
			
//			String txt = cell.getText();
			List<String> firstRow = (List<String>) tableViewer.getElementAt(0);
			List<String> editRow = (List<String>) cell.getElement();
			
			String newSentence = "";
			String text = "";
			for (int i=0; i<firstRow.size(); ++i) {
				if (col == i) {
					text = editRow.get(i);
					newSentence += text+" ";
					if (firstRow.get(i).isEmpty())
						type = EditType.ADD;
				}
				else {
					newSentence += firstRow.get(i)+" ";
				}
			}
			newSentence = newSentence.trim();
			
			sendEditEvent(col, text, type, isDoubleClick);
		}
	}
	
	private void setTestData() {
		final String [][] testData = new String[][] {
			new String[]{"Zwischen", "Reschprunner", "und","Schrogsfärdien", "strissigen", "handlung,"},
			new String[]{"Zischen", "Rechprunner", "ud","Schrogsfärdlen", "strittiger", "Handlung"},
			new String[]{"Wischen", "Rechprumer", "ad","Schrogsfaden", "streitigen", "hamdlung"},
			new String[]{"Zwitschen", "Rechpramer", "","schrogsfärdlen", "strittigen", "händlung,"},
			new String[]{"Mischen", "Reschpruner", "","Schrogsfaerdlen", "", "Handhabung,"},
		};
				
		setWordGraphMatrix("Zwischen Reschprunner und Schrogsfaden strittigen", testData, false);
	}
	
	private void initEditBehaviour(final int type) {
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer, new FocusCellOwnerDrawHighlighter(tableViewer));
		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
		    @Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
		    	logger.debug(event.toString());
		    	
				boolean singleSelect = ((IStructuredSelection)tableViewer.getSelection()).size() == 1;
				
				int mouseActivationType= 
						type == 0 ? ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION : ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION;
				
				boolean isLeftMouseSelect = event.eventType == mouseActivationType && ((MouseEvent)event.sourceEvent).button == 1;

				return singleSelect && (isLeftMouseSelect
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
						|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
		    	
		    	
		        // Enable editor only with mouse double click
//		        if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
//		            EventObject source = event.sourceEvent;
//		            if (source instanceof MouseEvent && ((MouseEvent)source).button == 3)
//		                return false;
//
//		            return true;
//		        }
//
//		        return false;
		    }
		};
		
		TableViewerEditor.create(tableViewer, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL | 
			    ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | 
			    ColumnViewerEditor.TABBING_VERTICAL |
			    ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}
	
	private ArrayList<List<String> > matrix2ArrayList(String lineText, String[][] matrix, boolean onlyUnique) {
		ArrayList<ArrayList<String>> columns = new ArrayList<>();
		
		// add first row which is the text of the given line
		String[] words = lineText.trim().split("\\s+");
		for (int j=0; j<words.length; ++j) {
			if (columns.size() <= j) {
				columns.add(new ArrayList<String>());
			}
			columns.get(j).add(words[j]);
		}
		
		// add suggestions:
		int nRows=0;
		for (int i=0; i<matrix.length; ++i) {
			for (int j=0; j<matrix[i].length; ++j) {
				if (columns.size() <= j) {
					columns.add(new ArrayList<String>());
				}
				ArrayList<String> col = columns.get(j);
				if (col.isEmpty()) // first row must be filled with either a word or an empty string!
					col.add("");
				
				if (!col.contains(matrix[i][j]) || !onlyUnique) {
					col.add(matrix[i][j]);
					if (columns.size() > nRows)
						nRows = columns.size();
				}
			}
		}
		nRows++; // +1 for the first row!
		
		// now create a list of rows:
		ArrayList<List<String>> data = new ArrayList<>();
		for (int i=0; i<nRows; ++i) {
			ArrayList<String> row = new ArrayList<>();
			for (int j=0; j<columns.size(); ++j) {
				if (i < columns.get(j).size())
					row.add(columns.get(j).get(i));
				else
					row.add("");
			}
			
			data.add(row);
		}
		
//		for (int i=0; i<matrix.length; ++i) {
//			
//			List<String> uniqueWords = new ArrayList<String>();
//			for (int j=0; j<matrix[i].length; ++j) {
//				if (!uniqueWords.contains(matrix[i][j]) || !onlyUnique) {
//					uniqueWords.add(matrix[i][j]);
//				}
//			}
//			
//			data.add(uniqueWords);
//		}
		
		return data;
	}
	
	private void sendEditEvent(int editIndex, String text, EditType type, boolean editAtCursor) {
		if (editIndex >= 0 && editIndex < table.getColumnCount()) {
			editing = true;
			
			int nWords = lineText.trim().split("\\s+").length;
			if (editIndex > nWords) // no edit index > nWords allowed -> word will be moved to the end of the line
				editIndex = nWords;
			
			// TODO: fire ModifyEvent
			Event e = new Event();
			e.item = this;
			
			e.index = editIndex;
//			e.text = type == EditType.DELETE ? "" : line.trim().split("\\s+")[editIndex];
			e.text = text;
			
			e.data = new WordGraphEditData(type, editAtCursor);
			notifyListeners(SWT.Modify, e);
			
			editing = false;
			if (type == EditType.DELETE) {
				TreeSet<Integer> newIndices = new TreeSet<Integer>();
				for (Integer i : editedIndices) {
					if (i > e.index)
						newIndices.add(i-1);
					else if (i < e.index)
						newIndices.add(i);
				}
				editedIndices = newIndices;
			} else
				editedIndices.add(editIndex);
		}
		
	}
	
	/**
	 * Sets the data for the word graph editor.
	 * @param line The current value of the line.
	 * @param wordGraphMatrix The matrix of the n-best transcripted lines.
	 * @param editIndex The index of the column that was edited. Set to -1 if whole data is reset.
	 */
	public void setWordGraphMatrix(String line, String[][] wordGraphMatrix, boolean fromCache) {
		if (false) return;
		
		wgMatrix = wordGraphMatrix;
		lineText = line==null ? "" : line;
				
		if (wgMatrix==null) {
			initColumns(null);		
			tableViewer.setInput(null);
			return;
		}
		
		lineText.split("\\s");
		
		ArrayList<List<String> > data = matrix2ArrayList(lineText, wgMatrix, true);
		
//		tableViewer.setInput(null);
		
		initColumns(data);
		tableViewer.setInput(data);
		
		if (!fromCache) {
			editedIndices.clear();
		}
//		tableViewer.refresh();
		
		// resize all columns to max text size of row:
		for (TableColumn tc : tableViewer.getTable().getColumns())
			tc.pack();
	}
	
	public boolean isEditing() { return editing; }
	
	public void refresh() {
		tableViewer.refresh();
	}
	
	public void reload() {
		setWordGraphMatrix(lineText, wgMatrix, false);
	}
	
	private int colIndex(TableItem ti, int x, int y) {
		return -1;
		
	}
	
	private int rowIndex(Object element) {
		int i=0;
		for (List<String> row : (ArrayList<List<String>>)tableViewer.getInput()) {
			if (row == element) {
				return i;
			}
			++i;
		}
		return -1;
	}
		
	private void initColumns(ArrayList<List<String>> data) {
		table.setRedraw(false);
		tableViewer.setInput(null);
		final int MIN_COLS = 10;
		nColumnsInData = 0;
		if (data == null)
			return;
		
		for (Collection<String> row : data) {
			if (row.size() > nColumnsInData)
				nColumnsInData = row.size();
		}
		
//		if (nColumns < MIN_COLS)
//			nColumns = MIN_COLS;
		
		final int columnCount = table.getColumnCount();
		int diff = Math.max(MIN_COLS, nColumnsInData) - table.getColumnCount();
		for (int i=table.getColumnCount(); i<columnCount+diff; ++i) {
			logger.debug("creating column "+i);
			final int index = i;
			final TableViewerColumn col1 = new TableViewerColumn(tableViewer, SWT.NONE);
			col1.getColumn().setText(""+(i+1));
			col1.getColumn().setWidth(150);
			col1.setLabelProvider(new WgEditorColumnLabelProvider(i));
			if (false)
				col1.setEditingSupport(new WgEditorEditingSupport(index, tableViewer));
		}
		
		table.setRedraw(true);
	}
	
	public static void main(String [] args) {
//		String tmp = "";
//		for (String s : tmp.trim().split("\\s+")) {
//			System.out.println("split = '"+s+"'");
//		}
		
		Pattern pattern = Pattern.compile("\\S+");
	    Matcher matcher = pattern.matcher(" this is a text    with words?!234	 adf ee i");
	    // Check all occurrences
	    while (matcher.find()) {
	        System.out.print("Start index: " + matcher.start());
	        System.out.print(" End index: " + matcher.end());
	        System.out.println(" Found: " + matcher.group());
	    }
		
		
	}


}
