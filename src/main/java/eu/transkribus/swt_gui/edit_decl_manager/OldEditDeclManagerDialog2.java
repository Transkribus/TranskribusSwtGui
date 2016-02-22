package eu.transkribus.swt_gui.edit_decl_manager;
//package org.dea.transcript.trp.gui.edit_decl_manager;
//
//import java.io.FileNotFoundException;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
//import javax.ws.rs.ServerErrorException;
//import javax.xml.bind.JAXBException;
//
//import org.dea.swt.util.DialogUtil;
//import org.dea.swt.util.Images;
//import org.dea.transcript.trp.client.util.SessionExpiredException;
//import org.dea.transcript.trp.core.exceptions.NoConnectionException;
//import org.dea.transcript.trp.core.model.beans.EdFeature;
//import org.dea.transcript.trp.core.model.beans.EdOption;
//import org.dea.transcript.trp.core.model.beans.TrpDocMetadata;
//import org.dea.transcript.trp.core.model.beans.auth.TrpRole;
//import org.dea.transcript.trp.gui.doc_overview.DocOverviewWidget;
//import org.dea.transcript.trp.gui.mainwidget.Storage;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.layout.RowLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Dialog;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.ToolTip;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
////public class CollectionManagerWidget extends Composite {
//public class OldEditDeclManagerDialog2 extends Dialog {
//	
//	private final static Logger logger = LoggerFactory.getLogger(OldEditDeclManagerDialog2.class);
//		
//	private Shell shlEditorialDeclaration;
//	
//	private EditDeclManagerDialog efd;
//	
//	private Button addFeatBtn, delFeatBtn, okBtn, cnclBtn, edtFeatsBtn;
//	
//	private List<Combo> featCombos = new LinkedList<>();
//	
//	private Composite featComposite;
//	
//	static final Storage store = Storage.getInstance();
//	
//	private static List<EdFeature> feats;
//	private static List<EdFeature> allFeats;
//	private static List<TrpDocMetadata> colDocs;
//
//	public OldEditDeclManagerDialog2(Shell parent, int style, DocOverviewWidget docOverviewWidget) {
//		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS));
////		this.setSize(800, 800);
//		this.setText("Editorial Declaration");
//	}
//	
//	/**
//	 * Open the dialog.
//	 * @return the result
//	 */
//	public Object open() {
//		createContents();
//		shlEditorialDeclaration.open();
//		shlEditorialDeclaration.layout();
//		Display display = getParent().getDisplay();
//		while (!shlEditorialDeclaration.isDisposed()) {
//			if (!display.readAndDispatch()) {
//				display.sleep();
//			}
//		}
//		return null;
//	}
//	
//	private void createContents() {
//		shlEditorialDeclaration = new Shell(getParent(), getStyle());
//		shlEditorialDeclaration.setText("Editorial Declaration");
//		shlEditorialDeclaration.setLayout(new GridLayout(1, true));
//		shlEditorialDeclaration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//
//
//		final Group editDeclGroup = new Group(shlEditorialDeclaration, SWT.NONE);
//		editDeclGroup.setLayout(new GridLayout(1, true));
//		shlEditorialDeclaration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		
////		final ScrolledComposite sc2 = new ScrolledComposite(editDeclGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
////	    sc2.setExpandHorizontal(true);
////	    sc2.setExpandVertical(true);
////		sc2.setContent(featComposite);
//		
//		featComposite = new Composite(editDeclGroup, SWT.NONE);
//		featComposite.setLayout(new GridLayout(2, false));
//		featComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		
//		try{
//			feats = store.getEditDeclFeatures();
//			allFeats = store.getAvailFeatures();
//			colDocs = store.getRemoteDocList();
//		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e) {
//			ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shlEditorialDeclaration, SWT.ICON_ERROR, e.getMessage(), "Error loading Editorial Declaration", -1, -1, true);
//		}
//		
//		//remove selected features from allFeats
//		Iterator<EdFeature> it = allFeats.iterator();
//		while(it.hasNext()){
//			EdFeature thisF = it.next();
//			for(EdFeature f : feats){
//				if(f.getFeatureId() == thisF.getFeatureId()){
//					it.remove();
//				}
//			}
//		}
//		
//		updateFeatList(featComposite);
//		
//		Composite btnComp = new Composite(editDeclGroup, SWT.NONE);
//		btnComp.setLayout(new RowLayout());
//		
//		addFeatBtn = new Button(btnComp, SWT.NONE);
//		addFeatBtn.setImage(Images.getOrLoad("/icons/add.png"));
//		addFeatBtn.setToolTipText("Add a transcription feature to this editorial declaration");
//		addFeatBtn.pack();
//		addFeatBtn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				EdFeature f = allFeats.get(0);
//				feats.add(f);
//				allFeats.remove(0);
//				if(allFeats.isEmpty()){
//					addFeatBtn.setEnabled(false);
//				}
//				
//				for(Combo c : featCombos){
//					c.remove(f.getTitle());
//				}
//				
//				createFeatCombo(featComposite, feats.size()-1, true);
//				
//				if(!delFeatBtn.isEnabled()){
//					delFeatBtn.setEnabled(false);
//				}
//				
//				editDeclGroup.pack();
//				shlEditorialDeclaration.pack();
//			}
//		});
//		if(allFeats.isEmpty()){
//			addFeatBtn.setEnabled(false);
//		}
//		
//		delFeatBtn = new Button(btnComp, SWT.NONE);
//		delFeatBtn.setImage(Images.getOrLoad("/icons/delete.png"));
//		delFeatBtn.setToolTipText("Delete the last transcription feature from this editorial declaration");
//		delFeatBtn.pack();
//		delFeatBtn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				final int lastInd = feats.size()-1;
//				EdFeature f = feats.get(lastInd);
//				allFeats.add(f);
//				feats.remove(lastInd);
//				if(!addFeatBtn.isEnabled()){
//					addFeatBtn.setEnabled(true);
//				}
//				
//				featCombos.get(featCombos.size()-1).dispose();
//				featCombos.remove(featCombos.size()-1);
//				
//				Control[] childs = featComposite.getChildren();
//				childs[childs.length-1].dispose();
//				
//				if(feats.isEmpty()){
//					delFeatBtn.setEnabled(false);
//				}
//				
//				for(Combo c : featCombos){
//					c.add(f.getTitle());
//					c.setData(f.getTitle(), f);
//				}
//				
//				editDeclGroup.pack();
//				shlEditorialDeclaration.pack();
//			}
//		});
//		if(feats.isEmpty()){
//			delFeatBtn.setEnabled(false);
//		}
//		
//		Composite mainBtnComp = new Composite(shlEditorialDeclaration, SWT.NONE);
//		mainBtnComp.setLayout(new RowLayout());
//		
//		okBtn = new Button(mainBtnComp, SWT.NONE);
//		okBtn.setText("OK");
//		okBtn.pack();
//		okBtn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {				
//				for(EdFeature f : feats){
//					logger.debug("Storing: " + f.getTitle());
//				}
//				
//				try {
//					store.saveEditDecl(feats);
//					getShell().dispose();
//				} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException | FileNotFoundException | JAXBException ex) {
//					ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shlEditorialDeclaration, SWT.ICON_ERROR, ex.getMessage(), "Error saving Editorial Declaration", -1, -1, true);
//					logger.error("Could not save!", ex);
//				}
//			}
//		});
//		
//		cnclBtn = new Button(mainBtnComp, SWT.NONE);
//		cnclBtn.setText("Cancel");
//		cnclBtn.pack();
//		cnclBtn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				getShell().dispose();
//			}
//		});
//		
//		edtFeatsBtn = new Button(mainBtnComp, SWT.RIGHT);
//		edtFeatsBtn.setText("Edit Features...");
//		
//		Label copyLabel = new Label(mainBtnComp, 0);
//		copyLabel.setText("Copy to document:");
//		
//		final Combo copyCombo = new Combo(mainBtnComp, SWT.NONE | SWT.READ_ONLY);
//		for(TrpDocMetadata md : colDocs){
//			if(md.getDocId() != store.getDoc().getId()){
//				copyCombo.add(md.getTitle());
//				copyCombo.setData(md.getTitle(), md);
//			}
//		}
//		copyCombo.pack();
//		
//		Button copyBtn = new Button(mainBtnComp, SWT.NONE);
//		copyBtn.setText("Copy");
//		copyBtn.pack();
//		copyBtn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				final String title = copyCombo.getText();
//				if(title != null && !title.isEmpty()){
//					TrpDocMetadata md = (TrpDocMetadata)copyCombo.getData(title);
//					logger.debug("Apply editdecl to doc: " + md.getDocId());
//					try {
//						store.saveEditDecl(md.getDocId(), feats);
//					} catch (SessionExpiredException | ServerErrorException
//							| IllegalArgumentException | NoConnectionException e1) {
//						ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shlEditorialDeclaration, SWT.ICON_ERROR, e1.getMessage(), "Error saving Editorial Declaration", -1, -1, true);
//						logger.error("Could not save!", e1);
//					}
//				}
//			}
//		});
//		
//		TrpRole role = store.getRoleOfUserInCurrentCollection();
//		if(role.getValue() >= TrpRole.Editor.getValue()){
//			edtFeatsBtn.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					if (efd == null || efd.getShell() == null || efd.getShell().isDisposed()) {
//						efd = new EditDeclManagerDialog(getShell(), SWT.NONE);
//						efd.open();
//					} else {
//						efd.getShell().setVisible(true);
//					}
//				}
//			});
//		} else {
//			edtFeatsBtn.setEnabled(false);
//			addFeatBtn.setEnabled(false);
//			delFeatBtn.setEnabled(false);
//			okBtn.setEnabled(false);
//		}
//		
//		shlEditorialDeclaration.pack();
//	}
//	
//	public void updateFeatList(Composite featComposite) {
////		featCombos.clear();
////		for(Control c : featComposite.getChildren()){
////			c.dispose();
////		}
//		TrpRole role = store.getRoleOfUserInCurrentCollection();
//		boolean isEditable = role.getValue() >= TrpRole.Editor.getValue();
//		for(int i = 0; i < feats.size(); i++){
//			createFeatCombo(featComposite, i, isEditable);
//		}
//
//	}
//
//	private void createFeatCombo(final Composite composite, final int index, final boolean isEnabled) {
//		final Combo combo = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
//		EdFeature selFeat = feats.get(index);
//		combo.add(selFeat.getTitle());
//		combo.setData(selFeat.getTitle(), selFeat);			
//		for(int i = 0; i < allFeats.size(); i++){
//			EdFeature f = allFeats.get(i);
//			combo.add(f.getTitle());
//			combo.setData(f.getTitle(), f);				
//		}
//		combo.select(0);
//		combo.setToolTipText(selFeat.getDescription());
//		featCombos.add(combo);
//		combo.setEnabled(isEnabled);
//		
//		final Group optGroup = new Group(composite, SWT.NONE);
//		optGroup.setLayout(new GridLayout(1, false));
//		optGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		
//		for(EdOption o : feats.get(index).getOptions()){
//			createOptRad(optGroup, o, index, isEnabled);
//		}
//		
//		combo.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				
//				final String title = combo.getText();
//				EdFeature f = (EdFeature)combo.getData(title);
//				
//				combo.setToolTipText(f.getDescription());
//				
//				
//				EdFeature formerFeat = feats.get(index);
//
//				feats.set(index, f);
//				Iterator<EdFeature> it = allFeats.iterator();
//				while (it.hasNext()){
//					if(it.next().getFeatureId() == f.getFeatureId()){
//						it.remove();
//						break;
//					}
//				}
//				allFeats.add(formerFeat);
//				for(Combo c : featCombos){
//					if(!c.equals(combo)){
//						//FIXME
//						c.remove(title);
//						c.add(formerFeat.getTitle());
//						c.setData(formerFeat.getTitle(), formerFeat);
//					}
//				}
//				
//				for(Control c : optGroup.getChildren()){
//					c.dispose();
//				}
//				
//				logger.debug("Select " + f.getTitle());
//				
//				for(EdOption o : f.getOptions()){
//					createOptRad(optGroup, o, index, true);
//				}
//				optGroup.getShell().pack();
////				optGroup.pack();
//				optGroup.layout();
//			}
//		});
//		
////		final Button delBtn = new Button(composite, SWT.NONE);
////		delBtn.setImage(Images.getOrLoad("/icons/delete.png"));
////		delBtn.setToolTipText("Remove this transcription feature");
////		delBtn.addSelectionListener(new SelectionAdapter() {
////			@Override
////			public void widgetSelected(SelectionEvent e) {
////				final String title = combo.getText();
////				EdFeature f = (EdFeature)combo.getData(title);
////				Iterator<EdFeature> it = feats.iterator();
////				while(it.hasNext()){
////					if(it.next().getFeatureId() == f.getFeatureId()){
////						it.remove();
////						break;
////					}
////				}
////				allFeats.add(f);
////				if(!addFeatBtn.isEnabled()){
////					addFeatBtn.setEnabled(true);
////				}
////				featCombos.remove(combo);
////				for(Combo c : featCombos){
////					c.add(f.getTitle());
////					c.setData(f.getTitle(), f);
////				}
////				optGroup.dispose();
////				combo.dispose();
////				delBtn.dispose();
////				shlEditorialDeclaration.pack();
////			}
////		});
//		
//	}
//
//	private void createOptRad(Group optGroup, EdOption o, final int featIndex, final boolean isEnabled) {
//		Button optRad = new Button(optGroup, SWT.RADIO);
//		optRad.setText(o.getText());
//		optRad.setData(o);
//		if(o.isSelected()){
//			optRad.setSelection(true);
//		}
//		final int optId = o.getOptionId();
//		optRad.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				for(EdOption o : feats.get(featIndex).getOptions()){
//					logger.debug("Select " + o.getText() + ": " + (o.getOptionId() == optId));
//					o.setSelected(o.getOptionId() == optId);
//				}
//			}
//		});
//		optRad.setEnabled(isEnabled);
//	}
//
//	public Shell getShell() { return shlEditorialDeclaration; }
//	
//	private final OldEditDeclManagerDialog2 getEditDeclManagerDialog(){
//		return this;
//	}
//	
//	public Composite getFeatComposite(){
//		return featComposite;
//	}
//}
