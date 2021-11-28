package org.mortalis.plainalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBootReceiver extends BroadcastReceiver {
  
  @Override
  public void onReceive(Context context, Intent intent) {
    Fun.logd("AlarmBootReceiver.onReceive()");
    MainService.startAlarm(context);
  }
  
}
