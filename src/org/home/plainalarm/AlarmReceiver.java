package org.home.plainalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.os.PowerManager;
import android.content.Intent;

public class AlarmReceiver extends WakefulBroadcastReceiver {
  
  public static final String ALARM_WAKEUP_INTENT = "ALARM_WAKEUP_INTENT"; 
  
  @Override
  public void onReceive(final Context context, Intent intent) {
    Log.d(Vars.APP_LOG_TAG, "AlarmReceiver.onReceive()");
    
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    int wl_flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE;
    PowerManager.WakeLock wl = pm.newWakeLock(wl_flags, "AlarmReceiver");
    // wl.acquire(2000);
    wl.acquire();
    wl.release();
    
    Intent i = new Intent();
    i.setClass(context, MainActivity.class);
    // int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION;
    int flags = Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY;
    i.setFlags(flags);
    i.putExtra(ALARM_WAKEUP_INTENT, true);
    context.startActivity(i);
  }
  
}