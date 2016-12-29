package org.eclipse.swt.widgets;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;

import eu.transkribus.swt.util.SWTUtil;

/**
 * @deprecated TODO
 * @author sebastian
 *
 */
public class AdvancedCombo extends Composite {	
	Text selectedText;
	ListViewer lv;
	
	Shell listShell;
	
	LabelProvider lp;
	Object[] input;
	
	public AdvancedCombo(Composite parent, int style) {
//		this.setLayout(new FillLayout());
		
		selectedText = new Text(this, 0);
		selectedText.setText("no text selected...");
		selectedText.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Rectangle r = selectedText.getBounds();				
				listShell.setLocation(r.x, r.y + r.height);
				
				listShell.setVisible(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		listShell = new Shell(getShell(), SWT.NO_TRIM);
		lv = new ListViewer(listShell, style);
		lv.setContentProvider(ArrayContentProvider.getInstance());
		
		lv.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		lv.getList().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object sel = ((IStructuredSelection) lv.getSelection()).getFirstElement();
				if (sel != null) {
					String text = lp.getText(sel);
					selectedText.setText(text);
					
					listShell.setVisible(false);
				}			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		
		listShell.setVisible(false);
		
	}
	
	public void setInput(Object[] input, LabelProvider lp) {
		this.input = input;
		this.lp = lp;
		
		lv.setInput(input);
		lv.setLabelProvider(lp);
	}
	
	
	public static void main(String[] args) throws LoginException {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				getShell().setLayout(new FillLayout());
				getShell().setSize(600, 600);
				
				class Person {
					public String fn;
					public String ln;
					public Person(String fn, String ln) {
						this.fn = fn;
						this.ln = ln;
					}
				}
				
				Person[] elements = { new Person("Hans", "Huber"), new Person("Franz", "Xaver"), new Person("John", "Doe") };
				
				AdvancedCombo ac = new AdvancedCombo(parent, 0);
				ac.setInput(elements, new LabelProvider() {
					@Override
					public String getText(Object arg0) {
						return ((Person)arg0).fn + " "+((Person) arg0).ln;
					}
				});

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
