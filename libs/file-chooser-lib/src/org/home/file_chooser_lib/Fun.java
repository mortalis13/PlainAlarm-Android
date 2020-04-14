package org.home.file_chooser_lib;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


public class Fun {
  
  private static Context context;
  
  
  public static void setContext(Context context) {
    if (Fun.context == null) Fun.context = context;
  }
  
  public static void removeContext() {
    Fun.context = null;
  }
  
  
  //---------------------------------------------- IO ----------------------------------------------
  
  public static FileFilter dirFilter = (file) -> {
    return (file.isDirectory() && file.canRead() && !file.isHidden());
  };
  
  public static Comparator<File> nocaseComp = (item1, item2) -> {
    if (item1 == null || item2 == null) return 0;
    return item1.getName().compareToIgnoreCase(item2.getName());
  };
  
  public static Comparator<String> nocaseCompStr = (item1, item2) -> {
    if (item1 == null || item2 == null) return 0;
    return item1.compareToIgnoreCase(item2);
  };
  
  
  public static boolean fileExists(String filePath) {
    if (empty(filePath)) return false;
    return new File(filePath).exists();
  }
  
  public static String getParentFolder(String filePath) {
    return new File(filePath).getParentFile().getAbsolutePath();
  }
  
  public static int getRandomInt(int from, int to) {
    return from + new Random().nextInt(to - from + 1);
  }
  
  public static boolean createFolder(String path) {
    boolean result = true;
    
    try {
      File f = new File(path);
      if (!f.exists()) {
        f.mkdir();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      result = false;
    }
    
    return result;
  }
  

  //---------------------------------------------- Resources ----------------------------------------------
  
  public static byte[] getRawResource(int resourceId) {
    if (context == null) return null;
    InputStream is = context.getResources().openRawResource(resourceId);
    
    byte[] buf = null;
    
    try {
      buf = new byte[is.available()];
      is.read(buf);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return buf;
  }
  
  public static int getColor(int resourceId) {
    if (context == null) return 0;
    return context.getResources().getColor(resourceId);
  }
  
  public static String getString(int resourceId) {
    if (context == null) return null;
    return context.getResources().getString(resourceId);
  }
  
  public static int getInteger(int resourceId) {
    if (context == null) return 0;
    return context.getResources().getInteger(resourceId);
  }
  
  public static int getDimension(int resourceId) {
    if (context == null) return 0;
    return (int) context.getResources().getDimension(resourceId);
  }
  
  
  //---------------------------------------------- Log ----------------------------------------------
  
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
          default:
              break;
        }
      }
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
  
  public static void logw(String format, Object... values) {
    try {
      logw(String.format(format, values));
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
  
  
  //---------------------------------------------- Utils ----------------------------------------------
  
  public static boolean empty(Object object) {
    if (object == null) {
      return true;
    }
    if (object instanceof String) {
      return ((String) object).trim().length() == 0;
    }
    if (object instanceof Collection) {
      return ((Collection) object).isEmpty();
    }
    if (object.getClass().isArray()) {
      if (((Object[]) object).length == 0) {
        return true;
      }
    }
    
    return false;
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
  
  
  //---------------------------------------------- Android Utils ----------------------------------------------
  
  public static void toast(Context context, String msg) {
    if (context == null) return;
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
  }
  
  public static void toast(String msg) {
    toast(context, msg);
  }
  
  public static void toastOnMain(String msg) {
    new Handler(Looper.getMainLooper()).post(() -> {
      toast(msg);
    });
  }
  
  public static float dpToPx(float dp) {
    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
    float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    return px;
  }
  
  public static float pxToDp(float px) {
    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
    float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    return dp;
  }
  
  public static int getScreenWidth() {
    if (context == null) return 0;
    int result = 0;
    
    try {
      DisplayMetrics displayMetrics = new DisplayMetrics();
      Activity activity = (Activity) context;
      activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      result = displayMetrics.widthPixels;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
  }
  
  public static int getScreenHeight() {
    if (context == null) return 0;
    int result = 0;
    
    try {
      DisplayMetrics displayMetrics = new DisplayMetrics();
      Activity activity = (Activity) context;
      activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      result = displayMetrics.heightPixels;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result;
  }
  
  public static float getTextWidth(TextView textView, String text) {
    if (context == null) return 0;
    
    Paint paint = textView.getPaint();
    float textWidth = paint.measureText(text);
    
    return textWidth;
  }
  
  public static int measureListItemHeight(Adapter adapter) {
    if (context == null) return 0;
    int result = 0;
    
    if (adapter.getCount() > 0) {
      int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      
      ViewGroup measureParent = new FrameLayout(context);
      View itemView = adapter.getView(0, null, measureParent);
      itemView.measure(widthMeasureSpec, heightMeasureSpec);
      
      result = itemView.getMeasuredHeight();
    }
    
    return result;
  }
  
  public static int measureViewHeight(View view) {
    int result = 0;
    
    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    view.measure(widthMeasureSpec, heightMeasureSpec);
    result = view.getMeasuredHeight();
    
    return result;
  }
  
  public static int measureViewWidth(View view) {
    int result = 0;
    
    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    view.measure(widthMeasureSpec, heightMeasureSpec);
    result = view.getMeasuredWidth();
    
    return result;
  }
  
  public static int measureContentWidth(Adapter adapter) {
    if (context == null) return 0;
    int maxWidth = 0;
    
    ViewGroup measureParent = null;
    View itemView = null;
    int itemType = 0;
    
    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    
    int count = adapter.getCount();
    for (int i = 0; i < count; i++) {
      int positionType = adapter.getItemViewType(i);
      if (positionType != itemType) {
        itemType = positionType;
        itemView = null;
      }
      
      if (measureParent == null) measureParent = new FrameLayout(context);
      itemView = adapter.getView(i, itemView, measureParent);
      itemView.measure(widthMeasureSpec, heightMeasureSpec);
      
      int itemWidth = itemView.getMeasuredWidth();
      if (itemWidth > maxWidth) maxWidth = itemWidth;
    }
    
    return maxWidth;
  }
  
  public static void showKeyboard(View view) {
    if (view == null) return;
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
  }
  
  public static void showKeyboard(View view, int delay) {
    new Handler().postDelayed(() -> showKeyboard(view), delay);
  }
  
  public static void showKeyboard() {
    if (context == null) return;
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }
  
  public static void hideKeyboard(Activity activity) {
    if (activity == null) return;
    View view = activity.findViewById(android.R.id.content);
    hideKeyboard(view);
  }

  public static void hideKeyboard(View view) {
    if (view == null) return;
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
  
  public static void rotateScreen(Activity activity) {
    int currentOrientation = activity.getResources().getConfiguration().orientation;
    
    int newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
      newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }
    
    activity.setRequestedOrientation(newOrientation);
    
    int rotationState = Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
    if (rotationState == 1) {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
  }
  
  public static void changeOverScrollGlowColor(Resources res, int colorID) {
    try {
      final int glowDrawableId = res.getIdentifier("overscroll_glow", "drawable", "android");
      final Drawable overscrollGlow = res.getDrawable(glowDrawableId);
      overscrollGlow.setColorFilter(res.getColor(colorID), PorterDuff.Mode.SRC_ATOP);

      final int edgeDrawableId = res.getIdentifier("overscroll_edge", "drawable", "android");
      final Drawable overscrollEdge = res.getDrawable(edgeDrawableId);
      overscrollEdge.setColorFilter(res.getColor(colorID), PorterDuff.Mode.SRC_ATOP);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void dimPopupBackground(PopupWindow popup) {
    View container = popup.getContentView().getRootView();
    Context context = popup.getContentView().getContext();
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
    p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
    p.dimAmount = 0.3f;
    wm.updateViewLayout(container, p);
  }
  
  public static void toggleFullScreen(Window window) {
    WindowManager.LayoutParams attrs = window.getAttributes();
    int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
    boolean inFullScreenMode = (attrs.flags & flag) != 0;
    
    if (inFullScreenMode) {
      attrs.flags &= ~flag;
    }
    else {
      attrs.flags |= flag;
    }
    window.setAttributes(attrs);
  }
  
}
