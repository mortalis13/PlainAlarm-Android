package org.mortalis.plainalarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


public class AlarmReceiver extends WakefulBroadcastReceiver {
  
  public static final String ALARM_WAKEUP_INTENT = "ALARM_WAKEUP_INTENT"; 
  
  @Override
  public void onReceive(final Context context, Intent intent) {
    Log.d(Vars.APP_LOG_TAG, "AlarmReceiver.onReceive()");
    
    Fun.screenWakeup(context);
    
    Intent i = new Intent();
    i.setClass(context, MainActivity.class);
    int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY;
    i.setFlags(flags);
    i.putExtra(ALARM_WAKEUP_INTENT, true);
    context.startActivity(i);
  }
  
}
