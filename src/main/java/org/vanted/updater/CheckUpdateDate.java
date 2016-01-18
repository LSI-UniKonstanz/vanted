package org.vanted.updater;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CheckUpdateDate {

	public static boolean isDateAfterUpdateDate(Date currentDate, Preferences preferenceForClass) {
		
		boolean timeout = false;
		String strReminderDate = preferenceForClass.get(ScanForUpdate.REMINDER_DATE, null);
		if (strReminderDate != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			try {
				Date storedDate = dateFormat.parse(strReminderDate);
				
				if (currentDate.after(storedDate))
					timeout = true;
			} catch (ParseException e) {
				if (Logger.getRootLogger().getLevel() == Level.DEBUG)
					e.printStackTrace();
				
				/*
				 * if someone put in a wrong formatted dat in the preferences
				 * replace it with the current date
				 */
				preferenceForClass.put(ScanForUpdate.REMINDER_DATE, dateFormat.format(currentDate));
				
				timeout = true;
			}
		}
		return timeout;
	}
}
