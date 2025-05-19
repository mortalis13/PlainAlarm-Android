package org.mortalis.plainalarm;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {
  
  public static final String ALARM_WAKEUP_INTENT = "ALARM_WAKEUP_INTENT"; 
  
  @Override
  public void onReceive(final Context context, Intent intent) {
    Fun.logd("AlarmReceiver.onReceive()");
    
    Fun.screenWakeup(context);
    
    Intent mainActivity = new Intent();
    mainActivity.setClass(context, MainActivity.class);
    mainActivity.putExtra(ALARM_WAKEUP_INTENT, true);
    
    int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                Intent.FLAG_ACTIVITY_NO_HISTORY;
    mainActivity.setFlags(flags);
    
    context.startActivity(mainActivity);
  }
  
}
