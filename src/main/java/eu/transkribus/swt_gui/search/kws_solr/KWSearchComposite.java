package eu.transkribus.swt_gui.search.kws_solr;

import java.net.URL;
import java.util.ArrayList;

import javax.ws.rs.client.InvocationCallback;

import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.TrpFimgStoreConf;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.beans.searchresult.KeywordSearchResult;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.fulltext.FullTextSearchComposite;

public class KWSearchComposite extends Composite{
	
	private final static Logger logger = LoggerFactory.getLogger(FullTextSearchComposite.class);
	FimgStoreGetClient imgStoreClient;
	Shell shell;
	Storage storage;
	Group facetsGroup;
	LabeledText inputText;
	
	String searchWord;
	KeywordSearchResult kwSearchResult;
	
	public KWSearchComposite(Composite parent, int style){
		super(parent, style);
		shell = parent.getShell();	
		try {
			imgStoreClient = new FimgStoreGetClient(new URL(TrpFimgStoreConf.getFimgStoreUrl()+"/"));
		} catch (Exception e) {
			logger.error("Could not create connection to FimgStore" + e);
			e.printStackTrace();
		}

		createContents();
		
	}
	
	private void createContents(){
		
		storage = Storage.getInstance();
		this.setLayout(new FillLayout());
		Composite c = new Composite(this, 0);
		c.setLayout(new FillLayout());
				
		SashForm sf = new SashForm(c, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));			
		
		facetsGroup = new Group(sf, SWT.NONE);
		facetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));	
		facetsGroup.setLayout(new GridLayout(2, false));
		facetsGroup.setText("Search HTR text for single words");
		
		TraverseListener findTagsOnEnterListener = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					findKW();
				}
			}
		};
		
		inputText = new LabeledText(facetsGroup, "Search for:");
		inputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		inputText.text.addTraverseListener(findTagsOnEnterListener);
		
	}
	
	private void findKW(){
		
		searchWord = inputText.getText().trim();
		if(searchWord.isEmpty()) {
			return;
		}
		
		storage = Storage.getInstance();
		
		
		//Async search
		InvocationCallback<KeywordSearchResult> callback = new InvocationCallback<KeywordSearchResult>() {

			@Override
			public void completed(KeywordSearchResult response) {
				kwSearchResult = response;
				if(kwSearchResult != null){	
					Display.getDefault().asyncExec(()->{
						logger.debug("searched"+searchWord);
						logger.debug("num hits: "+kwSearchResult.getNumResults());
					}); 
					
				}
				
			}

			@Override
			public void failed(Throwable throwable) {
				logger.error("Fulltext search failed."+ throwable);
				Display.getDefault().asyncExec(() -> {
					TrpMainWidget.getInstance().onError("Error searching keyword", throwable.getMessage(), throwable);
				});
			}
			
		};
		ArrayList<String> filters = new ArrayList<String>();
		String sorting = "childfield(probability) desc";
		
		try{
			storage.getConnection().searchKWAsync(searchWord, 0, 10, 0.0f, 1.0f, filters, sorting, 0, callback);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}

}
