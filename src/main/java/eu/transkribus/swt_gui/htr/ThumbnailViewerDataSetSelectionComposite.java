package eu.transkribus.swt_gui.htr;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.ThumbnailWidgetVirtualMinimal;
import eu.transkribus.swt_gui.htr.treeviewer.TreeViewerDataSetSelectionSashForm;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * This composite for composing datasets was part of {@link HtrTrainingDialog} but was replaced with {@link TreeViewerDataSetSelectionSashForm}..
 * Code is just copied here but was not tested in this form! That's why it's marked as deprecated and visibility is reduced
 */
@Deprecated
class ThumbnailViewerDataSetSelectionComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(ThumbnailViewerDataSetSelectionComposite.class);
	private final static String TAB_NAME_PREFIX = "Document ";
	
	private List<TrpDocMetadata> docList;
	
	private Button addTrainDocBtn, addTestDocBtn;
	private CTabFolder docTabFolder, testDocTabFolder;

	private Button useTrainGtVersionChk, useTestGtVersionChk;
	
	// keep references of all ThumbnailWidgets for gathering selection results
	private List<ThumbnailWidgetVirtualMinimal> trainTwList, testTwList;
	
	private final Storage store;
	
	public ThumbnailViewerDataSetSelectionComposite(Composite parent, int style, List<TrpDocMetadata> docList) {
		super(parent, style);
		
		this.docList = docList;
		store = Storage.getInstance();
		trainTwList = new LinkedList<>();
		testTwList = new LinkedList<>();
		
		Composite docCont = new Composite(parent, SWT.BORDER);
		docCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docCont.setLayout(new GridLayout(1, false));

		SashForm docSash = new SashForm(docCont, SWT.HORIZONTAL);
		docSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docSash.setLayout(new GridLayout(2, false));

		Composite trainDocCont = new Composite(docSash, SWT.NONE);
		trainDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainDocCont.setLayout(new GridLayout(2, false));

		addTrainDocBtn = new Button(trainDocCont, SWT.PUSH);
		addTrainDocBtn.setText("Add Train Document");
		addTrainDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(docTabFolder, SWT.CLOSE);
				Composite docOverviewCont = createDocOverviewCont(trainTwList, useTrainGtVersionChk.getSelection(),
						docTabFolder, store.getDoc());
				item.setControl(docOverviewCont);

				renameTabs(null, docTabFolder);
			}
		});

		useTrainGtVersionChk = new Button(trainDocCont, SWT.CHECK);
		useTrainGtVersionChk.setText("Use Groundtruth versions");
		useTrainGtVersionChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ThumbnailWidgetVirtualMinimal tw : trainTwList) {
					tw.setUseGtVersions(useTrainGtVersionChk.getSelection());
				}
				super.widgetSelected(e);
			}
		});

		docTabFolder = new CTabFolder(trainDocCont, SWT.BORDER | SWT.FLAT);
		docTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		CTabItem item = new CTabItem(docTabFolder, SWT.NONE);
		item.setText(TAB_NAME_PREFIX + 1);

		Composite docOverviewCont = createDocOverviewCont(trainTwList, useTrainGtVersionChk.getSelection(),
				docTabFolder, store.getDoc());
		item.setControl(docOverviewCont);

		docTabFolder.setSelection(0);

		docTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				renameTabs((CTabItem) event.item, docTabFolder);
			}
		});

		Composite testDocCont = new Composite(docSash, SWT.NONE);
		testDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		testDocCont.setLayout(new GridLayout(2, false));

		addTestDocBtn = new Button(testDocCont, SWT.PUSH);
		addTestDocBtn.setText("Add Test Document");
		addTestDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(testDocTabFolder, SWT.CLOSE);
				Composite testDocOverviewCont = createDocOverviewCont(testTwList, useTestGtVersionChk.getSelection(),
						testDocTabFolder, store.getDoc());
				item.setControl(testDocOverviewCont);
				renameTabs(null, testDocTabFolder);
			}
		});

		useTestGtVersionChk = new Button(testDocCont, SWT.CHECK);
		useTestGtVersionChk.setText("Use Groundtruth versions");
		useTestGtVersionChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ThumbnailWidgetVirtualMinimal tw : testTwList) {
					tw.setUseGtVersions(useTestGtVersionChk.getSelection());
				}
				super.widgetSelected(e);
			}
		});

		testDocTabFolder = new CTabFolder(testDocCont, SWT.BORDER | SWT.FLAT);
		testDocTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		CTabItem testItem = new CTabItem(testDocTabFolder, SWT.NONE);
		testItem.setText(TAB_NAME_PREFIX + 1);

		Composite testDocOverviewCont = createDocOverviewCont(testTwList, useTestGtVersionChk.getSelection(),
				testDocTabFolder, store.getDoc());
		testItem.setControl(testDocOverviewCont);

		testDocTabFolder.setSelection(0);

		testDocTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				renameTabs((CTabItem) event.item, testDocTabFolder);
			}
		});

		docSash.setWeights(new int[] { 50, 50 });
		testDocCont.pack();
		trainDocCont.pack();

		docCont.pack();
	}
	
	private Composite createDocOverviewCont(List<ThumbnailWidgetVirtualMinimal> twList, boolean useGtVersions,
			CTabFolder parent, TrpDoc doc) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout(1, false));

		Combo docCombo = new Combo(c, SWT.READ_ONLY);
		docCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		String[] items = new String[docList.size()];
		int selIndex = 0;
		for (int i = 0; i < docList.size(); i++) {
			TrpDocMetadata d = docList.get(i);
			items[i] = d.getDocId() + " - " + d.getTitle();
			if (doc != null && doc.getId() == d.getDocId()) {
				selIndex = i;
			}
		}
		docCombo.setItems(items);
		docCombo.select(selIndex);

		final ThumbnailWidgetVirtualMinimal tw = new ThumbnailWidgetVirtualMinimal(c, true, SWT.NONE);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		if (doc != null) {
			tw.setDoc(doc, useGtVersions);
		}
		twList.add(tw);

		docCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int index = docCombo.getSelectionIndex();
				TrpDocMetadata d = store.getDocList().get(index);
				try {
					tw.setDoc(store.getRemoteDoc(store.getCollId(), d.getDocId(), -1), useGtVersions);
				} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e1) {
					logger.error("Could not load remote doc!", e1);
				}
			}
		});

		c.pack();
		return c;
	}
	
	private void renameTabs(CTabItem closedItem, CTabFolder folder) {
		CTabItem[] items = folder.getItems();
		int count = 1;
		for (CTabItem item : items) {
			if (closedItem != null && item.equals(closedItem)) {
				continue;
			}
			logger.debug("Setting text: " + TAB_NAME_PREFIX + count);
			item.setText(TAB_NAME_PREFIX + count);
			count++;
		}
	}
	
	private List<DocumentSelectionDescriptor> getSelectionFromThumbnailWidgetList(
			List<ThumbnailWidgetVirtualMinimal> twList) {

		List<DocumentSelectionDescriptor> list = new LinkedList<>();
		for (ThumbnailWidgetVirtualMinimal tw : twList) {
			DocumentSelectionDescriptor dsd = tw.getSelectionDescriptor();

			if (dsd != null) {
				list.add(dsd);
			}
		}
		return list;
	}
	
	public List<DocumentSelectionDescriptor> getTrainSetSelection() {
		return getSelectionFromThumbnailWidgetList(trainTwList);
	}
	
	public List<DocumentSelectionDescriptor> getTestSetSelection() {
		return getSelectionFromThumbnailWidgetList(testTwList);
	}
}
