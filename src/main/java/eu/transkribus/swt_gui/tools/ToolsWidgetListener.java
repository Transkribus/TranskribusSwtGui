package eu.transkribus.swt_gui.tools;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.HtrModel;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.dialogs.SimpleLaDialog;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog.HtrRecMode;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog.RecMode;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.util.GuiUtil;

public class ToolsWidgetListener implements SelectionListener {
	private final static Logger logger = LoggerFactory.getLogger(ToolsWidgetListener.class);
	
	TrpMainWidget mw;
	ToolsWidget tw;
	SWTCanvas canvas;
	Storage store = Storage.getInstance();
		
	public ToolsWidgetListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.canvas = mainWidget.getCanvas();
		this.tw = mainWidget.getUi().getToolsWidget();
		
		addListener();
	}
	
	private void addListener() {
		// use utiliy method from SWTUtil class to avoid nullpointer exceptions!
		SWTUtil.addSelectionListener(tw.batchLaBtn, this);
		SWTUtil.addSelectionListener(tw.blockSegBtn, this);
		SWTUtil.addSelectionListener(tw.lineSegBtn, this);
		SWTUtil.addSelectionListener(tw.wordSegBtn, this);
		
//		blockSegWPsBtn.addSelectionListener(this);
//		tw.baselineBtn.addSelectionListener(this);
		
		SWTUtil.addSelectionListener(tw.structAnalysisPageBtn, this);
		SWTUtil.addSelectionListener(tw.startRecogBtn, this);
		SWTUtil.addSelectionListener(tw.computeWerBtn, this);
				
		//OLD
//		tw.startOcrBtn.addSelectionListener(this);
//		tw.startOcrPageBtn.addSelectionListener(this);
//		tw.languagesTable.addCheckStateListener(this);
//		tw.scriptTypeCombo.addModifyListener(this);
//		tw.runHtrOnPageBtn.addSelectionListener(this);
//		tw.htrModelsCombo.addModifyListener(this);
	}
	
	List<String> getSelectedRegionIds() {
		List<String> rids = new ArrayList<>();
		for (ICanvasShape s : canvas.getScene().getSelectedAsNewArray()) {
			ITrpShapeType st = GuiUtil.getTrpShape(s);
			if (st==null || !(st instanceof TrpTextRegionType)) {
				continue;
			}
			rids.add(st.getId());
		}
		return rids;
	}
	
	
	boolean isLayoutAnalysis(Object s) {
		return (s == tw.batchLaBtn || s == tw.blockSegBtn || s == tw.blockSegWPsBtn || s == tw.lineSegBtn || s == tw.baselineBtn);
	}
	
	boolean needsRegions(Object s) {
		return s == tw.baselineBtn || s == tw.lineSegBtn;
	}
		
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		TrpDoc d = store.getDoc();
		if (d == null)
			return;
				
		final TrpPage p = store.getPage();
//		boolean isPar = (s == tw.scriptTypeCombo || s == tw.languagesTable);
		
//		if (!isPar) {
			if (!store.isPageLoaded()) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Not available", "No page loaded!");
				return;
			} else if (!store.isLoggedIn()) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Not available", "You are not logged in!");
				return;
			} else if (store.isLocalDoc()) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Not available", "The tools are only available for remote documents!");
				return;
			}
//		}

//		final TrpServerConn conn = store.getConnection();
		try {
			int docId = d.getMd().getDocId();

			PcGtsType pageData = store.getTranscript().getPageData();
			String jobId = null;
			int colId = store.getCurrentDocumentCollectionId();
			
			if (needsRegions(s) && !PageXmlUtils.hasRegions(pageData)) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "You have to define text regions first!");
				return;
			}
			
			if (isLayoutAnalysis(s) && store.isTranscriptEdited())
				mw.saveTranscription(false);
			
			// layout analysis:
			if (s == tw.blockSegBtn) {
				logger.info("Get new block seg.");
				jobId = store.analyzeBlocks(colId, docId, p.getPageNr(), pageData, false);
			} 
			else if(s == tw.blockSegWPsBtn) {
				logger.info("Get new block seg. in PS");
				jobId = store.analyzeBlocks(colId, docId, p.getPageNr(), pageData, true);
			}
			else if(s == tw.lineSegBtn) {
				logger.info("Get new line seg.");
				List<String> rids = getSelectedRegionIds();
				jobId = store.analyzeLines(colId, docId, p.getPageNr(), pageData, rids.isEmpty() ? null : rids);
			}
			else if(s == tw.wordSegBtn) {
				logger.info("Get new word seg.");
				List<String> rids = getSelectedRegionIds();
				jobId = store.analyzeWords(colId, docId, p.getPageNr(), pageData, rids.isEmpty() ? null : rids);
			}
			else if(s == tw.baselineBtn) {
				logger.info("Get new Baselines.");
				List<String> rids = getSelectedRegionIds();
				jobId = store.addBaselines(colId, docId, p.getPageNr(), pageData, rids.isEmpty() ? null : rids);
			} 
			else if(s == tw.batchLaBtn) {
				SimpleLaDialog laD = new SimpleLaDialog(mw.getShell());
				int ret = laD.open();
				if (ret == IDialogConstants.OK_ID) {
					final String pageStr = laD.getPages();
					final boolean doBlockSeg = laD.isDoBlockSeg();
					final boolean doLineSeg = laD.isDoLineSeg();
					jobId = store.analyzeLayout(colId, docId, pageStr, doBlockSeg, doLineSeg);
				}
			}
			
			// struct analysis:
			else if (s == tw.structAnalysisPageBtn) {
				mw.analyzePageStructure(tw.detectPageNumbers.getSelection(), tw.detectRunningTitles.getSelection(), tw.detectFootnotesCheck.getSelection());
			}
			
			else if(s == tw.computeWerBtn){
				
				TrpTranscriptMetadata ref = (TrpTranscriptMetadata)tw.refVersionChooser.selectedMd;
				TrpTranscriptMetadata hyp = (TrpTranscriptMetadata)tw.hypVersionChooser.selectedMd;

				if (ref != null && hyp != null) {
					logger.debug("Computing WER: " + ref.getKey() + " - " + hyp.getKey());
					final String result = store.computeWer(ref, hyp);
					MessageBox mb = new MessageBox(TrpMainWidget.getInstance().getShell(), SWT.ICON_INFORMATION | SWT.OK);
					mb.setText("Result");
					mb.setMessage(result);
					mb.open();
				}
				
//				final int refIndex = tw.refVersionCombo.getSelectionIndex();
//				final int hypIndex = tw.hypVersionCombo.getSelectionIndex();
//				if(refIndex > -1 && hypIndex > -1){
//					String refStr = tw.refVersionCombo.getItem(tw.refVersionCombo.getSelectionIndex());
//					String hypStr = tw.hypVersionCombo.getItem(tw.hypVersionCombo.getSelectionIndex());
//					TrpTranscriptMetadata ref = (TrpTranscriptMetadata)tw.refVersionCombo.getData(refStr);
//					TrpTranscriptMetadata hyp = (TrpTranscriptMetadata)tw.hypVersionCombo.getData(hypStr);
//					
//					logger.debug("Computing WER: " + ref.getKey() + " - " + hyp.getKey());
//					final String result = store.computeWer(ref, hyp);
//					MessageBox mb = new MessageBox(TrpMainWidget.getInstance().getShell(), SWT.ICON_INFORMATION | SWT.OK);
//					mb.setText("Result");
//					mb.setMessage(result);
//					mb.open();
//				}
				
			} else if (s == tw.startRecogBtn) {
				TextRecognitionDialog trD = new TextRecognitionDialog(mw.getShell());
				int ret = trD.open();
				final String pageStr = trD.getSelectedPages();
				if (ret == IDialogConstants.OK_ID) {
					if(trD.getRecMode().equals(RecMode.OCR)){
						logger.info("starting ocr for doc "+docId+", pages " + pageStr + " and col "+colId);
						jobId = store.runOcr(colId, docId, pageStr);
					} else { //HTR
						store.saveTranscript(colId, null);
						store.setLatestTranscriptAsCurrent();
						if(trD.getHtrRecMode().equals(HtrRecMode.HMM)) {
							final HtrModel model = trD.getSelectedHtrModel();
							logger.info("starting HMM HTR for doc " + docId + " on pages " + pageStr + " with model = " + model.getModelName());
							jobId = store.runHtr(colId, docId, pageStr, model.getModelName());
						} else if(trD.getHtrRecMode().equals(HtrRecMode.RNN)){
							final String netName = trD.getRnnName();
							final String dictName = trD.getDictName();
							logger.info("starting RNN HTR for doc " + docId + " on pages " + pageStr + " with net = " + netName + " | dict = " + dictName);
							jobId = store.runRnnHtr(colId, docId, pageStr, netName, dictName);
						} else {
							DialogUtil.showErrorMessageBox(TrpMainWidget.getInstance().getShell(), "Info", "Result: 42");
						}
					}
				}
				
//			} else if (s == tw.languageCombo) {
//				d.getMd().setLanguage(tw.languageCombo.getText());
//				mw.saveDocMetadata();
//			}
				// ocr:
//				else if (s == tw.startOcrBtn) {
//					mw.saveDocMetadata();
//					TrpDocMetadata md = store.getDoc().getMd();
//					if(md.getScriptType() == null || md.getLanguage() == null || md.getLanguage().isEmpty()) {
//						DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "Please select script type and language.");
//					} else {
//						logger.info("starting ocr for doc "+docId+" and col "+colId);
//						jobId = store.runOcr(colId, docId);
//					}
//				} 
//				else if (s == tw.startOcrPageBtn){
//					mw.saveDocMetadata();
//					final int pageNr = p.getPageNr();
//					TrpDocMetadata md = store.getDoc().getMd();
//					if(md.getScriptType() == null || md.getLanguage() == null || md.getLanguage().isEmpty()) {
//						DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "Please select script type and language.");
//					} else {
//						logger.info("starting ocr for doc "+docId+", page " + pageNr + " and col "+colId);
//						jobId = store.runOcr(colId, docId, pageNr);
//					}
//				} else if (s == tw.getRunHtrOnPageBtn()) {
//					store.saveTranscript(colId, null);
//					store.setLatestTranscriptAsCurrent();
//					final int pageNr = store.getPage().getPageNr();
//					final String model = tw.htrModelsCombo.getItem(tw.htrModelsCombo.getSelectionIndex());
//					logger.info("starting HTR for doc " + docId + " on page " + pageNr + " with model = " + model);
//					jobId = store.runHtrOnPage(colId, docId, pageNr, model);
//				} 
			}
//			else {
//				mw.onError("Error", "Unknown event!", null);
//				return;
//			}
			
			logger.debug("started job with id = "+jobId);
						
			mw.reloadJobList();
			mw.updatePageLock();

//			if (jobId != null) {
//				mw.getUi().selectJobListTab();
//			}
			
			
			
			// updates widget on job status:
//			LayoutJobUpdater ju = new LayoutJobUpdater(mainWidget, jobId);
//			ju.startJobThread();
		} catch (ClientErrorException cee) {
			final int status = cee.getResponse().getStatus();
			if(status == 400) {
				DialogUtil.showErrorMessageBox(this.mw.getShell(), "Error", 
						"A job of this type already exists for this page/document!");
			} else {
				mw.onError("Error", cee.getMessage(), cee);
			}
		} catch (Exception ex) {
			mw.onError("Error", ex.getMessage(), ex);
		}
		return;
	}
	
//	private void setButtonsEnabled(boolean isEnabled){
//		lw.getBlocksBtn().setEnabled(isEnabled);
//		lw.getBlocksInPsBtn().setEnabled(isEnabled);
//		lw.getLinesBtn().setEnabled(isEnabled);
//	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

//	@Override public void keyTraversed(TraverseEvent e) {
//		Object s = e.getSource();
//		if (s == tw.languageCombo && e.detail == SWT.TRAVERSE_RETURN) {
//			logger.debug("enter pressed on language field!");
////			mw.saveDocMetadata();
//		}
//	}
	
// Deprecated: old OCR section. 
//	@Override
//	public void modifyText(ModifyEvent e) {
//		Object s = e.getSource();
//		
//		TrpDoc d = store.getDoc();
//		
//		if (s == tw.scriptTypeCombo) {
//			logger.debug("new script type: "+tw.scriptTypeCombo.getText());
//			ScriptType st = EnumUtils.fromString(ScriptType.class, tw.scriptTypeCombo.getText());
//			if (d != null) {
//				d.getMd().setScriptType(st);
////				mw.saveDocMetadata();
//			}
//		}
//	}
//	@Override
//	public void checkStateChanged(CheckStateChangedEvent event) {
//		logger.debug("check state changed!!");
//		if (event.getSource() == tw.languagesTable.getTv()) {
//			String languages = tw.languagesTable.getSelectedLanguagesString();
//			logger.debug("setting languages: "+languages);
//			TrpDoc d = store.getDoc();
//			if (d != null)
//				d.getMd().setLanguage(languages);
//			return;
//		}
//	}
}
