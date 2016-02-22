/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

import java.util.Calendar;
import java.util.Date;

/**
 * @deprecated DOES NOT COMPILE IN WINDOWS!!!!!!!!!!!!!
 */
@Deprecated
public class TrpDateTime extends DateTime {
	/* TRP STUFF */
	
//	static final int MIN_YEAR = 0; // TRP needs earlier years than 1752. Just
//									// use Greg. Cal. for now...
//
//	public void setDate(final Calendar cal) {
//		final int day = cal.get(Calendar.DAY_OF_MONTH);
//		final int month = cal.get(Calendar.MONTH);
//		final int year = cal.get(Calendar.YEAR);
//		setDate(year, month, day);
//	}
//
//	public void setDate(final long timeMillis) {
//		final Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(timeMillis);
//		setDate(cal);
//	}
//
//	public Date getTime() {
//		return calendar.getTime();
//	}
//
//	protected void checkSubclass() {
//	}
//
//	// the following two methods use MIN_YEAR and thus need to get overwritten:
//	boolean isValidDate(int year, int month, int day) {
//		if (year < MIN_YEAR || year > MAX_YEAR)
//			return false;
//		Calendar valid = Calendar.getInstance();
//		valid.set(year, month, day);
//		return valid.get(Calendar.YEAR) == year && valid.get(Calendar.MONTH) == month && valid.get(Calendar.DAY_OF_MONTH) == day;
//	}
//
//	void setTextField(int fieldName, int value, boolean commit, boolean adjust) {
//		if (commit) {
//			int max = calendar.getActualMaximum(fieldName);
//			int min = calendar.getActualMinimum(fieldName);
//			if (fieldName == Calendar.YEAR) {
//				max = MAX_YEAR;
//				min = MIN_YEAR;
//				/*
//				 * Special case: convert 1 or 2-digit years into reasonable
//				 * 4-digit years.
//				 */
//				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//				int currentCentury = (currentYear / 100) * 100;
//				if (value < (currentYear + 30) % 100)
//					value += currentCentury;
//				else if (value < 100)
//					value += currentCentury - 100;
//			}
//			if (value > max)
//				value = min; // wrap
//			if (value < min)
//				value = max; // wrap
//		}
//		int start = fieldIndices[currentField].x;
//		int end = fieldIndices[currentField].y;
//		text.setSelection(start, end);
//		String newValue = formattedStringValue(fieldName, value, adjust);
//		StringBuffer buffer = new StringBuffer(newValue);
//		/* Convert leading 0's into spaces. */
//		int prependCount = end - start - buffer.length();
//		for (int i = 0; i < prependCount; i++) {
//			switch (fieldName) {
//			case Calendar.MINUTE:
//			case Calendar.SECOND:
//				buffer.insert(0, 0);
//				break;
//			default:
//				buffer.insert(0, ' ');
//				break;
//			}
//		}
//		newValue = buffer.toString();
//		ignoreVerify = true;
//		text.insert(newValue);
//		ignoreVerify = false;
//		selectField(currentField);
//		if (commit)
//			setField(fieldName, value);
//	}

	/* TRP STUFF END */

	public TrpDateTime(Composite parent, int style) {
		super(parent, style);
	}
}