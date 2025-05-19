package org.mortalis.plainalarm;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Random;
import java.io.FileOutputStream;

import org.apache.commons.io.FilenameUtils;

import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;


public class Fun {
  
  public static String storagePath;
  
  public static boolean fileExists(String filePath) {
    return new File(filePath).exists();
  }
  
  public static String getParentFolder(String filePath) {
    return new File(filePath).getParentFile().getAbsolutePath();
  }
  
  public static String getBaseFileName(String filePath) {
    return FilenameUtils.getBaseName(filePath);
  }
  
  public static String getFileExt(String filePath) {
    return FilenameUtils.getExtension(filePath);
  }
  
  public static List<String> getSoundFiles(String folderPath) {
    List<String> result = new ArrayList<>();
    
    File[] files = new File(folderPath).listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        if (!file.isDirectory() && file.canRead()) {
          String fileExt = FilenameUtils.getExtension(file.getName());
          for (String ext: Vars.AUDIO_EXTS) {
            if (fileExt.equals(ext)) return true;
          }
        }
        
        return false;
      }
    });
    
    for (File file: files) {
      result.add(file.getAbsolutePath());
    }
    
    return result;
  }
  
  
  public static int getRandomInt(int from, int to) {
    return from + new Random().nextInt(to - from + 1);
  }
  
  public static void saveSharedPref(Context context, String key, String value) {
    if (context == null) return;
    SharedPreferences sharedPreferences = context.getSharedPreferences(Vars.PREFS_FILE, 0);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(key, value);
    editor.commit();
  }
  
  public static void saveSharedPref(Context context, String key, long value) {
    if (context == null) return;
    SharedPreferences sharedPreferences = context.getSharedPreferences(Vars.PREFS_FILE, 0);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putLong(key, value);
    editor.commit();
  }
  
  public static void saveSharedPref(Context context, String key, boolean value) {
    if (context == null) return;
    SharedPreferences sharedPreferences = context.getSharedPreferences(Vars.PREFS_FILE, 0);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, value);
    editor.commit();
  }
  
  public static String getSharedPref(Context context, String key) {
    if (context == null) return null;
    SharedPreferences sharedPreferences = context.getSharedPreferences(Vars.PREFS_FILE, 0);
    return sharedPreferences.getString(key, null);
  }
  
  public static long getSharedPrefLong(Context context, String key) {
    if (context == null) return 0;
    SharedPreferences sharedPreferences = context.getSharedPreferences(Vars.PREFS_FILE, 0);
    return sharedPreferences.getLong(key, -1);
  }
  
  public static boolean getSharedPrefBool(Context context, String key) {
    if (context == null) return false;
    SharedPreferences sharedPreferences = context.getSharedPreferences(Vars.PREFS_FILE, 0);
    return sharedPreferences.getBoolean(key, false);
  }
  
  
  public static void toast(Context context, String msg) {
    if (context == null) return;
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
  }
  
  
  private static void log_file(String msg) {
    File dir = new File(storagePath);
    try {
      File f = new File(dir, "plainalarm_log.txt");
      FileOutputStream fout = new FileOutputStream(f, true);
      fout.write(new Date().toString().getBytes());
      fout.write(" :: ".getBytes());
      fout.write(msg.getBytes());
      fout.write(0x0a);
      fout.close();
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
    
  private static void log(Object value, Vars.LogLevel level) {
    String msg = null;
    if (value != null) {
      msg = value.toString();
      if (Vars.APP_LOG_LEVEL == Vars.LogLevel.VERBOSE) {
        msg += " " + getCallerLogInfo();
      }
    }
    
    try {
      if (Vars.APP_LOG_LEVEL.compareTo(level) <= 0) {
        switch (level) {
        case INFO:
          Log.i(Vars.APP_LOG_TAG, msg);
          break;
        case DEBUG:
          Log.d(Vars.APP_LOG_TAG, msg);
          break;
        case WARN:
          Log.w(Vars.APP_LOG_TAG, msg);
          break;
        case ERROR:
          Log.e(Vars.APP_LOG_TAG, msg);
          break;
        }
      }
      
      // log_file(msg);
    }
    catch (Exception e) {
      System.out.println(Vars.APP_LOG_TAG + " :: " + msg);
    }
  }
  
  
  public static void log(String format, Object... values) {
    try {
      log(String.format(format, values));
    }
    catch (Exception e) {
      loge("Fun.log(format, values) Exception, " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  public static void logd(String format, Object... values) {
    try {
      logd(String.format(format, values));
    }
    catch (Exception e) {
      loge("Fun.logd(format, values) Exception, " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  public static void loge(String format, Object... values) {
    try {
      loge(String.format(format, values));
    }
    catch (Exception e) {
      loge("Fun.loge(format, values) Exception, " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  public static void log(Object value) {
    log(value, Vars.LogLevel.INFO);
  }
  
  public static void logd(Object value) {
    log(value, Vars.LogLevel.DEBUG);
  }
  
  public static void logw(Object value) {
    log(value, Vars.LogLevel.WARN);
  }
  
  public static void loge(Object value) {
    log(value, Vars.LogLevel.ERROR);
  }
  
  
  private static String getCallerLogInfo() {
    String result = "";
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    
    if (stackTrace != null && stackTrace.length > 1){
      boolean currentFound = false;
      
      int len = stackTrace.length;
      for (int i = 0; i < len; i++) {
        StackTraceElement stackElement = stackTrace[i];
        String className = stackElement.getClassName();
        
        if (className != null && className.equals(Fun.class.getName())) {
          currentFound = true;
        }
        
        if (currentFound && className != null && !className.equals(Fun.class.getName())) {
          String resultClass = stackElement.getClassName();
          String method = stackElement.getMethodName();
          int line = stackElement.getLineNumber();
          result = "[" + resultClass + ":" + method + "():" + line + "]";
          break;
        }
      }
    }
    
    return result;
  }
  
  public static void showNotification(Context context, int id, boolean fixed, String text) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Vars.NOTIFICATIONS_CHANNEL_ID);
    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    
    builder.setSmallIcon(R.drawable.ic_notifications_none_white_24dp);
    builder.setLargeIcon(largeIcon);
    builder.setOngoing(fixed);
    
    builder.setContentTitle(Vars.NOTIFICATION_TITLE);
    builder.setContentText(text);
    
    Intent intent = new Intent(context, MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    
    builder.setContentIntent(pendingIntent);
    
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(id, builder.build());
  }
  
  public static void showPlayerNotification(Context context, int id, String text) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Vars.NOTIFICATIONS_CHANNEL_ID);
    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    
    builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
    builder.setLargeIcon(largeIcon);
    builder.setOngoing(false);
    
    builder.setContentTitle(Vars.PLAYER_NOTIFICATION_TITLE);
    builder.setContentText(text);
    
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(id, builder.build());
  }
  
  public static void cancelNotification(Context context, int id) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(id);
  }
  
  public static void screenWakeup(Context context) {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    int wl_flags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE;
    PowerManager.WakeLock wl = pm.newWakeLock(wl_flags, "ScreenWakeupTag");
    // wl.acquire(2000);
    wl.acquire();
    wl.release();
  }
  
}
