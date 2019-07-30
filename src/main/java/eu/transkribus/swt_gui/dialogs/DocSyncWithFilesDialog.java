package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Assert;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.util.APreviewListViewer.PreviewListViewerListener;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.util.DocPageViewer;
import eu.transkribus.swt_gui.util.FileListViewer;

public class DocSyncWithFilesDialog extends Dialog {
	
	protected String typeOfFiles;
	protected TrpDoc target;
	protected List<File> sourceFiles;
	
	protected DocPageViewer targetViewer;
	protected FileListViewer sourceViewer;
	
	protected List<Pair<TrpPage, File>> matches;
	protected Text matchesText;
	
	protected SashForm sfVertical, sfHorizontal;
	protected Label infoLabel;
	protected PreviewListViewerListener listViewerListener;
	
	protected Object data;
	
	public DocSyncWithFilesDialog(Shell parentShell, String typeOfFiles) {
		super(parentShell);
		this.typeOfFiles = typeOfFiles;
	}
	
	public DocSyncWithFilesDialog(Shell parentShell, String typeOfFiles, TrpDoc target, List<File> sourceFiles) {
		this(parentShell, typeOfFiles);
		
		setData(target, sourceFiles);
	}
	
	public void setData(TrpDoc target, List<File> sourceFiles) {
		Assert.assertNotNull("target document null!", target);
		Assert.assertNotNull("source files null!", sourceFiles);
		
		this.target = target;
		this.sourceFiles = sourceFiles;		
	}	
	
	@Override protected boolean isResizable() {
	    return true;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		getShell().setText("Sync "+typeOfFiles+" files with current document");

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
//		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		infoLabel = new Label(container, SWT.WRAP | SWT.LEFT);
		infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 2));
		infoLabel.setText("Select source files on the left to match with pages of loaded document on the right");
		Fonts.setItalicFont(infoLabel);
//		titleLabel.setText("Select pages from the text source files on the right to correspond with pages on the target document on the left. \n"
//				+ "\n"
//				+ "Note: Pages are synced according to filename! Only checked pages will be synced."
//				);		
		
		sfVertical = new SashForm(container, SWT.VERTICAL);
		sfVertical.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sfVertical.setLayout(new GridLayout(1, true));

		sfHorizontal = new SashForm(sfVertical, SWT.HORIZONTAL);
		sfHorizontal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sfHorizontal.setLayout(new GridLayout(2, true));
		
		sourceViewer = new FileListViewer(sfHorizontal, 0, true, true, true);
		Fonts.setBoldFont(sourceViewer.getTitleLabel());
		sourceViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sourceViewer.setDataList(sourceFiles);
		sourceViewer.setTitle("Source files ("+sourceFiles.size()+")");
		
		targetViewer = new DocPageViewer(sfHorizontal, 0, true, true, false, true, false);
		Fonts.setBoldFont(targetViewer.getTitleLabel());
		targetViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		targetViewer.setDataList(target.getPages());
		targetViewer.setTitle("Loaded doc: '"+target.getMd().getTitle()+"' ("+target.getMd().getNrOfPages()+" pages)");
		
		listViewerListener = new PreviewListViewerListener() {
			@Override public void sortingChanged() {
				updateMatchesText();
			}

			@Override public void checkStatesChanged() {
				updateMatchesText();
			}
		};
		
		targetViewer.addListener(listViewerListener);
		sourceViewer.addListener(listViewerListener);
		
		matchesText = new Text(sfVertical, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		matchesText.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
//		Button reloadMatchesBtn = new Button(container, 0);
//		reloadMatchesBtn.setText("Reload matches");
//		SWTUtil.onSelectionEvent(reloadMatchesBtn, e -> updateMatchesText());
		
		sfHorizontal.setWeights(new int[] { 1, 1 } );
		sfVertical.setWeights(new int[] { 3, 1 } );
		
		updateMatchesText();

		return container;
	}
	
	private List<Pair<TrpPage, File>> computeCurrentMatches() {
		List<TrpPage> pages = targetViewer.getCheckedDataList();
		List<File> files = sourceViewer.getCheckedDataList();
		
		int N = Math.min(pages.size(), files.size());
		List<Pair<TrpPage, File>> matches = new ArrayList<>();
		for (int i=0; i<N; ++i) {
			matches.add(Pair.of(pages.get(i), files.get(i)));
		}
		
		return matches;
	}
	
	public List<Pair<TrpPage, File>> getMatches() {
		return matches;
	}
	
	/**
	 * Override this method in child classes to set custom parameter
	 */
	protected void setData() {
		this.data = null;
	}
	
	public Object getData() {
		return this.data;
	}
	
	private void updateMatchesText() {
		List<Pair<TrpPage, File>> matches = computeCurrentMatches();
		
		if (matches.size()>0) {
			String txt = "Syncing "+matches.size()+" files:\n\n";
			
//			int i=0;
//			int nPad = (matches.size()+"").length();
			for (Pair<TrpPage, File> match : matches) {
//				++i;
//				String nrStr = StringUtils.leftPad(""+i, nPad, "0");
				
				String trpPageStr = "Page "+match.getLeft().getPageNr()+" ("+match.getLeft().getImgFileName()+")";
				String fileStr = match.getRight().getName();
				
//				txt += "\t\t"+nrStr+": "+match.getLeft().getImgFileName()+"\t\t<-->\t\t"+match.getRight().getName()+"\n";
//				txt += "\t"+trpPageStr+"\t\t<--\t\t"+fileStr+"\n";
				txt += "\t"+fileStr+"\t\t-->\t\t"+trpPageStr+"\n";
			}
			matchesText.setText(txt);
		}
		else {
			matchesText.setText("No matches");
		}
	}
	
	@Override protected void okPressed() {
		matches = computeCurrentMatches();
		setData();
		
		super.okPressed();
	}

	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected Point getInitialSize() {
		return new Point(800, 800);
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      SWTUtil.centerShell(shell);
	}

}
