package eu.transkribus.swt_canvas.util;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class SCSimpleDateTimeWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(SCSimpleDateTimeWidget.class);
	
	Combo day, month;
	StyledText year;

	public SCSimpleDateTimeWidget(Composite parent, int style) {
		super(parent, style);
		
		
		init();
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		day.setEnabled(enabled);
		month.setEnabled(enabled);
		year.setEnabled(enabled);
	}
	
	void init() {
		this.setLayout(new FillLayout());
		
		day = new Combo(this, SWT.READ_ONLY);
		for (int i=1; i<=31; ++i)
			day.add(""+i);
		day.select(0);
		day.pack();
		
//		Label l1 = new Label(this, SWT.NONE);
//		l1.setText("-");
//		l1.pack();
		
		month = new Combo(this, SWT.READ_ONLY);
		for (int i=1; i<=12; ++i)
			month.add(""+i);
		month.select(0);
		month.pack();
		
//		Label l2 = new Label(this, SWT.NONE);
//		l2.setText("-");
//		l2.pack();
		
		
//		logger.debug("initing year");
//		List<String> yearList = new ArrayList<>(10000);
		
		year = new StyledText(this, SWT.SINGLE | SWT.CENTER | SWT.LEFT | SWT.BORDER);
		year.addVerifyListener(new VerifyListener() {
			
			@Override public void verifyText(VerifyEvent e) {
				String newText = year.getText().substring(0, e.start)+e.text+year.getText().substring(e.end);
				
				int year=0;
				try {
					year = Integer.parseInt(newText);
				} catch (NumberFormatException ex) {
					e.doit = false;
					return;
				}
				
				if (year < 0 || year > 9999)
					e.doit = false;	
			}
		});
		year.setText("2000");
//		year.setLineAlignment(0, 1, SWT.CENTER);
		
//		year.addVerifyListener(new VerifyListener() {
//			@Override public void verifyText(VerifyEvent e) {
//			}
//		});
		
//		year = new Combo(this, SWT.READ_ONLY);
//		for (int i=0; i<=9999; ++i)
//			yearList.add(""+i);
//		year.setItems(yearList.toArray(new String[yearList.size()]));
		
//		logger.debug("done!!!!");
		
		this.pack();
	}
	
	public int getDay() {
		try {
			int d = Integer.parseInt(day.getText());
			if (d < 1 || d > 31)
				return 1;
			else
				return d;
		} catch (NumberFormatException e) {
			return 1;
		}
	}
	
	public int getMonth() {
		try {
			int m = Integer.parseInt(month.getText());
			if (m < 1 || m > 12)
				return 1;
			else
				return m;
		} catch (NumberFormatException e) {
			return 1;
		}
	}
	
	public int getYear() {
		try {
			int y = Integer.parseInt(year.getText());
			if (y < -9999 || y > 9999)
				return 1;
			else
				return y;
		} catch (NumberFormatException e) {
			return 1;
		}
	}	
	
	public Date getDate() {
		Calendar cal = Calendar.getInstance();
		logger.debug("y = "+getYear()+" m = "+getMonth()+" d = "+getDay());
		cal.set(getYear(), getMonth()-1, getDay());
		
		return cal.getTime();
	}

	public void setDate(Date date) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		day.setText(""+cal.get(Calendar.DAY_OF_MONTH));
		month.setText(""+cal.get(Calendar.MONTH));
		year.setText(""+cal.get(Calendar.YEAR));
	}
	
	

}
