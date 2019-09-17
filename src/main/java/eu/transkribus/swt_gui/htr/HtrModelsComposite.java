package eu.transkribus.swt_gui.htr;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.dialogs.CharSetViewerDialog;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.dialogs.DocImgViewerDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrModelsComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HtrModelsComposite.class);

	Storage store = Storage.getInstance();

	HtrTableWidget htw;
	
	HtrDetailsWidget hdw;
	DocImgViewerDialog trainDocViewer, testDocViewer = null;
	CharSetViewerDialog charSetViewer = null;

	TrpHtr selectedHtr;

	public HtrModelsComposite(Composite parent, final String providerFilter, int flags) {
		super(parent, flags);
		this.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setLayout(new GridLayout(2, false));

		htw = new HtrTableWidget(sashForm, SWT.BORDER, providerFilter);
		htw.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
					@Override
					public void run() {
						updateDetails(getSelectedHtr());
					}
				});
			}
		});

		htw.getProviderCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateHtrs(htw.getProviderComboValue());
			}
		});
		
		final Table t = htw.getTableViewer().getTable();

		Menu menu = new Menu(t);
		t.setMenu(menu);

		MenuItem shareItem = new MenuItem(menu, SWT.NONE);
		shareItem.setText("Share model...");
		shareItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChooseCollectionDialog ccd = new ChooseCollectionDialog(getShell());
				
				@SuppressWarnings("unused")
				int ret = ccd.open();
				TrpCollection col = ccd.getSelectedCollection();
				TrpHtr htr = htw.getSelectedHtr();

				//because admin can see all models and if he then wants to share it to the collection he is actually in it is forbidden
				if (store.getCollId() == col.getColId() && !store.getUser().isAdmin()) {
					DialogUtil.showInfoMessageBox(getShell(), "Info",
							"The selected HTR is already included in this collection.");
					return;
				}
				try {
					store.addHtrToCollection(htr, col);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.debug("Could not add HTR to collection!", e1);
					String errorMsg = "The selected HTR could not be added to this collection.";
					if(!StringUtils.isEmpty(e1.getMessage())) {
						errorMsg += "\n" + e1.getMessage();
					}
					DialogUtil.showErrorMessageBox(getShell(), "Error sharing HTR",
							errorMsg);
				}
				DialogUtil.showInfoMessageBox(getShell(), "Success", "The HTR was added to the selected collection.");
				super.widgetSelected(e);
			}
		});

		MenuItem delItem = new MenuItem(menu, SWT.NONE);
		delItem.setText("Remove model from collection");
		delItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TrpHtr htr = htw.getSelectedHtr();
				try {
					store.removeHtrFromCollection(htr);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.debug("Could not remove HTR from collection!", e1);
					DialogUtil.showErrorMessageBox(getShell(), "Error removing HTR",
							"The selected HTR could not be removed from this collection.");
				}
				super.widgetSelected(e);
			}
		});

		t.addListener(SWT.MenuDetect, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (t.getSelectionCount() <= 0) {
					event.doit = false;
				}
			}

		});

		Group detailGrp = new Group(sashForm, SWT.BORDER);
		detailGrp.setText("Details");
		detailGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		detailGrp.setLayout(new GridLayout(1, false));

		hdw = new HtrDetailsWidget(detailGrp, SWT.VERTICAL);
		hdw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hdw.setLayout(new GridLayout(2, false));

		sashForm.setWeights(new int[] { 60, 40 });
		// fix for missing tooltip in chart after resize. Still does not work always...
		this.getShell().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				logger.trace("Resizing...");
				if(getShell().getMaximized()) {
					logger.trace("To MAX!");
				}
				
				hdw.triggerChartUpdate();
			}
		});
		
		updateHtrs(htw.getProviderComboValue());
		
		hdw.getShowTrainSetBtn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(selectedHtr == null) {
					return;
				}
				if (trainDocViewer != null) {
					trainDocViewer.setVisible();
				} else {
					try {
						trainDocViewer = new DocImgViewerDialog(getShell(), "Train Set", store.getTrainSet(selectedHtr));
						trainDocViewer.open();
					} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException
							| NoConnectionException e1) {
						logger.error(e1.getMessage(), e);
					}

					trainDocViewer = null;
				}
				super.widgetSelected(e);
			}
		});
		
		hdw.getShowTestSetBtn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(selectedHtr == null) {
					return;
				}
				if (testDocViewer != null) {
					testDocViewer.setVisible();
				} else {
					try {
						testDocViewer = new DocImgViewerDialog(getShell(), "Test Set", store.getTestSet(selectedHtr));
						testDocViewer.open();
					} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException
							| NoConnectionException e1) {
						logger.error(e1.getMessage(), e);
					}

					testDocViewer = null;
				}
				super.widgetSelected(e);
			}
		});

		hdw.getShowCharSetBtn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(selectedHtr == null) {
					return;
				}
				if (charSetViewer != null) {
					charSetViewer.setVisible();
				} else {
					try {
						charSetViewer = new CharSetViewerDialog(getShell(), 
								"Character Set of Model: " + selectedHtr.getName(), 
								selectedHtr.getCharSetList());
						charSetViewer.open();
					} catch (ClientErrorException | IllegalArgumentException e1) {
						logger.error(e1.getMessage(), e);
					}

					charSetViewer = null;
				}
			}
		});
	}
	
	public HtrModelsComposite(Composite parent, int flags) {
		this(parent, null, flags);
	}
	
	public void setSelection(int htrId) {
		logger.trace("Setting selection to htrId = {}", htrId);
		htw.setSelection(htrId);
	}
	
	public TrpHtr getSelectedHtr() {
		return htw.getSelectedHtr();
	}
	
	void updateDetails(TrpHtr selectedHtr) {
		this.selectedHtr = selectedHtr;
		hdw.updateDetails(selectedHtr);
	}

	private void updateHtrs(final String providerFilter) {
		List<TrpHtr> uroHtrs = store.getHtrs(providerFilter);
		htw.refreshList(uroHtrs);
	}
}
