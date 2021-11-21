package org.mortalis.plainalarm;

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
  public static int notif_current_id;
  
  public static void startAlarm(Context context) {
    Fun.logd("MainService.startAlarm()");
    
    boolean alarmStarted = Fun.getSharedPrefBool(context, Vars.PREF_KEY_ALARM_STARTED);
    long timeMillis = Fun.getSharedPrefLong(context, Vars.PREF_KEY_ALARM_TIME_MILLIS);
    String alarmTime = Fun.getSharedPref(context, Vars.PREF_KEY_ALARM_TEXT);
    
    if (alarmStarted && timeMillis != 0) {
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      Intent receiverIntent = new Intent(context, AlarmReceiver.class);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // >=API-21
        PendingIntent opIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(timeMillis, opIntent), pendingIntent);
      }
      else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        // >=API-19
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
      }
      else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
      }
      
      Fun.showNotification(context, Vars.NOTIFICATION_ID, true, "Alarm Set: " + alarmTime);
    }
  }
  
  public static void showNotification(String text) {
    if (context == null) return;
    Fun.showNotification(context, 1, false, text);
  }
  
  public static void showPlayerNotification(String text) {
    if (context == null) return;
    Fun.showPlayerNotification(context, ++notif_current_id, text);
  }
  
}
