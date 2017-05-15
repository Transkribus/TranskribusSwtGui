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
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
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
import eu.transkribus.swt_gui.dialogs.OcrDialog;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog2;
import eu.transkribus.swt_gui.htr.HtrTrainingDialog;
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
	OcrDialog od;
	TextRecognitionDialog2 trd2;

	public ToolsWidgetListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.canvas = mainWidget.getCanvas();
		this.tw = mainWidget.getUi().getToolsWidget();
		
		addListener();
	}
	
	private void addListener() {
		// use utiliy method from SWTUtil class to avoid nullpointer exceptions!
//		SWTUtil.addSelectionListener(tw.batchLaBtn, this);
//		SWTUtil.addSelectionListener(tw.regAndLineSegBtn, this);
//		SWTUtil.addSelectionListener(tw.lineSegBtn, this);
//		SWTUtil.addSelectionListener(tw.wordSegBtn, this);
		
		SWTUtil.addSelectionListener(tw.trComp.getRunBtn(), this);
		SWTUtil.addSelectionListener(tw.trComp.getTrainBtn(), this);
		
		SWTUtil.addSelectionListener(tw.startLaBtn, this);
		
//		blockSegWPsBtn.addSelectionListener(this);
//		tw.baselineBtn.addSelectionListener(this);
		
//		SWTUtil.addSelectionListener(tw.ocrBtn, this);
//		SWTUtil.addSelectionListener(tw.htrTrainBtn, this);
//		SWTUtil.addSelectionListener(tw.recogBtn, this);
		
		SWTUtil.addSelectionListener(tw.computeWerBtn, this);
		SWTUtil.addSelectionListener(tw.compareVersionsBtn, this);
		
		SWTUtil.addSelectionListener(tw.polygon2baselinesBtn, this);
				
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
		return s == tw.startLaBtn || s == tw.polygon2baselinesBtn;
//		return (s == tw.batchLaBtn || s == tw.regAndLineSegBtn || s == tw.lineSegBtn || s == tw.baselineBtn || s == tw.polygon2baselinesBtn);
	}
	
	boolean needsRegions(PcGtsType pageData, Object s) {
		if (PageXmlUtils.hasRegions(pageData)) {
			return false;
		}
		
		return (s == tw.startLaBtn && !tw.laComp.isDoBlockSeg() && tw.laComp.isDoLineSeg()) || s == tw.polygon2baselinesBtn;
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

		try {
			PcGtsType pageData = store.getTranscript().getPageData();
			List<String> jobIds=new ArrayList<>();
			
			int colId = store.getCurrentDocumentCollectionId();
			
			if (needsRegions(pageData, s)) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "You have to define text regions first!");
				return;
			}
			
			if (isLayoutAnalysis(s) && store.isTranscriptEdited()) {
				mw.saveTranscription(false);
			}

			if (s == tw.startLaBtn) {
				if (!tw.laComp.isCurrentTranscript()) {
					logger.debug("running la on pages: "+tw.laComp.getPages());
					jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(tw.laComp.getPages(),
							tw.laComp.isDoBlockSeg(), tw.laComp.isDoLineSeg(), tw.laComp.isDoWordSeg(), false, tw.laComp.getJobImpl(), null);
				} else {
					List<String> rids = getSelectedRegionIds();
					logger.debug("running la on current transcript and selected rids: "+CoreUtils.join(rids));
					jobIds = store.analyzeLayoutOnCurrentTranscript(rids,
							tw.laComp.isDoBlockSeg(), tw.laComp.isDoLineSeg(), tw.laComp.isDoWordSeg(), false, tw.laComp.getJobImpl(), null);	
				}
			}
			else if (s == tw.polygon2baselinesBtn) {
				if (!tw.otherToolsPagesSelector.isCurrentTranscript()) {
					logger.debug("polygon2baselinesBtn on pages: "+tw.otherToolsPagesSelector.getPagesStr());
					jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(tw.otherToolsPagesSelector.getPagesStr(),
							false, false, false, true, JobImpl.NcsrOldLaJob.toString(), null);
				} else {
					logger.debug("polygon2baselinesBtn on current transcript");
					List<String> rids = getSelectedRegionIds();
					
					jobIds = store.analyzeLayoutOnCurrentTranscript(rids,
							false, false, false, true, JobImpl.NcsrOldLaJob.toString(), null);
				}
			}
			
			// struct analysis:
//			else if (s == tw.polygon2baselinesBtn) {
//				mw.analyzePageStructure(tw.detectPageNumbers.getSelection(), tw.detectRunningTitles.getSelection(), tw.detectFootnotesCheck.getSelection());
//			}
			
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
			} 
			else if (tw.trComp.isHtr() && s == tw.trComp.getTrainBtn()) {
				if(htd != null) {
					htd.setVisible();
				} else {
					htd = new HtrTrainingDialog(mw.getShell());
					if(htd.open() == IDialogConstants.OK_ID) {
						CitLabHtrTrainConfig config = htd.getConfig();
						String jobId = store.runHtrTraining(config);
						jobIds.add(jobId);
					}
					htd = null;
				}
			}
			else if (tw.trComp.isHtr() && s == tw.trComp.getRunBtn()) {
				if(trd2 != null) {
					logger.debug("htr diag set visible");
					trd2.setVisible();
				} else {
					trd2 = new TextRecognitionDialog2(mw.getShell());
					if(trd2.open() == IDialogConstants.OK_ID) {
						TextRecognitionConfig config = trd2.getConfig();
						final String pages = trd2.getPages();
						try {
							String jobId = store.runHtr(pages, config);
							jobIds.add(jobId);
						} finally {
							trd2 = null;
						}
					}
					trd2 = null;
				}
			}
			else if (tw.trComp.isOcr() && s == tw.trComp.getRunBtn()) {
				if(od != null) {
					od.setVisible();
				} else {
					od = new OcrDialog(mw.getShell());
					int ret = od.open();
					
					if (ret == IDialogConstants.OK_ID) {
						final String pageStr = od.getPages();
						final OcrConfig config = od.getConfig();
						logger.info("starting ocr for doc "+store.getDocId()+", pages " + pageStr + " and col "+colId);
						String jobId = store.runOcr(colId, store.getDocId(), pageStr, config);
						jobIds.add(jobId);
					}
					od = null;
				}
			}
			
			if (!CoreUtils.isEmpty(jobIds)) {
				logger.debug("started "+jobIds.size()+" jobs");
				String jobIdsStr = mw.registerJobsToUpdate(jobIds);
				store.sendJobListUpdateEvent();
				mw.updatePageLock();
				
				String jobsStr = jobIds.size()>1 ? "jobs" : "job";
				DialogUtil.showInfoMessageBox(tw.getShell(), jobIds.size()+" "+jobsStr+" started!", "IDs:\n "+jobIdsStr);
			}
			
		} catch (ClientErrorException cee) {
			final int status = cee.getResponse().getStatus();
			if(status == 400) {
				DialogUtil.showErrorMessageBox(this.mw.getShell(), "Error", 
						"A job of this type already exists for this page/document!");
			} 
			else {
				mw.onError("Error", cee.getMessage(), cee);
			}
			
//			mw.onError("Error", cee.getMessage(), cee);
		} catch (Exception ex) {
			mw.onError("Error", ex.getMessage(), ex);
		} finally {
//			laDiag = null;
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
