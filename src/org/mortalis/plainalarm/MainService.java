package org.mortalis.plainalarm;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

@SuppressLint("NewApi")
public class MainService {
  
  public static Context context;
  private static int playerNotificationId = 1000;
  

  public static void startAlarm(long timeMillis) {
    Fun.logd("MainService.startAlarm()");
    
    // Resetting the notification, as in a sequence snooze + time change + wakeup + stop it stays in the fixed state
    Fun.cancelNotification(context, Vars.NOTIFICATION_ID);
    
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent receiverIntent = new Intent(context, AlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, PendingIntent.FLAG_IMMUTABLE);
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // >=API-21
      PendingIntent opIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
      alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(timeMillis, opIntent), pendingIntent);
    }
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // >=API-19
      alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
    }
    else {
      alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
    }
    
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_TIME_MILLIS, timeMillis);
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_STARTED, true);
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis);

    String alarmTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    Fun.logd(String.format("Alarm Time: %s = %d", alarmTime, timeMillis));
    
    Fun.showNotification(context, Vars.NOTIFICATION_ID, true, "Alarm Set: " + alarmTime);
    
    ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
    PackageManager pm = context.getPackageManager();

    pm.setComponentEnabledSetting(receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);
  }
  
  public static void stopAlarm() {
    Fun.logd("MainService.stopAlarm()");
    
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent receiverIntent = new Intent(context, AlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, PendingIntent.FLAG_IMMUTABLE);
    
    alarmManager.cancel(pendingIntent);
    
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_STARTED, false);
    Fun.cancelNotification(context, Vars.NOTIFICATION_ID);
  }
  
  
  public static void restoreAlarm(Context context) {
    Fun.logd("MainService.restoreAlarm()");
    
    boolean alarmStarted = Fun.getSharedPrefBool(context, Vars.PREF_KEY_ALARM_STARTED);
    long timeMillis = Fun.getSharedPrefLong(context, Vars.PREF_KEY_ALARM_TIME_MILLIS);
    String alarmTime = Fun.getSharedPref(context, Vars.PREF_KEY_ALARM_TEXT);
    
    if (alarmStarted && timeMillis != 0) {
      MainService.startAlarm(timeMillis);
    }
  }
  
  
  public static boolean isAlarmStarted() {
    return Fun.getSharedPrefBool(context, Vars.PREF_KEY_ALARM_STARTED);
  }
  
  public static void showPlayerNotification(String text) {
    if (context == null) return;
    Fun.showPlayerNotification(context, playerNotificationId++, text);
  }
  
}
