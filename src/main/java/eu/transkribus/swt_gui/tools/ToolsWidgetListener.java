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

import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ThumbnailManager;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.dialogs.HtrTrainingDialog;
import eu.transkribus.swt_gui.dialogs.OcrDialog;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog2;
import eu.transkribus.swt_gui.dialogs.la.LayoutAnalysisDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.GuiUtil;
import eu.transkribus.util.OcrConfig;
import eu.transkribus.util.TextRecognitionConfig;

public class ToolsWidgetListener implements SelectionListener {
	private final static Logger logger = LoggerFactory.getLogger(ToolsWidgetListener.class);
	
	TrpMainWidget mw;
	ToolsWidget tw;
	SWTCanvas canvas;
	Storage store = Storage.getInstance();
	
	ThumbnailManager tm;
	HtrTrainingDialog htd;
	@Deprecated
	TextRecognitionDialog trd;
	OcrDialog od;
	TextRecognitionDialog2 trd2;
	LayoutAnalysisDialog laDiag;
		
	public ToolsWidgetListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.canvas = mainWidget.getCanvas();
		this.tw = mainWidget.getUi().getToolsWidget();
		
		addListener();
	}
	
	private void addListener() {
		// use utiliy method from SWTUtil class to avoid nullpointer exceptions!
		SWTUtil.addSelectionListener(tw.batchLaBtn, this);
		SWTUtil.addSelectionListener(tw.regAndLineSegBtn, this);
		SWTUtil.addSelectionListener(tw.lineSegBtn, this);
		SWTUtil.addSelectionListener(tw.wordSegBtn, this);
		
//		blockSegWPsBtn.addSelectionListener(this);
//		tw.baselineBtn.addSelectionListener(this);
		
		SWTUtil.addSelectionListener(tw.structAnalysisPageBtn, this);
		SWTUtil.addSelectionListener(tw.ocrBtn, this);
		SWTUtil.addSelectionListener(tw.htrTrainBtn, this);
		SWTUtil.addSelectionListener(tw.recogBtn, this);
		SWTUtil.addSelectionListener(tw.computeWerBtn, this);
		SWTUtil.addSelectionListener(tw.compareVersionsBtn, this);
				
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
		return (s == tw.batchLaBtn || s == tw.regAndLineSegBtn || s == tw.lineSegBtn || s == tw.baselineBtn);
	}
	
	boolean needsRegions(Object s) {
		return s == tw.baselineBtn || s == tw.lineSegBtn;
	}
		
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		
		if (!store.isDocLoaded()) {
			DialogUtil.showErrorMessageBox(mw.getShell(), "Not available", "No document loaded!");
			return;
		}
				
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

//		final TrpServerConn conn = store.getConnection();
		try {
			TrpDoc d = store.getDoc();
			int docId = d.getMd().getDocId();
			TrpPage p = store.getPage();

			PcGtsType pageData = store.getTranscript().getPageData();
			String jobId = null;
			List<String> jobIds=null;
			
			int colId = store.getCurrentDocumentCollectionId();
			
			if (needsRegions(s) && !PageXmlUtils.hasRegions(pageData)) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "You have to define text regions first!");
				return;
			}
			
			if (isLayoutAnalysis(s) && store.isTranscriptEdited())
				mw.saveTranscription(false);
			
			// layout analysis:
//			if (s == tw.blockSegBtn) {
//				logger.info("Get new block seg.");
//				jobId = store.analyzeBlocks(colId, docId, p.getPageNr(), pageData, false);
//			} 
//			else if(s == tw.blockSegWPsBtn) {
//				logger.info("Get new block seg. in PS");
//				jobId = store.analyzeBlocks(colId, docId, p.getPageNr(), pageData, true);
//			}
			else if(s == tw.regAndLineSegBtn || s == tw.lineSegBtn) {
				boolean analRegions = s==tw.regAndLineSegBtn;
				logger.info("Get regions, lines and baselines, analyRegions = "+analRegions);
				
				List<String> rids = getSelectedRegionIds();
				String jobImpl = LayoutAnalysisDialog.getJobImplForMethod(tw.getSelectedLaMethod());
				logger.debug("jobImpl = "+jobImpl);
				
//				jobId = store.analyzeLines(colId, docId, p.getPageNr(), pageData, rids.isEmpty() ? null : rids);
				jobIds = store.analyzeLayoutOnCurrentTranscript(rids,analRegions, true, false, jobImpl, null);			
			}
//			else if(s == tw.lineSegBtn) {
//				logger.info("Get lines and baselines");
//				List<String> rids = getSelectedRegionIds();
////				boolean hasRegIds = CoreUtils.isEmpty(rids);
//				
//				jobId = store.analyzeLines(colId, docId, p.getPageNr(), pageData, rids.isEmpty() ? null : rids);
//				
//				jobIds = store.analyzeLayoutOnCurrentTranscript(rids,
//						false, true, false, laDiag.getJobImpl(), null);
//				
//			}
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
				if(laDiag != null && SWTUtil.isDisposed(laDiag.getShell())) {
					laDiag.setVisible();
				} else {
					laDiag = new LayoutAnalysisDialog(mw.getShell());
					int ret = laDiag.open();
					if (ret == IDialogConstants.OK_ID) {
						List<String> rids = getSelectedRegionIds();
						logger.debug("selected regIds = "+CoreUtils.join(rids));
						
						// FIXME: if pageStr contains only current page nr, select currently selected transcript !?????						
						if (!laDiag.isCurrentTranscript()) {
							logger.debug("running la on pages: "+laDiag.getPages());
							jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(laDiag.getPages(),
										laDiag.isDoBlockSeg(), laDiag.isDoLineSeg(), laDiag.isDoWordSeg(), laDiag.getJobImpl(), null);
						} else {
							logger.debug("running la on current transcript and selected rids: "+CoreUtils.join(rids));
							jobIds = store.analyzeLayoutOnCurrentTranscript(rids,
									laDiag.isDoBlockSeg(), laDiag.isDoLineSeg(), laDiag.isDoWordSeg(), laDiag.getJobImpl(), null);	
						}
					}
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
				
			}else if (s == tw.compareVersionsBtn) {
				
				TrpTranscriptMetadata ref = (TrpTranscriptMetadata)tw.refVersionChooser.selectedMd;
				TrpTranscriptMetadata hyp = (TrpTranscriptMetadata)tw.hypVersionChooser.selectedMd;
				
				ArrayList<String> refText = new ArrayList<String>(); 
				ArrayList<String> hypText = new ArrayList<String>();
				
				if (ref != null && hyp != null) {

					for (TrpRegionType region : ref.unmarshallTranscript().getPage().getTextRegionOrImageRegionOrLineDrawingRegion()){
						if (region instanceof TrpTextRegionType){
							for (TextLineType line : ((TrpTextRegionType) region).getTextLine()){
								refText.add(((TrpTextLineType) line).getUnicodeText());
								//refText = refText.concat(region.getUnicodeText());
							}
						}
					}
					for (TrpRegionType region : hyp.unmarshallTranscript().getPage().getTextRegionOrImageRegionOrLineDrawingRegion()){
						if (region instanceof TrpTextRegionType){
							for (TextLineType line : ((TrpTextRegionType) region).getTextLine()){
								hypText.add(((TrpTextLineType) line).getUnicodeText());
							//hypText = hypText.concat(region.getUnicodeText());
							}
						}
					}

				
					DiffCompareTool diff = new DiffCompareTool(mw.getShell().getDisplay(), hypText, refText);
					
					mw.openVersionsCompareDialog(diff.getResult());
				}					
			
			} else if (s == tw.ocrBtn) {
				//OLD TEXT RECOGNITION DIALOG
//				if(trd != null) {
//					trd.setVisible();
//				} else {
//					trd = new TextRecognitionDialog(mw.getShell());
//					int ret = trd.open();
//					final String pageStr = trd.getSelectedPages();
//					if (ret == IDialogConstants.OK_ID) {
//						if(trd.getRecMode().equals(RecMode.OCR)){
//							logger.info("starting ocr for doc "+docId+", pages " + pageStr + " and col "+colId);
//							jobId = store.runOcr(colId, docId, pageStr);
//						} else { //HTR
//							store.saveTranscript(colId, null);
//							store.setLatestTranscriptAsCurrent();
//							if(trd.getHtrRecMode().equals(HtrRecMode.HMM)) {
//								final HtrModel model = trd.getSelectedHtrModel();
//								logger.info("starting HMM HTR for doc " + docId + " on pages " + pageStr + " with model = " + model.getModelName());
//								jobId = store.runHtr(colId, docId, pageStr, model.getModelName());
//							} else if(trd.getHtrRecMode().equals(HtrRecMode.RNN)){
//								final String netName = trd.getRnnName();
//								final String dictName = trd.getDictName();
//								logger.info("starting RNN HTR for doc " + docId + " on pages " + pageStr + " with net = " + netName + " | dict = " + dictName);
//								jobId = store.runRnnHtr(colId, docId, pageStr, netName, dictName);
//							} else {
//								DialogUtil.showErrorMessageBox(TrpMainWidget.getInstance().getShell(), "Info", "Result: 42");
//							}
//						}
//					}
//					trd = null;
//				}
				if(od != null) {
					od.setVisible();
				} else {
					od = new OcrDialog(mw.getShell());
					int ret = od.open();
					
					if (ret == IDialogConstants.OK_ID) {
						final String pageStr = od.getPages();
						final OcrConfig config = od.getConfig();
						logger.info("starting ocr for doc "+docId+", pages " + pageStr + " and col "+colId);
						jobId = store.runOcr(colId, docId, pageStr, config);
					}
					od = null;
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
			} else if(s==tw.htrTrainBtn) {
				if(!store.isAdminLoggedIn()) {
					DialogUtil.showInfoMessageBox(mw.getShell(), "Not Available", "HTR Training is currently under development and only available to Admins.\n"
							+ "In case you want to request a data set to be trained, please contact us at email@transkribus.eu.");
				} else {
					if(htd != null) {
						htd.setVisible();
					} else {
						htd = new HtrTrainingDialog(mw.getShell());
						if(htd.open() == IDialogConstants.OK_ID) {
							CitLabHtrTrainConfig config = htd.getConfig();
							jobId = store.runHtrTraining(config);
						}
						htd = null;
					}
				}
			} else if(s==tw.recogBtn) {
				if(trd2 != null) {
					logger.debug("htr diag set visible");
					trd2.setVisible();
				} else {
					trd2 = new TextRecognitionDialog2(mw.getShell());
					if(trd2.open() == IDialogConstants.OK_ID) {
						TextRecognitionConfig config = trd2.getConfig();
						final String pages = trd2.getPages();
						try {
							jobId = store.runHtr(pages, config);
						} finally {
							trd2 = null;
						}
					}
					trd2 = null;
				}
			}
//			else {
//				mw.onError("Error", "Unknown event!", null);
//				return;
//			}
			
			if (jobId != null) { // single job started
				logger.debug("started job with id = "+jobId);
							
				mw.registerJobToUpdate(jobId);
				
				store.sendJobListUpdateEvent();
				mw.updatePageLock();
				
				DialogUtil.showInfoMessageBox(tw.getShell(), "Job started", "Started job with id = "+jobId);
			} else if (jobIds != null) { // multiple jobs started
				logger.debug("started jobs: "+jobIds.size());
				String jobIdsStr = mw.registerJobsToUpdate(jobIds);				
				store.sendJobListUpdateEvent();
				mw.updatePageLock();
				
				DialogUtil.showInfoMessageBox(tw.getShell(), jobIds.size()+ " jobs started", jobIds.size()+ " jobs started\nIDs:\n "+jobIdsStr);
			}
		} catch (ClientErrorException cee) {
//			final int status = cee.getResponse().getStatus();
//			if(status == 400) {
//				DialogUtil.showErrorMessageBox(this.mw.getShell(), "Error", 
//						"A job of this type already exists for this page/document!");
//			} 
//			else {
//				mw.onError("Error", cee.getMessage(), cee);
//			}
			
			mw.onError("Error", cee.getMessage(), cee);
		} catch (Exception ex) {
			mw.onError("Error", ex.getMessage(), ex);
		} finally {
			laDiag = null;
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
