package eu.transkribus.swt_gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.ThumbnailManagerVirtual;
import eu.transkribus.swt.util.ThumbnailWidgetVirtualMinimal;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrTrainingDialog extends Dialog {

	private CTabFolder paramTabFolder;
	private CTabItem uroTabItem;

	private Button addDocBtn, addTestDocBtn;
	private CTabFolder docTabFolder, testDocTabFolder;
	
	Storage store = Storage.getInstance();

	public HtrTrainingDialog(Shell parent) {
		super(parent);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		SashForm sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(2, false));

		Composite paramCont = new Composite(sash, SWT.BORDER);
		paramCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramCont.setLayout(new GridLayout(4, false));

		Label modelNameLbl = new Label(paramCont, SWT.FLAT);
		modelNameLbl.setText("Model Name:");
		Text modelNameTxt = new Text(paramCont, SWT.BORDER);
		modelNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label langLbl = new Label(paramCont, SWT.FLAT);
		langLbl.setText("Language:");
		Text langTxt = new Text(paramCont, SWT.BORDER);
		langTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label descLbl = new Label(paramCont, SWT.FLAT);
		descLbl.setText("Description:");
		Text descTxt = new Text(paramCont, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 3;
//		gd.horizontalSpan = 3;
		descTxt.setLayoutData(gd);
		
		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		uroTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		uroTabItem.setText("CITlab");

		Composite uroParamCont = new Composite(paramTabFolder, SWT.NONE);
		uroParamCont.setLayout(new GridLayout(4, false));

		paramTabFolder.setSelection(uroTabItem);

		Label numEpochsLbl = new Label(uroParamCont, SWT.NONE);
		numEpochsLbl.setText("Nr. of Epochs:");
		Text numEpochsTxt = new Text(uroParamCont, SWT.BORDER);
		numEpochsTxt.setText("200");
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label learningRateLbl = new Label(uroParamCont, SWT.NONE);
		learningRateLbl.setText("Learning Rate:");
		Text learningRateTxt = new Text(uroParamCont, SWT.BORDER);
		learningRateTxt.setText("2e-3");
		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label noiseLbl = new Label(uroParamCont, SWT.NONE);
		noiseLbl.setText("Noise:");
		Combo noiseCmb = new Combo(uroParamCont, SWT.READ_ONLY);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		noiseCmb.setItems(new String[] {"no", "both"});
		
		Label trainSizeLbl = new Label(uroParamCont, SWT.NONE);
		trainSizeLbl.setText("Train Size per Epoch:");
		Text trainSizeTxt = new Text(uroParamCont, SWT.BORDER);
		trainSizeTxt.setText("1000");
		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label baseModelLbl = new Label(uroParamCont, SWT.NONE);
		baseModelLbl.setText("Base Model:");
		Combo baseModelCmb = new Combo(uroParamCont, SWT.READ_ONLY);
		baseModelCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		baseModelCmb.setItems(new String[] {"bla", "blubb"});
		
		
		uroTabItem.setControl(uroParamCont);

		// "200", //numEpochs
		// "2e-3", //;1e-3", //learningRate
		// "both", //noise
		// 1000, //TrainSizePerEpoch
		// null, //baseModel

		paramCont.pack();

		// doc selection ===========================================================================================

		Composite docCont = new Composite(sash, SWT.BORDER);
		docCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docCont.setLayout(new GridLayout(1, false));

		SashForm docSash = new SashForm(docCont, SWT.HORIZONTAL);
		docSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docSash.setLayout(new GridLayout(2, false));		
		
		
		Composite trainDocCont = new Composite(docSash, SWT.NONE);
		trainDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainDocCont.setLayout(new GridLayout(1, false));
		
		addDocBtn = new Button(trainDocCont, SWT.PUSH);
		addDocBtn.setText("Add Document");
		addDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(docTabFolder, SWT.CLOSE);
				item.setText("Document " + docTabFolder.getItemCount());
				Composite docOverviewCont = createDocOverviewCont(docTabFolder, store.getDoc());
				item.setControl(docOverviewCont); 
			}
		});

		docTabFolder = new CTabFolder(trainDocCont, SWT.BORDER | SWT.FLAT);
		docTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		CTabItem item = new CTabItem(docTabFolder, SWT.NONE);
		item.setText("Document 1");
		
		Composite docOverviewCont = createDocOverviewCont(docTabFolder, store.getDoc());
		item.setControl(docOverviewCont); 

		docTabFolder.setSelection(0);
		
		docTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				System.out.println("GONE");
			}
		});

		Composite testDocCont = new Composite(docSash, SWT.NONE);
		testDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		testDocCont.setLayout(new GridLayout(1, false));
		
		addTestDocBtn = new Button(testDocCont, SWT.PUSH);
		addTestDocBtn.setText("Add Test Document");
		addTestDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(testDocTabFolder, SWT.CLOSE);
				item.setText("Document " + testDocTabFolder.getItemCount());
				Composite testDocOverviewCont = createDocOverviewCont(testDocTabFolder, store.getDoc());
				item.setControl(testDocOverviewCont); 
			}
		});
		
		testDocTabFolder = new CTabFolder(testDocCont, SWT.BORDER | SWT.FLAT);
		testDocTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		CTabItem testItem = new CTabItem(testDocTabFolder, SWT.NONE);
		testItem.setText("Document 1");
		
		Composite testDocOverviewCont = createDocOverviewCont(testDocTabFolder, store.getDoc());
		testItem.setControl(testDocOverviewCont); 

		testDocTabFolder.setSelection(0);
		
		testDocTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				System.out.println("GONE");
			}
		});
		
		docSash.setWeights(new int[] {50, 50});
		testDocCont.pack();
		trainDocCont.pack();
		
		docCont.pack();

		sash.setWeights(new int[] { 34, 66 });
		return cont;
	}

	private Composite createDocOverviewCont(CTabFolder parent, TrpDoc doc) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout(1, false));
		
		Combo docCombo = new Combo(c, SWT.READ_ONLY);
		docCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		List<TrpDocMetadata> docList = store.getDocList();
		String[] items = new String[docList.size()];
		int selIndex = 0;
		for(int i = 0; i < docList.size(); i++) {
			TrpDocMetadata d = docList.get(i);
			items[i] = d.getDocId() + " - " + d.getTitle();
			if(doc != null && doc.getId() == d.getDocId()) {
				selIndex = i;
			}
		}
		docCombo.setItems(items);
		docCombo.select(selIndex);
		
		final ThumbnailWidgetVirtualMinimal tw = new ThumbnailWidgetVirtualMinimal(c, SWT.NONE);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if(doc != null) {
			tw.setDoc(doc);
		}
		
		docCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				final int index = docCombo.getSelectionIndex();
				TrpDocMetadata d = store.getDocList().get(index);
				try {
					tw.setDoc(store.getRemoteDoc(store.getCollId(), d.getDocId(), -1));
				} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		c.pack();
		return c;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("HTR Training");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}

}
