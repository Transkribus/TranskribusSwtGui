package eu.transkribus.swt_gui.comments_widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CommentTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class CommentsWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(CommentsWidget.class);
	
	CommentsTable commentsTable;
	Combo scopeCombo;
	Text commentText;
	Button refresh, addComment, editComment, deleteComment, showComments;
	Label intro;

	public CommentsWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		SashForm sf = new SashForm(this, SWT.VERTICAL);
		
		Composite top = new Composite(sf, 0);
		top.setLayout(new GridLayout(3, false));
				
		Label l = new Label(top, 0);
		l.setText("Enter your comment:");
		l.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		commentText = new Text(top,  SWT.MULTI);
		commentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
//		commentText.setMessage(String.format("Enter your comment...(Select text in editor first)")); // onyl works when SWT.SEARCH is set in Text constructor!
		
		//commentText.setText("test");
//		commentText.addModifyListener(new ModifyListener() {
//			@Override public void modifyText(ModifyEvent e) {
//				logger.debug("modifying comment...: "+commentText.getText());
//				editSelectedComment();
//			}
//		});
		
	    // add a focus listener
//	    FocusListener focusListener = new FocusListener() {
//	      public void focusGained(FocusEvent e) {
//	        Text t = (Text) e.widget;
//	        t.cut();
//	        //t.selectAll();
//	      }
//
//	      public void focusLost(FocusEvent e) {
//	        Text t = (Text) e.widget;
//	        if (t.getSelectionCount() > 0) {
//	          t.clearSelection();
//	        }
//	      }
//	    };
//	    commentText.addFocusListener(focusListener);
		

        addComment = new Button(top, SWT.PUSH);
        addComment.setImage(Images.ADD);
        addComment.setText("Add");
        addComment.setToolTipText("Adds the comment below to the current selection in the transcription widget");
        addComment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        addComment.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				addNewComment();
				commentText.setText("");
			}
		});
        
        editComment = new Button(top, SWT.PUSH);
        editComment.setImage(Images.PENCIL);
        editComment.setText("Edit selected");
        editComment.setToolTipText("Edits the selected comment with the text below");
        editComment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        editComment.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				editSelectedComment();
			}
		}); 
        
        deleteComment = new Button(top, SWT.PUSH);
        deleteComment.setImage(Images.DELETE);
        deleteComment.setText("Delete selected");
        deleteComment.setToolTipText("Deletes the selected comment from the list");
        deleteComment.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        deleteComment.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				deleteSelectedComment();
			}
		});        
        
        Composite bottom = new Composite(sf, 0);
        bottom.setLayout(new GridLayout(2, false));
        
		if (false) {
		Label scopeLabel = new Label(bottom, 0);
		scopeLabel.setText("Comments - scope: ");
		
		scopeCombo = new Combo(bottom, SWT.READ_ONLY | SWT.DROP_DOWN);
//		scopeCombo.setItems(new String[] {"Page", "Document"});
		scopeCombo.setItems(new String[] {"Page"});
		scopeCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				reloadComments();
			}
		});
		scopeCombo.select(0);
		}
		
		refresh = new Button(bottom, SWT.PUSH);
		refresh.setImage(Images.REFRESH);
		refresh.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				reloadComments();
			}
		});
		
		showComments = new Button(bottom, SWT.CHECK);
		showComments.setText("Highlight comments");
		showComments.setToolTipText("Display comments in transcription widget");
		showComments.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget()!=null)
					TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget().redrawText(true);
			}
		});
		showComments.setSelection(true);
		
		commentsTable = new CommentsTable(bottom, 0);
		commentsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		commentsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				refreshSelectedComment();
			}
		});
		
		commentsTable.addDoubleClickListener(new IDoubleClickListener() {	
			@Override public void doubleClick(DoubleClickEvent event) {
				showSelectedComment();
			}
		});

		
		sf.setWeights(new int[] { 35, 65 });
	}
	
	protected void showSelectedComment() {
		CommentTag c = getSelectedComment();
		if (c != null) {
			logger.debug("showing comment: "+c);
			TrpMainWidget.getInstance().showLocation(c);
		}
	}

	private void addNewComment() {
		if (commentText.getText().isEmpty()) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "Cannot add an empty comment!");
			return;
		}
		
		TrpMainWidget mw = TrpMainWidget.getInstance();
		
		boolean isTextSelectedInTranscriptionWidget = mw.isTextSelectedInTranscriptionWidget();
		if (!isTextSelectedInTranscriptionWidget) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "No text seleceted in transcription widget!");
			return;
		}
		
		Map<String, Object> atts = new HashMap<>();
		atts.put(CommentTag.COMMENT_PROPERTY_NAME, commentText.getText());
		mw.getTaggingWidgetListener().addTagForSelection(CommentTag.TAG_NAME, atts);
		reloadComments();
	}
	
	private void editSelectedComment() {
		CommentTag c = getSelectedComment();
		if (c != null) {
			c.setComment(commentText.getText());
			for (CustomTag ct : c.continuations) {
				if (ct instanceof CommentTag) // should always be true here
					((CommentTag)ct).setComment(commentText.getText());
			}
			
			commentsTable.refresh(c, true);
		}
	}
	
	private void deleteSelectedComment() {		
		CommentTag c = getSelectedComment();
		if (c != null) {
			c.getCustomTagList().deleteTagAndContinuations(c);
			reloadComments();
		}		
	}
	
	public CommentTag getSelectedComment() {
		IStructuredSelection sel = (IStructuredSelection) commentsTable.getSelection();
		if (!sel.isEmpty()) {
			return (CommentTag) sel.getFirstElement();
		}
		return null;
	}
	
	public void refreshSelectedComment() {
		CommentTag c = getSelectedComment();
		commentText.setText(c==null ? "" : c.getComment());
	}
	
	public void reloadComments() {
//		boolean docLevel = scopeCombo.getSelectionIndex() == 1; // TODO: get tags on doc-level!
		
		Storage store = Storage.getInstance();
		
		List<CommentTag> comments = CustomTagUtil.getIndexedCustomTagsForLines(store.getTranscript().getPage(), CommentTag.TAG_NAME);
		
//		List<CommentTag> comments = new ArrayList<>();
//		if (store.hasTranscript()) {
//			for (TrpTextRegionType r : store.getTranscript().getPage().getTextRegions(true)) {
//				for (TextLineType l : r.getTextLine()) {
//					TrpTextLineType tl = (TrpTextLineType) l;
//					List<CommentTag> cs = tl.getCustomTagList().getIndexedTags(CommentTag.TAG_NAME);
//					comments.addAll(cs);
//				}
//			}
//		}
		
		commentsTable.setInput(comments);
	}

	public boolean isShowComments() {
		return showComments.getSelection();
	}
	
	

}
