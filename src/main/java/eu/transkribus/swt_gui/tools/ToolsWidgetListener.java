package eu.transkribus.swt_gui.tools;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.net.ssl.ConfigurableSSLServerSocketFactory;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpErrorRateResult;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpP2PaLAModel;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ThumbnailManager;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.dialogs.CITlabAdvancedLaConfigDialog;
import eu.transkribus.swt_gui.dialogs.ErrorRateAdvancedDialog;
import eu.transkribus.swt_gui.dialogs.OcrDialog;
import eu.transkribus.swt_gui.dialogs.P2PaLAConfDialog;
import eu.transkribus.swt_gui.dialogs.P2PaLAConfDialog.P2PaLARecogConf;
import eu.transkribus.swt_gui.dialogs.SamplesCompareDialog;
import eu.transkribus.swt_gui.htr.HtrTextRecognitionDialog;
import eu.transkribus.swt_gui.htr.HtrTrainingDialog;
import eu.transkribus.swt_gui.la.Text2ImageSimplifiedDialog;
import eu.transkribus.swt_gui.la.Text2ImageSimplifiedConfComposite.Text2ImageConf;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.mainwidget.storage.Storage.StorageException;
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
	HtrTextRecognitionDialog trd2;

	public ToolsWidgetListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.canvas = mainWidget.getCanvas();
		this.tw = mainWidget.getUi().getToolsWidget();

		addListener();
	}

	private void addListener() {
		SWTUtil.addSelectionListener(tw.trComp.getRunBtn(), this);

		SWTUtil.onSelectionEvent(tw.trComp.getTrainBtn(), (e) -> {
			startHtrTrainingDialog();
		});

		SWTUtil.addSelectionListener(tw.startLaBtn, this);
		
		if(!ToolsWidget.IS_LEGACY_WER_GROUP) {
			SWTUtil.addSelectionListener(tw.computeWerBtn, this);
			SWTUtil.addSelectionListener(tw.computeAdvancedBtn, this);
		}
		
		SWTUtil.addSelectionListener(tw.compareVersionsBtn, this);
		SWTUtil.addSelectionListener(tw.compareSamplesBtn, this);
		SWTUtil.addSelectionListener(tw.polygon2baselinesBtn, this);
		SWTUtil.addSelectionListener(tw.baseline2PolygonBtn, this);
		SWTUtil.addSelectionListener(tw.p2palaBtn, this);
		SWTUtil.addSelectionListener(tw.t2iBtn, this);
		
		Storage.getInstance().addListener(new IStorageListener() {
			Storage store = Storage.getInstance();
			
			public void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
				tw.refVersionChooser.setToGT();
				tw.hypVersionChooser.setToCurrent();
			}
		});
	}

	List<String> getSelectedRegionIds() {
		List<String> rids = new ArrayList<>();
		for (ICanvasShape s : canvas.getScene().getSelectedAsNewArray()) {
			ITrpShapeType st = GuiUtil.getTrpShape(s);
			if (st == null || !(st instanceof TrpTextRegionType)) {
				continue;
			}
			rids.add(st.getId());
		}
		return rids;
	}

	boolean isLayoutAnalysis(Object s) {
		return s == tw.startLaBtn || s == tw.polygon2baselinesBtn || s == tw.baseline2PolygonBtn || s==tw.p2palaBtn || s==tw.t2iBtn;
		// return (s == tw.batchLaBtn || s == tw.regAndLineSegBtn || s == tw.lineSegBtn
		// || s == tw.baselineBtn || s == tw.polygon2baselinesBtn);
	}

	boolean needsRegions(PcGtsType pageData, Object s) {
		if (PageXmlUtils.hasRegions(pageData)) {
			return false;
		}

		return (s == tw.startLaBtn && !tw.laComp.isDoBlockSeg() && tw.laComp.isDoLineSeg())
				|| s == tw.polygon2baselinesBtn || s == tw.baseline2PolygonBtn;
	}

	private void startHtrTrainingDialog() {
		try {
			store.checkLoggedIn();

			if (htd != null) {
				htd.setVisible();
			} else {
				htd = new HtrTrainingDialog(mw.getShell(), Storage.getInstance().getHtrTrainingJobImpls());
				if (htd.open() == IDialogConstants.OK_ID) {
					// new: check here if user wants to store or not
					// if (!mw.saveTranscriptDialogOrAutosave()) {
					// //if user canceled this
					// return;
					// }
					String jobId = null;
					if (htd.getCitlabTrainConfig() != null) {
						CitLabHtrTrainConfig config = htd.getCitlabTrainConfig();
						jobId = store.runHtrTraining(config);
						showSuccessMessage(jobId);
					} else if (htd.getCitlabT2IConfig() != null) {
						CitLabSemiSupervisedHtrTrainConfig config = htd.getCitlabT2IConfig();
						jobId = store.runCitLabText2Image(config);
						showSuccessMessage(jobId);
					}
				}
				htd = null;
			}
		} catch (StorageException e) {
			DialogUtil.showErrorMessageBox(mw.getShell(), "Error", e.getMessage());
		} catch (Exception e) {
			mw.onError("Error while starting training job: " + e.getMessage(), e.getMessage(), e);
		}
	}

	private void showSuccessMessage(List<String> jobIds) {
		showSuccessMessage(jobIds.toArray(new String[0]));
	}

	private void showSuccessMessage(String... jobIds) {
		if (!CoreUtils.isEmpty(jobIds)) {
			logger.debug("started " + jobIds.length + " jobs");
			String jobIdsStr = mw.registerJobsToUpdate(jobIds);
			store.sendJobListUpdateEvent();
			mw.updatePageLock();

			String jobsStr = jobIds.length > 1 ? "jobs" : "job";
			DialogUtil.showInfoMessageBox(tw.getShell(), jobIds.length + " " + jobsStr + " started!",
					"IDs:\n " + jobIdsStr);
		}
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
			DialogUtil.showErrorMessageBox(mw.getShell(), "Not available",
					"The tools are only available for remote documents!");
			return;
		}

		try {
			PcGtsType pageData = store.getTranscript().getPageData();
			List<String> jobIds = new ArrayList<>();

			int colId = store.getCurrentDocumentCollectionId();

			if (needsRegions(pageData, s)) {
				DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "You have to define text regions first!");
				return;
			}

			if (isLayoutAnalysis(s) && store.isTranscriptEdited()) {
				mw.saveTranscription(false);
			}

			// new: check here if user wants to store or not: e.g layout corrected and HTR
			// started but not saved before
			if (!mw.saveTranscriptDialogOrAutosave()) {
				// if user canceled this
				return;
			}

			if (s == tw.startLaBtn) {
				logger.debug("PARAMETERS = " + tw.laComp.getParameters());
				String pageStr = (!tw.laComp.isCurrentTranscript() ? tw.laComp.getPages() : Integer.toString(store.getPage().getPageNr()));
				String msg = "Do you really want to start the LA for page(s) " + pageStr + "  ?";
				
				String configInfoStr = null;
				//get information on config for configurable methods
				if(JobImpl.CITlabAdvancedLaJob.equals(tw.laComp.getJobImpl())) {
					configInfoStr = new CITlabAdvancedLaConfigDialog(mw.getShell(), tw.laComp.getParameters()).getConfigInfoString();
				}
				
				if(configInfoStr != null) {
					msg += "\n\nSettings:\n" + configInfoStr;
				}
				
				if (DialogUtil.showYesNoDialog(mw.getShell(), "Layout recognition", msg)!=SWT.YES) {
					return;
				}

				if (!tw.laComp.isCurrentTranscript()) {
					logger.debug("running la on pages: " + tw.laComp.getPages());
					jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(tw.laComp.getPages(),
							tw.laComp.isDoBlockSeg(), tw.laComp.isDoLineSeg(), tw.laComp.isDoWordSeg(), false, false,
							tw.laComp.getJobImpl().toString(), tw.laComp.getParameters());
				} else {
					List<String> rids = getSelectedRegionIds();
					logger.debug("running la on current transcript and selected rids: " + CoreUtils.join(rids));
					jobIds = store.analyzeLayoutOnCurrentTranscript(rids, tw.laComp.isDoBlockSeg(),
							tw.laComp.isDoLineSeg(), tw.laComp.isDoWordSeg(), false, false,
							tw.laComp.getJobImpl().toString(), tw.laComp.getParameters());
				}
			}
			else if (s == tw.polygon2baselinesBtn || s == tw.baseline2PolygonBtn) {
				boolean isPolygon2Baseline = s == tw.polygon2baselinesBtn;
				String jobImpl = isPolygon2Baseline ? JobImpl.NcsrOldLaJob.toString() : JobImpl.UpvlcLaJob.toString();
				String btnName = isPolygon2Baseline ? "polygon2baselinesBtn" : "baseline2PolygonBtn";

				if (!tw.otherToolsPagesSelector.isCurrentTranscript()) {
					logger.debug(btnName + " on pages: " + tw.otherToolsPagesSelector.getPagesStr());
					jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(tw.otherToolsPagesSelector.getPagesStr(),
							false, false, false, isPolygon2Baseline, !isPolygon2Baseline, jobImpl, null);
				} else {
					logger.debug(btnName + " on current transcript");
					List<String> rids = getSelectedRegionIds();

					jobIds = store.analyzeLayoutOnCurrentTranscript(rids, false, false, false, isPolygon2Baseline,
							!isPolygon2Baseline, jobImpl, null);
				}
			}
			else if (s == tw.p2palaBtn) {
				P2PaLAConfDialog diag = new P2PaLAConfDialog(tw.getShell(), Storage.getInstance().getP2PaLAModels());
				if (diag.open()==IDialogConstants.OK_ID) {
					P2PaLARecogConf conf = diag.getConf();
					if (conf != null) {
						String jobImpl = JobImpl.P2PaLAJob.toString();
						TrpP2PaLAModel model = conf.model;
						if (model == null) {
							DialogUtil.showErrorMessageBox(tw.getShell(), "No model selected", "Please select a P2PaLA model");
							return;
						}
						logger.debug("Selected P2PaLA model: "+model);
						ParameterMap pm = new ParameterMap();
						pm.addIntParam(JobConst.PROP_MODEL_ID, model.getId());
						pm.addParameter(JobConst.PROP_MODELNAME, model.getName());
						
						if (!conf.currentTranscript) {
							logger.debug("p2palaBtn on pages: " + tw.otherToolsPagesSelector.getPagesStr());
							jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(conf.pagesStr,
									true, true, false, false, false, jobImpl, pm);
						} else {
							logger.debug("p2palaBtn on current transcript");
//							List<String> rids = getSelectedRegionIds();
							jobIds = store.analyzeLayoutOnCurrentTranscript(null, true, true, false, false, false, jobImpl, pm);
						}
					}
					else {
						DialogUtil.showErrorMessageBox(tw.getShell(), "No configuration", "Please select a P2PaLA model");
						return;
					}
				}
				//// -------------- old code:
//				String jobImpl = JobImpl.P2PaLAJob.toString();
//				TrpP2PaLAModel model = tw.getSelectedP2PaLAModel();
//				if (model == null) {
//					DialogUtil.showErrorMessageBox(tw.getShell(), "No model selected", "Please select a P2PaLA model");
//					return;
//				}
//				logger.debug("Selected P2PaLA model: "+model);
//				ParameterMap pm = new ParameterMap();
//				pm.addIntParam(JobConst.PROP_MODEL_ID, model.getId());
//				pm.addParameter(JobConst.PROP_MODELNAME, model.getName());
//				
//				if (!tw.otherToolsPagesSelector.isCurrentTranscript()) {
//					logger.debug("p2palaBtn on pages: " + tw.otherToolsPagesSelector.getPagesStr());
//					jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(tw.otherToolsPagesSelector.getPagesStr(),
//							true, true, false, false, false, jobImpl, pm);
//				} else {
//					logger.debug("p2palaBtn on current transcript");
////					List<String> rids = getSelectedRegionIds();
//					jobIds = store.analyzeLayoutOnCurrentTranscript(null, true, true, false, false, false, jobImpl, pm);
//				}
			}
			else if (s == tw.t2iBtn) {
				Text2ImageConf conf = (Text2ImageConf) tw.t2iBtn.getData();
				Text2ImageSimplifiedDialog diag = new Text2ImageSimplifiedDialog(tw.getShell(), conf);
				if (diag.open()==IDialogConstants.OK_ID) {
					conf = diag.getConfig();
					logger.debug("setting t2i conf to: "+conf);
					tw.t2iBtn.setData(conf);
					
					// now run T2I:
					String jobImpl = JobImpl.T2IJob.toString();
					
//					Text2ImageConf conf = (Text2ImageConf) tw.t2iConfBtn.getData();
					logger.debug("starting t2i - conf = "+conf);
					
					TrpHtr htr = conf.model;
					if (htr == null) {
						DialogUtil.showErrorMessageBox(tw.getShell(), "No model selected", "Please select a base model for Text2Image");
						return;
					}
					ParameterMap pm = new ParameterMap();
					pm.addIntParam(JobConst.PROP_MODEL_ID, htr.getHtrId());
					pm.addBoolParam(JobConst.PROP_PERFORM_LAYOUT_ANALYSIS, conf.performLa);
					pm.addBoolParam(JobConst.PROP_REMOVE_LINE_BREAKS, conf.removeLineBreaks);
					pm.addDoubleParam(JobConst.PROP_THRESHOLD, conf.threshold);
					
					if (conf.skip_word!=null) {
						pm.addDoubleParam(JobConst.PROP_T2I_SKIP_WORD, conf.skip_word);
					}
					if (conf.skip_bl!=null) {
						pm.addDoubleParam(JobConst.PROP_T2I_SKIP_BASELINE, conf.skip_bl);
					}
					if (conf.jump_bl!=null) {
						logger.debug("setting jump_bl = "+conf.jump_bl);
						pm.addDoubleParam(JobConst.PROP_T2I_JUMP_BASELINE, conf.jump_bl);
					}
					if (conf.editStatus!=null) {
						pm.addParameter(JobConst.PROP_EDIT_STATUS, conf.editStatus.getStr());
					}
					
					if (!conf.currentTranscript) {
						logger.debug("t2i on pages: " + conf.pagesStr);
						jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(conf.pagesStr, true, true, false, false, false, jobImpl, pm);
					} else {
						logger.debug("t2i on current transcript");
//						List<String> rids = getSelectedRegionIds();
						jobIds = store.analyzeLayoutOnCurrentTranscript(null, true, true, false, false, false, jobImpl, pm);
					}
				}
				// OLD
//				String jobImpl = JobImpl.T2IJob.toString();
//				
//				Text2ImageConf conf = (Text2ImageConf) tw.t2iConfBtn.getData();
//				logger.debug("starting t2i - conf = "+conf);
//				
//				TrpHtr htr = conf.model;
//				if (htr == null) {
//					DialogUtil.showErrorMessageBox(tw.getShell(), "No model selected", "Please select a base model for Text2Image");
//					return;
//				}
//				ParameterMap pm = new ParameterMap();
//				pm.addIntParam(JobConst.PROP_MODEL_ID, htr.getHtrId());
//				pm.addBoolParam(JobConst.PROP_PERFORM_LAYOUT_ANALYSIS, conf.performLa);
//				pm.addBoolParam(JobConst.PROP_REMOVE_LINE_BREAKS, conf.removeLineBreaks);
//				pm.addDoubleParam(JobConst.PROP_THRESHOLD, conf.threshold);
//				
//				if (!tw.otherToolsPagesSelector.isCurrentTranscript()) {
//					logger.debug("t2i on pages: " + tw.otherToolsPagesSelector.getPagesStr());
//					jobIds = store.analyzeLayoutOnLatestTranscriptOfPages(tw.otherToolsPagesSelector.getPagesStr(),
//							true, true, false, false, false, jobImpl, pm);
//				} else {
//					logger.debug("t2i on current transcript");
////					List<String> rids = getSelectedRegionIds();
//					jobIds = store.analyzeLayoutOnCurrentTranscript(null, true, true, false, false, false, jobImpl, pm);
//				}
			}

			// struct analysis:
			// else if (s == tw.polygon2baselinesBtn) {
			// mw.analyzePageStructure(tw.detectPageNumbers.getSelection(),
			// tw.detectRunningTitles.getSelection(),
			// tw.detectFootnotesCheck.getSelection());
			// }

//			else if (s == tw.computeWerBtn) {
//
//				TrpTranscriptMetadata ref = (TrpTranscriptMetadata) tw.refVersionChooser.selectedMd;
//				TrpTranscriptMetadata hyp = (TrpTranscriptMetadata) tw.hypVersionChooser.selectedMd;
//
//				if (ref != null && hyp != null) {
//					
//					if(ToolsWidget.IS_LEGACY_WER_GROUP) {
//						logger.debug("Computing WER: " + ref.getKey() + " - " + hyp.getKey());
//						final String result = store.computeWer(ref, hyp);
//						MessageBox mb = new MessageBox(TrpMainWidget.getInstance().getShell(), SWT.ICON_INFORMATION | SWT.OK);
//						mb.setText("Result");
//						mb.setMessage(result);
//						mb.open();
//					} else {					
//						logger.debug("Computing WER: " + ref.getKey() + " - " + hyp.getKey());
//	
//						TrpErrorRateResult resultErr = store.computeErrorRate(ref, hyp);
//						logger.debug("resultError was calculated : "+resultErr.getCer());
//						ErrorRateDialog dialog = new ErrorRateDialog(mw.getShell(), resultErr);
//						dialog.open();
//
//					}
//				}
//				
//			}
			else if (s == tw.computeWerBtn) {

				TrpTranscriptMetadata ref = (TrpTranscriptMetadata) tw.refVersionChooser.selectedMd;
				TrpTranscriptMetadata hyp = (TrpTranscriptMetadata) tw.hypVersionChooser.selectedMd;

				if (ref != null && hyp != null) {
					
					logger.debug("Computing WER: " + ref.getKey() + " - " + hyp.getKey());
					TrpErrorRateResult result = store.computeErrorRate(ref, hyp);
					final String resultText = "Word Error Rate:\n"+ result.getWerDouble()+"\n Character Error Rate:\n"+result.getCerDouble();
					MessageBox mb = new MessageBox(TrpMainWidget.getInstance().getShell(), SWT.ICON_INFORMATION | SWT.OK);	
					mb.setText("Result");
					mb.setMessage(resultText);
					mb.open();
					
				}
				
			} else if (s == tw.computeAdvancedBtn) {
				
				ErrorRateAdvancedDialog dialog = new ErrorRateAdvancedDialog(mw.getShell());
				dialog.open();
				
				
			} else if (s == tw.compareSamplesBtn) {
				SamplesCompareDialog dialog = new SamplesCompareDialog(mw.getShell());
				dialog.open();
				
				
			}else if (s == tw.compareVersionsBtn) {
				
				String diffText = mw.getTextDifferenceOfVersions(false);
				mw.openVersionsCompareDialog(diffText);
				
			}
			// else if (tw.trComp.isHtr() && s == tw.trComp.getTrainBtn()) {
			// if(htd != null) {
			// htd.setVisible();
			// } else {
			// htd = new HtrTrainingDialog(mw.getShell());
			// if(htd.open() == IDialogConstants.OK_ID) {
			// CitLabHtrTrainConfig config = htd.getConfig();
			// String jobId = store.runHtrTraining(config);
			// jobIds.add(jobId);
			// }
			// htd = null;
			// }
			// }
			else if (tw.trComp.isHtr() && s == tw.trComp.getRunBtn()) {
				if (trd2 != null) {
					logger.debug("htr diag set visible");
					trd2.setVisible();
				} else {
					trd2 = new HtrTextRecognitionDialog(mw.getShell());
					if (trd2.open() == IDialogConstants.OK_ID) {
												
						TextRecognitionConfig config = trd2.getConfig();
						final String pages = trd2.getPages();
						
						String msg = "Do you really want to start the HTR for page(s) " + pages + "  ?";
						if (DialogUtil.showYesNoDialog(mw.getShell(), "Handwritten Text Recognition", msg)!=SWT.YES) {
							trd2 = null;
							return;
						}
						
						try {
							String jobId = store.runHtr(pages, config);
							jobIds.add(jobId);
						} finally {
							trd2 = null;
						}
					}
					trd2 = null;
				}
			} else if (tw.trComp.isOcr() && s == tw.trComp.getRunBtn()) {
				if (od != null) {
					od.setVisible();
				} else {
					od = new OcrDialog(mw.getShell());
					int ret = od.open();

					if (ret == IDialogConstants.OK_ID) {
						final String pageStr = od.getPages();
						final OcrConfig config = od.getConfig();
						
						String msg = "Do you really want to start the OCR for page(s) " + pageStr + "  ?";
						if (DialogUtil.showYesNoDialog(mw.getShell(), "Optical Character Recognition", msg)!=SWT.YES) {
							od = null;
							return;
						}
						
						logger.info("starting ocr for doc " + store.getDocId() + ", pages " + pageStr + " and col "
								+ colId);
						String jobId = store.runOcr(colId, store.getDocId(), pageStr, config);
						jobIds.add(jobId);
					}
					od = null;
				}
			}

			showSuccessMessage(jobIds);

		} catch (TrpClientErrorException | TrpServerErrorException ee) {
			final int status = ee.getResponse().getStatus();
			if (status == 400) {
				logger.error(ee.getMessage(), ee);
				DialogUtil.showErrorMessageBox(this.mw.getShell(), "Error", ee.getMessageToUser());
			} else {
				mw.onError("Error", ee.getMessageToUser(), ee);
			}
		} catch (ClientErrorException cee) {
			final int status = cee.getResponse().getStatus();
			if (status == 400) {
				DialogUtil.showErrorMessageBox(this.mw.getShell(), "Error",
						"A job of this type already exists for this page/document!");
			} else {
				mw.onError("Error", cee.getMessage(), cee);
			}

			// mw.onError("Error", cee.getMessage(), cee);
		} catch (Exception ex) {
			mw.onError("Error", ex.getMessage(), ex);
		} finally {
			// laDiag = null;
		}
		return;
	}

	// private void setButtonsEnabled(boolean isEnabled){
	// lw.getBlocksBtn().setEnabled(isEnabled);
	// lw.getBlocksInPsBtn().setEnabled(isEnabled);
	// lw.getLinesBtn().setEnabled(isEnabled);
	// }

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	// @Override public void keyTraversed(TraverseEvent e) {
	// Object s = e.getSource();
	// if (s == tw.languageCombo && e.detail == SWT.TRAVERSE_RETURN) {
	// logger.debug("enter pressed on language field!");
	//// mw.saveDocMetadata();
	// }
	// }
}
