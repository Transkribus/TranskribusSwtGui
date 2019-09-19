package eu.transkribus.swt_gui.htr;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.builder.ExportCache;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;
import eu.transkribus.swt_gui.util.DocPagesSelector;
import eu.transkribus.util.TextRecognitionConfig;

public class HtrTextRecognitionDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTextRecognitionDialog.class);
	
	private HtrTextRecognitionConfigDialog trcd = null;
	
	private Button thisPageBtn, severalPagesBtn;
	private DocPagesSelector dps;
	private Button doLinePolygonSimplificationBtn, keepOriginalLinePolygonsBtn, doStoreConfMatsBtn;
	private Label structureLable;
	private Combo structureTags;
	private ExpandableComposite structure;
	
	private Storage store = Storage.getInstance();
	
	private TextRecognitionConfig config;
	private String pages;
	
	public HtrTextRecognitionDialog(Shell parent) {
		super(parent);
	}
    
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(3, false));
		
		thisPageBtn = new Button(cont, SWT.RADIO);
		thisPageBtn.setText("On this page");
		thisPageBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		thisPageBtn.setSelection(true);
		
		severalPagesBtn = new Button(cont, SWT.RADIO);
		severalPagesBtn.setText("Pages:");
		severalPagesBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		dps = new DocPagesSelector(cont, SWT.NONE, false, store.getDoc().getPages());
		dps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		dps.setEnabled(false);
		
		severalPagesBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dps.setEnabled(severalPagesBtn.getSelection());
			}
		});
		
		doLinePolygonSimplificationBtn = new Button(cont, SWT.CHECK);
		doLinePolygonSimplificationBtn.setText("Do polygon simplification");
		doLinePolygonSimplificationBtn.setToolTipText("Perform a line polygon simplification after the recognition process to reduce the number of output points and thus the size of the file");
		doLinePolygonSimplificationBtn.setSelection(true);
		doLinePolygonSimplificationBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		keepOriginalLinePolygonsBtn = new Button(cont, SWT.CHECK);
		keepOriginalLinePolygonsBtn.setText("Keep original line polygons");
		keepOriginalLinePolygonsBtn.setToolTipText("Keep the original line polygons after the recognition process, e.g. if they have been already corrected");
		keepOriginalLinePolygonsBtn.setSelection(false);
		keepOriginalLinePolygonsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		doStoreConfMatsBtn = new Button(cont, SWT.CHECK);
		doStoreConfMatsBtn.setText("Enable Keyword Spotting");
		doStoreConfMatsBtn.setToolTipText("The internal recognition result respresentation, needed for keyword spotting, will be stored in addition to the transcription.");
		doStoreConfMatsBtn.setSelection(true);
		doStoreConfMatsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		structure = new ExpandableComposite(cont, ExpandableComposite.COMPACT);
		structure.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		structure.setText("Structure");
		Fonts.setBoldFont(structure);
		
		Composite structureToolsGroup = new Composite(structure, SWT.SHADOW_ETCHED_IN);
		structureToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		structureToolsGroup.setLayout(new GridLayout(2, false));
		
		structure.setClient(structureToolsGroup);

		structureTags = new Combo(structureToolsGroup,  SWT.DROP_DOWN | SWT.READ_ONLY);
		structureTags.setToolTipText("Perform recognition only on chosen structure tags");
		structureTags.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		List<StructCustomTagSpec> tags = store.getStructCustomTagSpecs();
		

//		Set<Integer> collIds = null;
//		Set<Integer> docIds = null;
//
//		collIds = CoreUtils.createSet(store.getCollId());
//		docIds = CoreUtils.createSet(store.getDocId());
//		List<TrpDbTag> searchTags = null;
//		
//		try {
//			searchTags = store.getConnection().searchTags(collIds, docIds,  null, null, null, null, true,
//					false, null);
//		} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
//			e1.printStackTrace();
//		}

		structureTags.add("All");
		
		for(StructCustomTagSpec tag : tags) {
			logger.debug(tag.toString());
			String[] items = structureTags.getItems();
			
			if(!Arrays.stream(items).anyMatch(tag.getCustomTag().getType()::equals)) {
				structureTags.add(tag.getCustomTag().getType());
			}
			
		}
		
		structure.setExpanded(true);		
		
//		structure.addExpansionListener(new ExpansionAdapter() {
//			public void expansionStateChanged(ExpansionEvent e) {
//				
//				List<StructCustomTagSpec> tags = store.getStructCustomTagSpecs();
//				for(StructCustomTagSpec tag : tags) {
//					logger.debug(tag.toString());
//					String[] items = structureTags.getItems();
//					if(!Arrays.stream(items).anyMatch(tag.getCustomTag().getType()::equals)) {
//						structureTags.add(tag.getCustomTag().getType());
//					}
//					
//				}
//			}
//		});
		structureTags.select(0);
		
		SWTUtil.onSelectionEvent(keepOriginalLinePolygonsBtn, e -> {
			doLinePolygonSimplificationBtn.setEnabled(!keepOriginalLinePolygonsBtn.getSelection());
		});
		doLinePolygonSimplificationBtn.setEnabled(!keepOriginalLinePolygonsBtn.getSelection());
		
		Text configTxt = new Text(cont, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		configTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 6));
				
		Button configBtn = new Button(cont, SWT.PUSH);
		configBtn.setText("Configure...");
		configBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		configBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(trcd == null) {
					trcd = new HtrTextRecognitionConfigDialog(parent.getShell(), config);
					if(trcd.open() == IDialogConstants.OK_ID) {
						logger.info("OK pressed");
						config = trcd.getConfig();
						configTxt.setText(config.toString());
						store.saveTextRecognitionConfig(config);
					}
					trcd = null;
				} else {
					trcd.setVisible();
				}
			}
		});
		
		config = store.loadTextRecognitionConfig();
		logger.debug("Config loaded:" + config);
		if(config != null) {
			configTxt.setText(config.toString());
		}
		
		return cont;
	}

		
	@Override
	protected void okPressed() {
		List<String> structures = new ArrayList<>();
		structures.add(structureTags.getItem(structureTags.getSelectionIndex()));
		
		if(thisPageBtn.getSelection()) {
			pages = ""+store.getPage().getPageNr();
		} else if(severalPagesBtn.getSelection()) {
			pages = dps.getPagesText().getText();
		}
		
		if(pages == null || pages.isEmpty()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please specify pages for recognition.");
			return;
		}
		
		try {
			CoreUtils.parseRangeListStr(pages, store.getDoc().getNPages());
		} catch (IOException e) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Page selection is invalid.");
			return;
		}
		
		if(config == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Bad Configuration", "Please define a configuration.");
			return;
		}
		
		config.setRegions(structures);
		config.setKeepOriginalLinePolygons(keepOriginalLinePolygonsBtn.getSelection());
		config.setDoLinePolygonSimplification(doLinePolygonSimplificationBtn.getSelection());
		config.setDoStoreConfMats(doStoreConfMatsBtn.getSelection());
		
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text Recognition");
		newShell.setMinimumSize(300, 400);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(300, 400);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
	
	public TextRecognitionConfig getConfig() {
		return this.config;
	}
	
	public String getPages() {
		return this.pages;
	}

}
