package eu.transkribus.swt_gui.collection_manager;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PartInitException;
import org.junit.Assert;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpCrowdProject;
import eu.transkribus.core.model.beans.TrpCrowdProjectMessage;
import eu.transkribus.core.model.beans.TrpCrowdProjectMilestone;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.dialogs.BugDialog;
import eu.transkribus.swt_gui.dialogs.CommonExportDialog;
import eu.transkribus.swt_gui.dialogs.CrowdSourcingMessageDialog;
import eu.transkribus.swt_gui.dialogs.CrowdSourcingMilestoneDialog;
import eu.transkribus.swt_gui.dialogs.TextFieldDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.mainwidget.storage.StorageUtil;

public class CollectionEditorDialog extends Dialog {
	
	Text nameTxt, descrTxt, aimOfCrowdProjectTxt;
	Button isCrowdsourceBtn, isELearningBtn, createBtnMsg, editBtnMsg, createBtnMst, editBtnMst;
	Button deleteBtnMsg, deleteBtnMst, takeBtnMsg, takeBtnMst;
	Label landingLbl, tblLblMsg, tblLblMst, aimLbl;
	Link link;
	
	CrowdSourcingMessageDialog crowdMsgDiag;
	CrowdSourcingMilestoneDialog crowdMstDiag;
	Object dataEntered;
		
	Table tableMsg, tableMst;
	
	Storage storage = Storage.getInstance();
	
	List<Integer> mst2delete = new ArrayList<Integer>();
	List<Integer> msg2delete = new ArrayList<Integer>();
	
	List<TrpCrowdProjectMilestone> mst2edit = new ArrayList<TrpCrowdProjectMilestone>();
	List<TrpCrowdProjectMessage> msg2edit = new ArrayList<TrpCrowdProjectMessage>();

	private TrpCollection collection;
	
	private boolean mdChanged = false;
	private boolean crowdMdChanged = false;
	private boolean saveAsNew = false;
	
	private boolean editAllowed = false;

	public CollectionEditorDialog(Shell parentShell, TrpCollection c) {
		super(parentShell);
		Assert.assertNotNull("Collection cannot be null!", c);
		
		this.collection = c;
		
		TrpUserLogin user = storage.getUser();
		if (user.isAdmin() || StorageUtil.isOwnerOfCollection(collection)){
			editAllowed = true;
		}
	}
	
//	public void setVisible() {
//		if (super.getShell() != null && !super.getShell().isDisposed()) {
//			super.getShell().setVisible(true);
//		}
//	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(2, false));
		
		Label nameLbl = new Label(cont, SWT.NONE);
		nameLbl.setText("Name:");
		nameTxt = new Text(cont, SWT.BORDER);
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label descrLbl = new Label(cont, SWT.NONE);
		descrLbl.setText("Description:");
		descrTxt = new Text(cont, SWT.BORDER | SWT.MULTI);
		descrTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		landingLbl = new Label(cont, SWT.NONE);
		landingLbl.setText("Landing web page");
//		Text landingUrl = new Text(cont, SWT.READ_ONLY);
//		landingUrl.setText(url);
		
		String url = "https://transkribus.eu/crowd/"+collection.getColId();
		link = new Link(cont, SWT.NONE);
	    link.setText("<a href=\"https://transkribus.eu/crowd/"+collection.getColId()+"\">"+url+"</a>");
	    
	    link.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e) {
               //System.out.println("You have selected: "+e.text);
               //  Open default external browser 
               org.eclipse.swt.program.Program.launch(e.text);
            }
        });
	    
		aimLbl = new Label(cont, SWT.NONE);
		aimLbl.setText("Aim of this crowd project:");
		//aimLbl.setEnabled(collection.isCrowdsourcing());
		aimOfCrowdProjectTxt = new Text(cont, SWT.BORDER | SWT.MULTI);
		aimOfCrowdProjectTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		aimOfCrowdProjectTxt.setEnabled(collection.isCrowdsourcing());
		if (collection.getCrowdProject() != null && collection.getCrowdProject().getAim() != null){
			aimOfCrowdProjectTxt.setText(collection.getCrowdProject().getAim());
		}

	    // Action.
//	    Action add = new Action("New message") {
//	      public void run() {
//	          // Append.
//	    	  TableItem item = new TableItem(table, SWT.NULL);
//	    	  table.select(table.getItemCount() - 1);
//	      }
//	    };
//	    add.setImageDescriptor(ImageDescriptor.createFromImage(Images.ADD));
//	    add.setText("Add new message");

	    GridLayout gridLayout = new GridLayout();
	    Composite comp = new Composite(cont, SWT.FILL);
	    comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	    comp.setLayout(gridLayout);
	    
		tblLblMst = new Label(comp, SWT.NONE);
		tblLblMst.setText("Milestones:");
	    
	    tableMst = new Table(comp, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
	    tableMst.setLayoutData(new GridData(GridData.FILL_BOTH));

	    tableMst.setLinesVisible(collection.isCrowdsourcing());
	    tableMst.setHeaderVisible(collection.isCrowdsourcing());

	    TableColumn title = new TableColumn(tableMst, SWT.LEFT);
	    title.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    title.setText("Title");

	    TableColumn description = new TableColumn(tableMst, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
	    description.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    description.setText("Description");
	    
	    TableColumn date = new TableColumn(tableMst, SWT.TIME);
	    date.setData(new DateTime (tableMst, SWT.TIME));
	    date.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    date.setText("Due date");
	    
	    TableColumn creationDate = new TableColumn(tableMst, SWT.DROP_DOWN);
	    creationDate.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    creationDate.setText("Date Created");
	    
//	    TableColumn mstId = new TableColumn(tableMst, SWT.LEFT);
//	    mstId.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
//	    mstId.setText("ID");
	    	    
	    title.setWidth(100);
	    description.setWidth(350);
	    date.setWidth(140);
	    creationDate.setWidth(140);
	    
	    tableMst.pack();
	    
		Composite btnsMst = new Composite(comp, 0);
		btnsMst.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		btnsMst.setLayout(new GridLayout(4, true));
				
		createBtnMst = new Button(btnsMst, SWT.PUSH);
		createBtnMst.setText("New Milestone");
		createBtnMst.setImage(Images.ADD);
		createBtnMst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		editBtnMst = new Button(btnsMst, SWT.PUSH);
		editBtnMst.setText("Edit selected milestone");
		editBtnMst.setImage(Images.IMAGE_EDIT);
		editBtnMst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		takeBtnMst = new Button(btnsMst, SWT.PUSH);
		takeBtnMst.setText("Edit/Save As New");
		takeBtnMst.setImage(Images.IMAGE_EDIT);
		takeBtnMst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		deleteBtnMst = new Button(btnsMst, SWT.PUSH);
		deleteBtnMst.setText("Delete selected milestone");
		deleteBtnMst.setImage(Images.IMAGE_DELETE);
		deleteBtnMst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if (editAllowed){
			fillMilestoneTable();
		}
  	  	
		tblLblMsg = new Label(comp, SWT.NONE);
		tblLblMsg.setText("Messages:");

	    tableMsg = new Table(comp, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
	    tableMsg.setLayoutData(new GridData(GridData.FILL_BOTH));

	    tableMsg.setLinesVisible(collection.isCrowdsourcing());
	    tableMsg.setHeaderVisible(collection.isCrowdsourcing());

	    TableColumn subject = new TableColumn(tableMsg, SWT.LEFT);
	    subject.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    subject.setText("Subject");

	    TableColumn message = new TableColumn(tableMsg, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
	    message.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    message.setText("Message");
	    
	    TableColumn milestone = new TableColumn(tableMsg, SWT.NULL);
	    milestone.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    milestone.setText("Milestone");
	    
	    TableColumn created = new TableColumn(tableMsg, SWT.NULL);
	    created.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    created.setText("Date Created");
	    
	    TableColumn email = new TableColumn(tableMsg, SWT.NULL);
	    email.setData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	    email.setText("Email Sent");
	    
	    subject.setWidth(100);
	    message.setWidth(350);
	    milestone.setWidth(200);
	    created.setWidth(140);
	    
	    tableMsg.pack();
	    
	    if (editAllowed){
	    	fillMessageTable();
	    }
	           
  	  	//tableMsg.select(tableMsg.getItemCount() - 1);
  	  	
		Composite btns = new Composite(comp, 0);
		btns.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		btns.setLayout(new GridLayout(4, true));
				
		createBtnMsg = new Button(btns, SWT.PUSH);
		createBtnMsg.setText("Add Message");
		createBtnMsg.setImage(Images.ADD);
		createBtnMsg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		editBtnMsg = new Button(btns, SWT.PUSH);
		editBtnMsg.setText("Edit selected message");
		editBtnMsg.setImage(Images.IMAGE_EDIT);
		editBtnMsg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		takeBtnMsg = new Button(btns, SWT.PUSH);
		takeBtnMsg.setText("Edit/Save As New");
		takeBtnMsg.setImage(Images.IMAGE_EDIT);
		takeBtnMsg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		deleteBtnMsg = new Button(btns, SWT.PUSH);
		deleteBtnMsg.setText("Delete selected message");
		deleteBtnMsg.setImage(Images.IMAGE_DELETE);
		deleteBtnMsg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		SelectionAdapter btnsListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (e.getSource() == createBtnMsg) {
					createOrUpdateMessage(null);
					tableMsg.select(tableMsg.getItemCount() - 1);
				}
				else if(e.getSource() == editBtnMsg){
					if (tableMsg.getSelectionIndex() != -1){
						System.out.println(tableMsg.getSelectionIndex());
						TableItem item = tableMsg.getItem(tableMsg.getSelectionIndex());
						createOrUpdateMessage(item);
					}
				}
				else if(e.getSource() == takeBtnMsg){
					if (tableMsg.getSelectionIndex() != -1){
						TableItem item = tableMsg.getItem(tableMsg.getSelectionIndex());
						saveAsNew = true;
						createOrUpdateMessage(item);
					}
				}
				else if(e.getSource() == deleteBtnMsg){
					if (tableMsg.getSelectionIndex() != -1){
						int index = tableMsg.getSelectionIndex();
						TableItem item = tableMsg.getItem(index);
						int id = (int) item.getData("id");
						//try {
							//remember ids to remove?? so during cancel we do not delete them from db -> only by pressing OK
							msg2delete.add(id);
							crowdMdChanged = true;
							
							//storage.getConnection().deleteCrowdProjectMessage(collection.getColId(), id);
//						} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						TrpCrowdProjectMessage currMsg = collection.getCrowdProject().getCrowdProjectMessage(id);
					  	collection.getCrowdProject().getCrowdProjectMessages().remove(currMsg);
					  	tableMsg.remove(index);
					}
				}
				else if(e.getSource() == createBtnMst){
					createOrUpdateMilestone(null);
				}
				else if(e.getSource() == editBtnMst){
					if (tableMst.getSelectionIndex() != -1){
						//System.out.println(tableMst.getSelectionIndex());
						TableItem item = tableMst.getItem(tableMst.getSelectionIndex());
						createOrUpdateMilestone(item);
					}
				}
				else if(e.getSource() == takeBtnMst){
					if (tableMst.getSelectionIndex() != -1){
						TableItem item = tableMst.getItem(tableMst.getSelectionIndex());
						saveAsNew = true;
						createOrUpdateMilestone(item);
					}
				}
				else if(e.getSource() == deleteBtnMst){
					if (tableMst.getSelectionIndex() != -1){
						int index = tableMst.getSelectionIndex();
						TableItem item = tableMst.getItem(index);
						int id = (int) item.getData("id");
						
						//try {
							mst2delete.add(id);
							crowdMdChanged = true;
							//storage.getConnection().deleteCrowdProjectMilestone(collection.getColId(), id);
//						} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						TrpCrowdProjectMilestone currMst = collection.getCrowdProject().getCrowdProjectMilestone(id);
					  	collection.getCrowdProject().getCrowdProjectMilestones().remove(currMst);
					  	tableMst.remove(index);
					  	//cascade for messages
					  	int i = 0;
					  	for (TableItem ti : tableMsg.getItems()){
					  		if (ti.getData("mstId") == null){
					  			i++;
					  			continue;
					  		}
					  		else if (id == (int) ti.getData("mstId")){
								TrpCrowdProjectMessage currMsg = collection.getCrowdProject().getCrowdProjectMessage((int) ti.getData("id"));
							  	collection.getCrowdProject().getCrowdProjectMessages().remove(currMsg);
					  			tableMsg.remove(i);
					  		}
					  		else{
					  			i++;
					  		}
					  	}
					}
				}
			}
		};
		createBtnMsg.addSelectionListener(btnsListener);
		editBtnMsg.addSelectionListener(btnsListener);
		takeBtnMsg.addSelectionListener(btnsListener);
		deleteBtnMsg.addSelectionListener(btnsListener);
		
		createBtnMst.addSelectionListener(btnsListener);
		editBtnMst.addSelectionListener(btnsListener);
		takeBtnMst.addSelectionListener(btnsListener);
		deleteBtnMst.addSelectionListener(btnsListener);
  	  	
//	    ToolBar toolBar = new ToolBar(comp, SWT.RIGHT | SWT.FLAT);
//
//	    ToolBarManager manager = new ToolBarManager(toolBar);
//	    manager.add(add);
//	    manager.update(true);	  	
		
		Label attencione = new Label(cont, SWT.NONE);
		attencione.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 2, 1));
		attencione.setText("Attention: By clicking these options your collection will be shown to the public");
		attencione.setEnabled(editAllowed);
		
		isCrowdsourceBtn = new Button(cont, SWT.CHECK);
		isCrowdsourceBtn.setText("Crowdsourcing");
		isCrowdsourceBtn.setEnabled(editAllowed);
		isCrowdsourceBtn.addSelectionListener(new SelectionAdapter()
		{
		    @Override
		    public void widgetSelected(SelectionEvent e)
		    {
		        Button button = (Button) e.widget;
		        updateValues(button.getSelection());

		        //if crowd project is checked -> either there is already a project and needs to be loaded or we
		        //have to create a new one
		        if(button.getSelection()){
		        	try {
						TrpCrowdProject project = storage.loadCrowdProject(collection.getColId());
						collection.setCrowdProject(project);
					} catch (SessionExpiredException | ServerErrorException | ClientErrorException
							| NoConnectionException e1) {
						// TODO Auto-generated catch block
						//System.out.println("in catch block");
						e1.printStackTrace();
					}

		        	if (collection.getCrowdProject() == null){
		        		System.out.println("Create new crowd project");
		        		crowdMdChanged = true;
		        		collection.setCrowdProject(new TrpCrowdProject(collection.getColId()));
		        	}
		        	else{
		        		//System.out.println("reloading..");
		        		
		        		reloadCollectionEditorWidget();
		        	}
		        }
		        else{
		        	clearCollectionEditorWidget();
		        }
		    }



			private void reloadCollectionEditorWidget() {
				link.setText("<a href=\"https://transkribus.eu/crowd/"+collection.getColId()+"\">"+url+"</a>");
				if (collection.getCrowdProject().getAim() != null){
					aimOfCrowdProjectTxt.setText(collection.getCrowdProject().getAim());
				}
				fillMilestoneTable();
				fillMessageTable();
				
			}
		});
		
		new Label(cont, SWT.NONE);
		isELearningBtn = new Button(cont, SWT.CHECK);
		isELearningBtn.setText("eLearning");
		isELearningBtn.setEnabled(editAllowed);
		
		updateValues();
		
		return cont;
	}
	
	private void clearCollectionEditorWidget() {
		for (TableItem ti : tableMsg.getItems()){
			ti.dispose();
		}
		for (TableItem ti : tableMst.getItems()){
			ti.dispose();
		}
		tableMsg.clearAll();
		tableMst.clearAll();
		aimOfCrowdProjectTxt.setText("");
		link.setText("");
		
	}
	
	private void fillMilestoneTable() {
		if (collection.getCrowdProject() != null){
			for (TrpCrowdProjectMilestone mst : collection.getCrowdProject().getCrowdProjectMilestones()){
				if (mst.getProjectId() == null){
					continue;
				}
		  	  	TableItem newMst = new TableItem(tableMst, SWT.NULL);
		  	  	newMst.setText(new String[] {mst.getTitle(), mst.getDescription(), mst.getDueDate(), mst.getDate()});
		  	  	newMst.setData("id", mst.getMilestoneId());
			}
		}
	}

	private void fillMessageTable() {
		if (collection.getCrowdProject() != null){
			//System.out.println("load messages");
			for (TrpCrowdProjectMessage msg : collection.getCrowdProject().getCrowdProjectMessages()){
				if (msg.getProjectId() == null){
					continue;
				}
		  	  	TableItem newMsg = new TableItem(tableMsg, SWT.NULL);
		  	  	String emailInfo = "No";
		  	  	if (msg.isEmailSent()){
		  	  		emailInfo = "Yes";
		  	  	}
		  	  	String milestone = "No milestone assigned";
		  	  	if (msg.getMilestoneId() != null){
		  	  		milestone = collection.getCrowdProject().getCrowdProjectMilestone(msg.getMilestoneId()).toShortString();
		  	  	}
		  	  	newMsg.setText(new String[] {msg.getSubject(), msg.getMessage(), milestone, msg.getDate(), emailInfo});
		  	  	newMsg.setData("id", msg.getMessageId());
		  	  	newMsg.setData("mstId", msg.getMilestoneId());
			}
		}
	}
	
	public void createOrUpdateMessage(TableItem ti) {
		ArrayList<TrpCrowdProjectMilestone> milestones = collection.getCrowdProject().getCrowdProjectMilestones();
		String[] milestonesStrings = new String[milestones.size()];
		int i = 0;
		for (TableItem mst : tableMst.getItems()){
			milestonesStrings[i++] = mst.getText(0);
		}
				
		if(crowdMsgDiag == null && ti == null){
			crowdMsgDiag = new CrowdSourcingMessageDialog(getShell(), SWT.NONE, milestones);
			dataEntered = crowdMsgDiag.open();
		}else if(crowdMsgDiag == null && ti != null){
			//id contains the assigned milestoneor null if not assigned
			TrpCrowdProjectMilestone currMst = null;
			System.out.println(" milestone id = " + ti.getData("mstId"));
			if (ti.getData("mstId") != null){
				currMst = collection.getCrowdProject().getCrowdProjectMilestone((int)ti.getData("mstId"));
			}
			crowdMsgDiag = new CrowdSourcingMessageDialog(getShell(), SWT.NONE, ti.getText(0), ti.getText(1), currMst, milestones);
			dataEntered = crowdMsgDiag.open();
		}else{
			System.out.println("crowdMsgDiag not null - set active");
			crowdMsgDiag.setActive();
		}
						
		try {
			if (dataEntered == null){
				System.out.println("no data entered");
				crowdMsgDiag = null;
				return;
			}
			
		  	Integer mstId = null;
		  	String mstString = "No milestone";
	    	if (crowdMsgDiag.getSelectedMstId() != 0){
	    		TrpCrowdProjectMilestone assignedMst = collection.getCrowdProject().getCrowdProjectMilestone(crowdMsgDiag.getSelectedMstId());
	    		mstString = assignedMst.toShortString();
	    		mstId = assignedMst.getMilestoneId();
	    		System.out.println( " milstone ID for this message " + mstId);
	    	}

			if (ti == null || (ti != null && saveAsNew)){
				
			  	TableItem newItem = new TableItem(tableMsg, SWT.NULL);
			  	newItem.setData("mstId", mstId);
			  	newItem.setText(
			      new String[] {
			    	crowdMsgDiag.getSubject(),
			    	crowdMsgDiag.getMessage(),
			    	mstString,
			    	crowdMsgDiag.getDate()
			    	});
			    
			  	//tableMsg.select(tableMsg.getItemCount() - 1);
			  	TrpCrowdProjectMessage currMessage = new TrpCrowdProjectMessage(crowdMsgDiag.getSubject(), crowdMsgDiag.getMessage(), mstId, crowdMsgDiag.getDate());
			  	if (ti != null && saveAsNew){
			  		//to enable deletion of the message in case of cancel
			  		currMessage.setProjectId(null);
			  	}	  	
			  	int id = storage.storeCrowdProjectMessage(currMessage, collection.getColId());
			  	currMessage.setMessageId(id);
			  	currMessage.setProjectId(collection.getCrowdProject().getProjId());
			  	newItem.setData("id", id);
			  	collection.getCrowdProject().getCrowdProjectMessages().add(currMessage);
			  	saveAsNew = false;
			}
			else{
				ti.setText(0, crowdMsgDiag.getSubject());
				ti.setText(1, crowdMsgDiag.getMessage());
				ti.setText(2, mstString);
				ti.setText(3, crowdMsgDiag.getDate());
				ti.setData("mstId", mstId);
				
			  	TrpCrowdProjectMessage currMessage = collection.getCrowdProject().getCrowdProjectMessage((int)ti.getData("id"));
			  	currMessage.update(crowdMsgDiag.getSubject(), crowdMsgDiag.getMessage(), mstId, crowdMsgDiag.getDate());
			  	msg2edit.add(currMessage);
				//storage.storeCrowdProjectMessage(currMessage, collection.getColId());
			}
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			crowdMdChanged = true;
			crowdMsgDiag = null;
		}

	}
	
	public void createOrUpdateMilestone(TableItem ti) {

		if(crowdMstDiag == null && ti == null){
			crowdMstDiag = new CrowdSourcingMilestoneDialog(getShell(), SWT.NONE);
			dataEntered = crowdMstDiag.open();
		}else if(crowdMstDiag == null && ti != null){

			//collection.getCrowdProject().getCrowdProjectMilestone((int) ti.getData("id"));
			crowdMstDiag = new CrowdSourcingMilestoneDialog(getShell(), SWT.NONE, ti.getText(0), ti.getText(1), ti.getText(2));
			dataEntered = crowdMstDiag.open();
		}else{
			crowdMstDiag.setActive();
		}
						
		try {
			if (dataEntered == null){
				System.out.println("no data entered");
				crowdMstDiag = null;
				return;
			}
			else if (ti == null || (ti != null && saveAsNew) ){
				
				TrpCrowdProjectMilestone currMst = new TrpCrowdProjectMilestone();
				currMst.setTitle(crowdMstDiag.getSubject());
				currMst.setDescription(crowdMstDiag.getMessage());
				currMst.setDueDate(crowdMstDiag.getDueDate());
				currMst.setDate(crowdMstDiag.getDate());
				if (ti != null && saveAsNew){
					currMst.setProjectId(null);
				}
				int id = storage.storeCrowdProjectMilestone(currMst, collection.getColId());
				currMst.setMilestoneId(id);
				currMst.setProjectId(collection.getCrowdProject().getProjId());
				
				//System.out.println("new milestone id = " + id);
				
			  	collection.getCrowdProject().getCrowdProjectMilestones().add(currMst);

			  	TableItem newItem = new TableItem(tableMst, SWT.NULL);
			  	newItem.setData("id", id);
			  	newItem.setText(
			      new String[] {
			    		  crowdMstDiag.getSubject(),
			    		  crowdMstDiag.getMessage(),
			    		  crowdMstDiag.getDueDate(),
			    		  crowdMstDiag.getDate()
			    	});
			    
			  	//tableMst.select(tableMst.getItemCount() - 1);
			  	saveAsNew = false;
			}
			else{
				ti.setText(0, crowdMstDiag.getSubject());
				ti.setText(1, crowdMstDiag.getMessage());
				ti.setText(2, crowdMstDiag.getDueDate());
				ti.setText(3, crowdMstDiag.getDate());
				
				TrpCrowdProjectMilestone currMst = collection.getCrowdProject().getCrowdProjectMilestone((int)ti.getData("id"));
				currMst.setTitle(crowdMstDiag.getSubject());
				currMst.setDescription(crowdMstDiag.getMessage());
				currMst.setDueDate(crowdMstDiag.getDueDate());
				currMst.setDate(crowdMstDiag.getDate());
				
				//storage.storeCrowdProjectMilestone(currMst, collection.getColId());
				mst2edit.add(currMst);
			
			}
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException
				| NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			crowdMdChanged = true;
			crowdMstDiag = null;
		}
	}
	
	private void updateValues() {
		if (collection == null) {
			nameTxt.setText("");
			descrTxt.setText("");
			isCrowdsourceBtn.setSelection(false);
			isELearningBtn.setSelection(false);
			
			updateValues(false);

		} else {
			nameTxt.setText(collection.getColName());
			descrTxt.setText(collection.getDescription() == null ? "" : collection.getDescription());
			isCrowdsourceBtn.setSelection(collection.isCrowdsourcing());
			isELearningBtn.setSelection(collection.isElearning());
			
			updateValues(collection.isCrowdsourcing() && editAllowed);
		}

	}

	private void updateValues(boolean visibility) {
        landingLbl.setEnabled(visibility);
        link.setEnabled(visibility);
        tblLblMsg.setEnabled(visibility);
        tableMsg.setEnabled(visibility);
	    tableMsg.setLinesVisible(visibility);
	    tableMsg.setHeaderVisible(visibility);
        createBtnMsg.setEnabled(visibility);
        editBtnMsg.setEnabled(visibility);
        takeBtnMsg.setEnabled(visibility);
        deleteBtnMsg.setEnabled(visibility);
        tblLblMst.setEnabled(visibility);
        tableMst.setEnabled(visibility);
	    tableMst.setLinesVisible(visibility);
	    tableMst.setHeaderVisible(visibility);
        createBtnMst.setEnabled(visibility);
        editBtnMst.setEnabled(visibility);
        takeBtnMst.setEnabled(visibility);
        deleteBtnMst.setEnabled(visibility);
	    aimOfCrowdProjectTxt.setEnabled(visibility);
	    aimLbl.setEnabled(visibility);
		
	}

	public TrpCollection getCollection() {
		return collection;
	}
	
	public boolean isMdChanged() {
		return mdChanged;
	}
	
	public boolean isCrowdMdChanged() {
		return crowdMdChanged;
	}

	@Override
	protected void cancelPressed() {
		clearCollectionEditorWidget();
		super.cancelPressed();
	};
	
	@Override
	protected void okPressed() {
			
		final String name = nameTxt.getText();
		final String descr = descrTxt.getText();
		final String aim = aimOfCrowdProjectTxt.getText(); 
		final boolean isCrowdsource = isCrowdsourceBtn.getSelection();
		final boolean isELearning = isELearningBtn.getSelection();
		
		if (StringUtils.isEmpty(name)) {
			DialogUtil.showErrorMessageBox(this.getShell(), 
					"Invalid Input", 
					"Collection name must not be empty!");
			return;
		}
		
		//if crowdsourcing was checked but the collection was not for crowdsourcing yet
		if(!collection.isCrowdsourcing() && isCrowdsource) {
			int ret = DialogUtil.showYesNoDialog(this.getShell(), 
					"Collection was marked for crowdsourcing", 
					"You have marked the collection to be available for crowdsourcing.\n"
					+ "This will allow any user to subscribe to this collection, see its content "
					+ "and edit the contained documents!\nAre you sure you want to do this?");
			if(ret != SWT.YES) {
				return;
			}
		}
		
		//if crowdsourcing was checked but the collection was not for crowdsourcing yet
		if(!collection.isElearning() && isELearning) {
			int ret = DialogUtil.showYesNoDialog(this.getShell(), 
					"Collection was marked for eLearning", 
					"You have marked the collection to be available for eLearning.\n"
					+ "This will allow any user to subscribe to this collection and see its content.\n"
					+ "Are you sure you want to do this?");
			if(ret != SWT.YES) {
				return;
			}
		}
		
		mdChanged = !name.equals(collection.getColName()) 
				|| !descr.equals(collection.getDescription())
				|| isCrowdsource != collection.isCrowdsourcing()
				|| isELearning != collection.isElearning();
				
		if(mdChanged) {
			collection.setColName(name);
			collection.setDescription(descr);
			collection.setCrowdsourcing(isCrowdsource);
			collection.setElearning(isELearning);
		}
		
		//changes of crowdsourcing project are handled separatately
		if (collection.isCrowdsourcing() && collection.getCrowdProject() != null){
			//store a new crowd project
			TrpCrowdProject crowdProject = collection.getCrowdProject();
			if (!aim.equals(crowdProject.getAim())){
				crowdMdChanged = true;
				crowdProject.setAim(aim);
			}
			int projectId = crowdProject.getProjId();
			try {
				/*
				 * here we save milestones and messages durable by setting the projectId for them
				 * if Cancel is clicked they are deleted instead
				 */
				for (TrpCrowdProjectMilestone mst : crowdProject.getCrowdProjectMilestones()){
					//System.out.println("mst id " + mst.getMilestoneId());
					mst.setProjectId(projectId);
					storage.storeCrowdProjectMilestone(mst, collection.getColId());
				}
				for (TrpCrowdProjectMessage msg : crowdProject.getCrowdProjectMessages()){
					//System.out.println("msg id " + msg.getMessageId());
					msg.setProjectId(projectId);
					storage.storeCrowdProjectMessage(msg, collection.getColId());
				}
				/*
				 * delete the chosen messages durable when Ok was pressed
				 * Cancel keep them in the database and they get reloaded next time
				 */
				for (Integer id : mst2delete){
					storage.getConnection().deleteCrowdProjectMilestone(collection.getColId(), id);
				}
				for (Integer id : msg2delete){
					storage.getConnection().deleteCrowdProjectMessage(collection.getColId(), id);
				}
				for (TrpCrowdProjectMilestone currMst : mst2edit){
					storage.storeCrowdProjectMilestone(currMst, collection.getColId());
				}
				for (TrpCrowdProjectMessage currMsg : msg2edit){
					storage.storeCrowdProjectMessage(currMsg, collection.getColId());
				}
				

			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
					| NoConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			collection.setCrowdProject(crowdProject);
		}

		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit Collection Metadata");
		newShell.setMinimumSize(640, 480);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 680);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
	}
}
