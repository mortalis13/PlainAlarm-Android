package org.mortalis.plainalarm;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.home.file_chooser_lib.DirectoryPickerDialog;
import org.home.file_chooser_lib.FilePickerDialog;

import android.os.Environment;
import android.net.Uri;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.graphics.drawable.Animatable;
import android.support.graphics.drawable.Animatable2Compat.AnimationCallback;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.BroadcastReceiver;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;

import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.NotificationManager;


public class MainActivity extends AppCompatActivity {
  
  private static int DRAWABLE_PREF_BACKGROUND_ENABLED           = R.drawable.plain_pref_background_enabled;
  private static int DRAWABLE_PREF_BACKGROUND_DISABLED          = R.drawable.plain_pref_background_disabled;
  private static int DRAWABLE_ALARM_PANEL_BACKGROUND_STOP       = R.drawable.alarm_switcher_background_stop;
  private static int DRAWABLE_ALARM_PANEL_BACKGROUND_START      = R.drawable.alarm_switcher_background_start;
  private static int DRAWABLE_TOGGLE_BUTTON_BACKGROUND_ENABLED  = R.drawable.toggle_button_enabled;
  private static int DRAWABLE_TOGGLE_BUTTON_BACKGROUND_DISABLED = R.drawable.toggle_button_disabled;
  
  private boolean textWatcherEnabled = true;
  private boolean isAlarmWakeup;
  
  private DirectoryPickerDialog dirPickerDialog;
  private FilePickerDialog filePickerDialog;
  
  private Context context;
  private AlarmManager alarmManager;
  private PendingIntent pendingIntent;
  private AudioManager audioManager;
  
  private View parentView;
  
  private LinearLayout soundSelector;
  private LinearLayout soundFolderSelector;
  private Spinner volumeSelector;
  private Spinner snoozeTimeSelector;
  private ImageButton bSnooze;
  private LinearLayout panelAlarmState;
  
  private ImageView imageAlarmWakeupAnimation;
  
  private TextView soundPathView;
  private TextView soundFolderPathView;
  
  private TextView alarmPresetText1;
  private TextView alarmPresetText2;
  private TextView alarmPresetText3;
  private TextView alarmPresetText4;
  
  private EditText hoursField;
  private EditText minutesField;
  
  private Animatable alarmWakeupAnimation;
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Fun.logd("MainActivity.onCreate()");
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    context = this;
    MainService.context = context;
    
    requestAppPermissions(context);
    createNotificationChannel();
    
    init();
    configUI();
    restoreState();
    
    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent receiverIntent = new Intent(this, AlarmReceiver.class);
    pendingIntent = PendingIntent.getBroadcast(this, 0, receiverIntent, 0);
    
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }
  
  @Override
  protected void onResume() {
    Fun.logd("MainActivity.onResume()");
    super.onResume();
    
    Fun.logd("onResume -> isWakeupIntent: " + isWakeupIntent(getIntent()));
    if (isWakeupIntent(getIntent())) {
      getIntent().putExtra(AlarmReceiver.ALARM_WAKEUP_INTENT, false);
      wakeupAlarm();
    }
  }
  
  @Override
  protected void onNewIntent(Intent intent) {
    Fun.logd("MainActivity.onNewIntent()");
    super.onNewIntent(intent);
    // setIntent(intent);
    Fun.logd("onNewIntent -> isWakeupIntent: " + isWakeupIntent(intent));
    if (isWakeupIntent(intent)) wakeupAlarm();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
  
  
  // -----------------------------------------------------------
  
  private void requestAppPermissions(Context context) {
    boolean isWriteGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    
    if (!isWriteGranted) {
      requestPermissions(new String[] {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      }, Vars.APP_PERMISSION_REQUEST_ACCESS_EXTERNAL_STORAGE);
    }
    
    // PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    // Fun.log("isIgnoringBatteryOptimizations(): " + pm.isIgnoringBatteryOptimizations(getPackageName()));
    
    // Intent intent1 = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
    // startActivity(intent1);
    
    if (!Settings.canDrawOverlays(context)){
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivity(intent);
    }
  }
  
  private void init() {
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    
    filePickerDialog = new FilePickerDialog(context);
    filePickerDialog.setExtensionFilter(Vars.AUDIO_EXTS);
    filePickerDialog.setFileSelectedListener(file -> {
      String path = file.getAbsolutePath();
      Fun.saveSharedPref(context, Vars.PREF_KEY_SOUND_FILE_PATH, path);
      soundPathView.setText(path);
    });
    
    dirPickerDialog = new DirectoryPickerDialog(context, true);
    dirPickerDialog.setFileSelectedListener(file -> {
      String path = file.getPath();
      Fun.saveSharedPref(context, Vars.PREF_KEY_SOUND_FOLDER_PATH, path);
      soundFolderPathView.setText(path);
    });
    
    // Fun.storagePath = context.getFilesDir().getPath();
    Fun.storagePath = Environment.getExternalStorageDirectory().getPath();
  }
  
  private void configUI() {
    // -- Views
    parentView = findViewById(R.id.parentView);
    hoursField = findViewById(R.id.hoursField);
    minutesField = findViewById(R.id.minutesField);
    
    soundSelector = findViewById(R.id.soundSelector);
    soundFolderSelector = findViewById(R.id.soundFolderSelector);
    volumeSelector = findViewById(R.id.volumeSelector);
    snoozeTimeSelector = findViewById(R.id.snoozeTimeSelector);
    bSnooze = findViewById(R.id.bSnooze);
    panelAlarmState = findViewById(R.id.panelAlarmState);
    
    imageAlarmWakeupAnimation = findViewById(R.id.imageAlarmWakeupAnimation);
    
    soundPathView = findViewById(R.id.soundPathView);
    soundFolderPathView = findViewById(R.id.soundFolderPathView);
    
    alarmPresetText1 = findViewById(R.id.alarmPresetText1);
    alarmPresetText2 = findViewById(R.id.alarmPresetText2);
    alarmPresetText3 = findViewById(R.id.alarmPresetText3);
    alarmPresetText4 = findViewById(R.id.alarmPresetText4);
    
    
    // -- Config
    parentView.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) hideSoftInput();
    });
    
    hoursField.addTextChangedListener(new NumberTextWatcher(hoursField, Vars.HOUR_MIN, Vars.HOUR_MAX, true));
    hoursField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    
    minutesField.addTextChangedListener(new NumberTextWatcher(minutesField, Vars.MINUTE_MIN, Vars.MINUTE_MAX, false));
    minutesField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    
    // hoursField.setFilters(new InputFilter[] { new NumberTextFilter(Vars.HOUR_MIN, Vars.HOUR_MAX, 2, Vars.TIME_CHARS) });
    // minutesField.setFilters(new InputFilter[] { new NumberTextFilter(Vars.MINUTE_MIN, Vars.MINUTE_MAX, 2, Vars.TIME_CHARS) });
    
    // String fontPath = "fonts/Digital.ttf";
    // Typeface typeface = Typeface.createFromAsset(getAssets(), fontPath);
    
    soundSelector.setOnClickListener(v -> {
      updateInputState();
      selectSound();
    });
    
    soundFolderSelector.setOnClickListener(v -> {
      updateInputState();
      selectSoundFolder();
    });
    
    panelAlarmState.setOnClickListener(v -> {
      hideSoftInput();
      
      boolean alarmStarted = isAlarmStarted();
      boolean snoozeOn = Fun.getSharedPrefBool(context, Vars.PREF_KEY_SNOOZE_ON);
      if (!isAlarmWakeup || !snoozeOn) {
        updateAlarmState(!alarmStarted);
      }
      
      if (!alarmStarted) {
        if (Vars.DEBUG_MODE || Vars.DEMO_MODE) {
          setAlarmStartedPref();
          wakeupAlarm();
          return;
        }
        
        Fun.logd("Starting Alarm");
        startAlarm();
        updateInputState();
      }
      else {
        Fun.logd("Stopping Alarm");
        stopAlarm();
      }
    });
    
    bSnooze.setOnClickListener(v -> {
      boolean snoozeOn = Fun.getSharedPrefBool(context, Vars.PREF_KEY_SNOOZE_ON);
      updateSnoozeState(!snoozeOn);
    });
    
    OnClickListener alarmPresetClickListener = (v) -> {
      TextView textView = (TextView) v;
      String presetText = (String) textView.getText();
      updateAlarmText(presetText);
      Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_TEXT, presetText);
      if (isAlarmStarted()) startAlarm();
    };
    
    OnLongClickListener alarmPresetLongClickListener = (v) -> {
      int id = v.getId();
      TextView textView = findViewById(id);
      
      String clockText = getClockText();
      textView.setText(clockText);
      
      String tag = (String) v.getTag();
      Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_PRESET + tag, clockText);
      
      return true;
    };
    
    alarmPresetText1.setOnClickListener(alarmPresetClickListener);
    alarmPresetText2.setOnClickListener(alarmPresetClickListener);
    alarmPresetText3.setOnClickListener(alarmPresetClickListener);
    alarmPresetText4.setOnClickListener(alarmPresetClickListener);
    
    alarmPresetText1.setOnLongClickListener(alarmPresetLongClickListener);
    alarmPresetText2.setOnLongClickListener(alarmPresetLongClickListener);
    alarmPresetText3.setOnLongClickListener(alarmPresetLongClickListener);
    alarmPresetText4.setOnLongClickListener(alarmPresetLongClickListener);
    
    alarmPresetText1.setTag(Vars.PREF_ALARM_PRESET_TAGS[0]);
    alarmPresetText2.setTag(Vars.PREF_ALARM_PRESET_TAGS[1]);
    alarmPresetText3.setTag(Vars.PREF_ALARM_PRESET_TAGS[2]);
    alarmPresetText4.setTag(Vars.PREF_ALARM_PRESET_TAGS[3]);
    
    initVolumeSelector();
    initSnoozeTimeSelector();
  }
  
  
  private void restoreState() {
    try {
      String timeStr = Fun.getSharedPref(context, Vars.PREF_KEY_ALARM_TIMESTAMP);
      Fun.log(".. timeStr: " + timeStr);
      
      String alarmText = Fun.getSharedPref(context, Vars.PREF_KEY_ALARM_TEXT);
      if (alarmText == null) alarmText = Vars.DEFAULT_ALARM_TEXT;
      if (Vars.DEMO_MODE) alarmText = Vars.DEMO_TIME;
      updateAlarmText(alarmText);
      
      String soundPath = Fun.getSharedPref(context, Vars.PREF_KEY_SOUND_FILE_PATH);
      if (soundPath == null) soundPath = "";
      if (Vars.DEMO_MODE) soundPath = Vars.DEMO_SOUND_PATH;
      soundPathView.setText(soundPath);
      
      String soundFolderPath = Fun.getSharedPref(context, Vars.PREF_KEY_SOUND_FOLDER_PATH);
      if (soundFolderPath == null) soundFolderPath = "";
      if (Vars.DEMO_MODE) soundFolderPath = Vars.DEMO_SOUND_FOLDER_PATH;
      soundFolderPathView.setText(soundFolderPath);
      
      updateSnoozeState(Fun.getSharedPrefBool(context, Vars.PREF_KEY_SNOOZE_ON));
      updateAlarmState(isAlarmStarted());
      
      List<String> alarmPresets = new ArrayList<>();
      for (String tag: Vars.PREF_ALARM_PRESET_TAGS) {
        String alarmPresetText = Fun.getSharedPref(context, Vars.PREF_KEY_ALARM_PRESET + tag);
        if (alarmPresetText == null) alarmPresetText = Vars.DEFAULT_ALARM_TEXT;
        alarmPresets.add(alarmPresetText);
      }
      if (Vars.DEMO_MODE) {
        alarmPresets.set(0, Vars.DEMO_PRESET_1);
        alarmPresets.set(1, Vars.DEMO_PRESET_2);
        alarmPresets.set(2, Vars.DEMO_PRESET_3);
        alarmPresets.set(3, Vars.DEMO_PRESET_4);
      }
      
      alarmPresetText1.setText(alarmPresets.get(0));
      alarmPresetText2.setText(alarmPresets.get(1));
      alarmPresetText3.setText(alarmPresets.get(2));
      alarmPresetText4.setText(alarmPresets.get(3));
      
      updateOptionsState();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  // ------------------------------ Actions ------------------------------
  private void initVolumeSelector() {
    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    // int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    int curVolume = (int) Fun.getSharedPrefLong(context, Vars.PREF_KEY_ALARM_VOLUME);
    if (curVolume == -1) curVolume = Vars.DEFAULT_ALARM_VOLUME;
    
    List<String> items = new ArrayList<>();
    for (int i = 0; i <= maxVolume; i++) {
      items.add(String.valueOf(i));
    }
    VolumeListAdapter adapter = new VolumeListAdapter(this, items);
    volumeSelector.setAdapter(adapter);
    volumeSelector.setSelection(curVolume);
    
    volumeSelector.postDelayed(() -> {
      volumeSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_VOLUME, position);
          // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, position, 0);
        }
        public void onNothingSelected(AdapterView<?> parent) {}
      });
    }, 1000);
  }
  
  private void initSnoozeTimeSelector() {
    int listPos = (int) Fun.getSharedPrefLong(context, Vars.PREF_KEY_SNOOZE_TIME_POS);
    if (listPos == -1) {
      listPos = 0;
      Fun.saveSharedPref(context, Vars.PREF_KEY_SNOOZE_TIME, Vars.DEFAULT_SNOOZE_TIME);
    }
    
    List<String> items = new ArrayList<String>() {{
      add("3m");
      add("5m");
      add("10m");
      add("15m");
      add("20m");
    }};

    SnoozeTimeListAdapter adapter = new SnoozeTimeListAdapter(this, items);
    snoozeTimeSelector.setAdapter(adapter);
    snoozeTimeSelector.setSelection(listPos);
    
    snoozeTimeSelector.postDelayed(() -> {
      snoozeTimeSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          int snoozeTime = Vars.DEFAULT_SNOOZE_TIME;
          try {
            String item = (String) parent.getItemAtPosition(position);
            snoozeTime = Integer.parseInt(item.replace("m", "")) * 60;
          }
          catch (Exception e) {
            e.printStackTrace();
          }
          Fun.saveSharedPref(context, Vars.PREF_KEY_SNOOZE_TIME_POS, position);
          Fun.saveSharedPref(context, Vars.PREF_KEY_SNOOZE_TIME, snoozeTime);
        }
        public void onNothingSelected(AdapterView<?> parent) {}
      });
    }, 1000);
  }
  
  private void setMinVolume() {
    int alarmVolume = volumeSelector.getSelectedItemPosition();
    // if (alarmVolume <= 0) alarmVolume = 1;
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, alarmVolume, 0);
  }
  
  
  // ------------------------------ Main Engine ------------------------------
  private void startAlarm() {
    int alarmHour = Integer.parseInt(hoursField.getText().toString());
    int alarmMinute = Integer.parseInt(minutesField.getText().toString());
    startAlarm(alarmHour, alarmMinute);
  }
  
  private void startAlarm(int alarmHour, int alarmMinute) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, alarmHour);
    calendar.set(Calendar.MINUTE, alarmMinute);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    if (System.currentTimeMillis() >= calendar.getTimeInMillis()) {
      calendar.add(Calendar.DATE, 1);
    }
    
    long timeMillis = calendar.getTimeInMillis();
    startAlarm(timeMillis);
  }
  
  private void startAlarm(long timeMillis) {
    Fun.logd("MainActivity.startAlarm()");
    
    isAlarmWakeup = false;
    // setMinVolume();
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // >=API-21
      PendingIntent opIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
      alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(timeMillis, opIntent), pendingIntent);
      
      // alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
    }
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // >=API-19
      alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
    }
    else {
      alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
    }
    
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_TIME_MILLIS, timeMillis);
    setAlarmStartedPref();
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeMillis);
    String hours = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
    String minutes = String.valueOf(calendar.get(Calendar.MINUTE));
    
    String timeStr = String.format("Alarm Time: %s:%s = %d", hours, minutes, timeMillis);
    Fun.logd(timeStr);
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_TIMESTAMP, timeStr);
    
    String alarmTime = getClockText(hours, minutes);
    Fun.showNotification(context, Vars.NOTIFICATION_ID, true, "Alarm Set: " + alarmTime);
  }
  
  private void snoozeAlarm(int seconds) {
    Fun.logd("snoozeAlarm(): " + seconds);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, seconds);
    long timeMillis = calendar.getTimeInMillis();
    startAlarm(timeMillis);
  }
  
  private void stopAlarm() {
    if (alarmManager != null) alarmManager.cancel(pendingIntent);
    if (alarmWakeupAnimation != null) alarmWakeupAnimation.stop();
    imageAlarmWakeupAnimation.setVisibility(View.GONE);
    
    stopSound();
    unsetAlarmStartedPref();
    Fun.cancelNotification(context, Vars.NOTIFICATION_ID);
    
    boolean snoozeOn = Fun.getSharedPrefBool(context, Vars.PREF_KEY_SNOOZE_ON);
    if (snoozeOn && isAlarmWakeup) {
      int snoozeTime = (int) Fun.getSharedPrefLong(context, Vars.PREF_KEY_SNOOZE_TIME);
      if (snoozeTime == -1) snoozeTime = Vars.DEFAULT_SNOOZE_TIME;
      if (Vars.DEBUG_MODE) snoozeTime = Vars.SNOOZE_TIME_DEBUG;
      snoozeAlarm(snoozeTime);
    }
    isAlarmWakeup = false;
  }
  
  private void wakeupAlarm() {
    Fun.logd("wakeupAlarm()");
    Fun.logd("Timestamp: " + System.currentTimeMillis());
    
    isAlarmWakeup = true;
    setMinVolume();
    playSound();
    animateClock();
  }
  
  
  // ---------------------------------------- UI Actions -------------------
  private void selectSound() {
    Fun.logd("selectSound()");
    
    boolean useSoundFolder = isUseSoundFolder();
    if (useSoundFolder) {
      unsetUseSoundFolderPref();
      updateOptionsState();
      return;
    }
    
    String soundPath = Fun.getSharedPref(context, Vars.PREF_KEY_SOUND_FILE_PATH);
    String startPath = null;
    if (soundPath != null) {
      startPath = Fun.getParentFolder(soundPath);
    }
    
    filePickerDialog.showDialog(startPath);
  }
  
  private void selectSoundFolder() {
    Fun.logd("selectSoundFolder()");
    
    boolean useSoundFolder = isUseSoundFolder();
    if (!useSoundFolder) {
      setUseSoundFolderPref();
      updateOptionsState();
      return;
    }
    
    String soundFolderPath = Fun.getSharedPref(context, Vars.PREF_KEY_SOUND_FOLDER_PATH);
    String startPath = null;
    if (soundFolderPath != null) {
      startPath = soundFolderPath;
    }
    
    dirPickerDialog.showDialog(startPath);
  }
  

  // --------------------------------------- UI Utils --------------------
  
  private void updateAlarmText(String text) {
    textWatcherEnabled = false;
    String[] items = text.split(":");
    hoursField.setText(items[0]);
    minutesField.setText(items[1]);
    textWatcherEnabled = true;
  }
  
  private void updateOptionsState() {
    boolean useSoundFolder = isUseSoundFolder();
    if (useSoundFolder) {
      soundSelector.setBackgroundResource(DRAWABLE_PREF_BACKGROUND_DISABLED);
      soundFolderSelector.setBackgroundResource(DRAWABLE_PREF_BACKGROUND_ENABLED);
    }
    else {
      soundSelector.setBackgroundResource(DRAWABLE_PREF_BACKGROUND_ENABLED);
      soundFolderSelector.setBackgroundResource(DRAWABLE_PREF_BACKGROUND_DISABLED);
    }
  }
  
  private void updateAlarmState(boolean enabled) {
    int stateBackgroundId = enabled ? DRAWABLE_ALARM_PANEL_BACKGROUND_STOP : DRAWABLE_ALARM_PANEL_BACKGROUND_START;
    panelAlarmState.setBackgroundResource(stateBackgroundId);
  }
  
  private void updateSnoozeState(boolean enabled) {
    int stateBackgroundId = enabled ? DRAWABLE_TOGGLE_BUTTON_BACKGROUND_ENABLED : DRAWABLE_TOGGLE_BUTTON_BACKGROUND_DISABLED;
    bSnooze.setBackgroundResource(stateBackgroundId);
    Fun.saveSharedPref(context, Vars.PREF_KEY_SNOOZE_ON, enabled);
  }
  
  private void animateClock() {
    imageAlarmWakeupAnimation.setVisibility(View.VISIBLE);
    
    Drawable alarmWakeupDrawable = imageAlarmWakeupAnimation.getDrawable();
    alarmWakeupAnimation = (Animatable) alarmWakeupDrawable;
    AnimatedVectorDrawableCompat.registerAnimationCallback(alarmWakeupDrawable, new AnimationCallback() {
      public void onAnimationEnd(Drawable drawable) {
        new Handler().postDelayed(() -> alarmWakeupAnimation.start(), 1000);
      }
    });
    alarmWakeupAnimation.start();
  }
  
  private String getClockText() {
    String hours = hoursField.getText().toString();
    String minutes = minutesField.getText().toString();
    return getClockText(hours, minutes);
  }
  
  private String getClockText(String hours, String minutes) {
    if (hours.length() == 1) hours = "0" + hours;
    if (minutes.length() == 1) minutes = "0" + minutes;
    
    String result = hours + ":" + minutes;
    return result;
  }
  
  private boolean isWakeupIntent(Intent intent) {
    boolean result = false;
    Bundle extras = intent.getExtras();
    if (extras != null) {
      result = extras.getBoolean(AlarmReceiver.ALARM_WAKEUP_INTENT);
      // intent.putExtra(AlarmReceiver.ALARM_WAKEUP_INTENT, false);
    }
    
    return result;
  }
  
  
  // --------------------------------------- Utils --------------------
  
  private void playSound() {
    Fun.logd("playSound()");
    
    Intent playerIntent = new Intent(this, PlayerService.class);
    boolean useSoundFolder = isUseSoundFolder();
    if (useSoundFolder) {
      String soundFolderPath = Fun.getSharedPref(context, Vars.PREF_KEY_SOUND_FOLDER_PATH);
      playerIntent.putExtra(Vars.EXTRA_SOUND_FOLDER_PATH, soundFolderPath);
      playerIntent.putExtra(Vars.EXTRA_SOUND_FROM_FOLDER, true);
    }
    else {
      String soundPath = Fun.getSharedPref(context, Vars.PREF_KEY_SOUND_FILE_PATH);
      playerIntent.putExtra(Vars.EXTRA_SOUND_PATH, soundPath);
    }
    
    startService(playerIntent);
  }
  
  private void stopSound() {
    Fun.logd("stopSound()");
    stopService(new Intent(this, PlayerService.class));
  }
  
  
  private void updateInputState() {
    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      if (inputMethodManager.isActive(hoursField)) {
        hoursField.clearFocus();
      }
      else if (inputMethodManager.isActive(minutesField)) {
        minutesField.clearFocus();
      }
      
      // inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
      View v = getWindow().getDecorView().getRootView();
      if (v != null) inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
  }
  
  private void showSoftInput(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      view.requestFocus();
      inputMethodManager.showSoftInput(view, 0);
    }
  }
  
  private void hideSoftInput() {
    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    View v = getWindow().getDecorView().getRootView();
    if (inputMethodManager == null || v == null) return;
    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
  }
  
  private boolean isAlarmStarted() {
    return Fun.getSharedPrefBool(context, Vars.PREF_KEY_ALARM_STARTED);
  }
  private void setAlarmStartedPref() {
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_STARTED, true);
  }
  private void unsetAlarmStartedPref() {
    Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_STARTED, false);
  }
  
  private boolean isUseSoundFolder() {
    return Fun.getSharedPrefBool(context, Vars.PREF_KEY_USE_SOUND_FOLDER);
  }
  private void setUseSoundFolderPref() {
    Fun.saveSharedPref(context, Vars.PREF_KEY_USE_SOUND_FOLDER, true);
  }
  private void unsetUseSoundFolderPref() {
    Fun.saveSharedPref(context, Vars.PREF_KEY_USE_SOUND_FOLDER, false);
  }
  
  
  // ---------------------------------------- Utils -------------------
  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String id = Vars.NOTIFICATIONS_CHANNEL_ID;
      CharSequence name = getString(R.string.notification_channel_name);
      String description = getString(R.string.notification_channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;

      NotificationChannel channel = new NotificationChannel(id, name, importance);
      channel.setDescription(description);
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  
  // ---------------------------------------- Classes -------------------
  class NumberTextWatcher implements TextWatcher {
    private int minValue;
    private int maxValue;
    private int maxLength;
    
    private boolean focusNext;
    private String prevText;
    
    private EditText editText;
    
    public NumberTextWatcher(EditText editText, int minValue, int maxValue, boolean focusNext) {
      this.editText = editText;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.focusNext = focusNext;
      maxLength = String.valueOf(maxValue).length();
    }
    
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (!textWatcherEnabled) return;
      
      String str = s.toString();
      if (str.length() == 0 || str.equals(prevText)) return;
      Fun.logd("onTextChanged: [" + s + "; " + start + "; " + before + "; " + count + "]");
      
      str = str.replaceAll("[^0-9]", "");
      
      StringBuilder sb = new StringBuilder(str);
      if (sb.length() > maxLength) {
        sb.delete(maxLength, sb.length());
      }
      
      try {
        int value = Integer.parseInt(sb.toString());
        if (value > maxValue) {
          sb.delete(start, start + count);
        }
      }
      catch (NumberFormatException e) {
        sb.replace(0, sb.length(), String.valueOf(minValue));
      }
      
      str = sb.toString();
      if (!s.toString().equals(str)) {
        editText.setText(str);
        editText.setSelection(str.length());
      }
      
      int maxComplex = Integer.parseInt(String.valueOf(maxValue).substring(0, 1));
      boolean uniqueDigit = str.length() == 1 && Integer.parseInt(str) > maxComplex;
      
      if (str.length() == 2 || uniqueDigit) {
        if (uniqueDigit) editText.setText("0" + str);
        
        // restart alarm on time change
        if (isAlarmStarted()) startAlarm();
        
        final View nextView = hoursField.focusSearch(View.FOCUS_FORWARD);
        if (focusNext && nextView != null) {
          // delay defore focusing (otherwise triggers textChanged() for the focused field)
          nextView.post(() -> nextView.requestFocus());
        }
        else {
          updateInputState();
        }
      }
      
      prevText = str;
      
      String clockText = getClockText();
      if (clockText.length() == 5) {
        Fun.saveSharedPref(context, Vars.PREF_KEY_ALARM_TEXT, clockText);
      }
    }
    
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void afterTextChanged(Editable s) {}
  }
  
  
  class NumberTextFilter extends NumberKeyListener {
    private int minValue;
    private int maxValue;
    private int maxLength;
    private char[] chars;
    
    public NumberTextFilter(int minValue, int maxValue, int maxLength, char[] chars) {
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.maxLength = maxLength;
      this.chars = chars;
    }
    
    @Override
    public int getInputType() {
      return InputType.TYPE_CLASS_TEXT;
    }
    
    @Override
    protected char[] getAcceptedChars() {
      return chars;
    }
    
    private int getSelectedPos(String value) {
      try {return Integer.parseInt(value); }
      catch (NumberFormatException e) {}
      return minValue;
    }
    
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
      CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
      
      if (filtered == null) {
        String result = String.valueOf(dest.subSequence(0, dstart)) + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
        int val = getSelectedPos(result);
        if (val > maxValue || result.length() > maxLength || result.length() == 0) return "";
        return null;
      }
      
      return filtered;
    }
  }
  
  
  class VolumeListAdapter extends ArrayAdapter<String> {
    private static final int ITEM_LAYOUT = R.layout.volume_list_item;
    private static final int SPINNER_LAYOUT = R.layout.volume_list_view;
    private Context context;
    private List<String> items;

    public VolumeListAdapter(Context context, List<String> items) {
      super(context, ITEM_LAYOUT, items);
      this.items = items;
      this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      return createView(position, convertView, parent, SPINNER_LAYOUT, false);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
      return createView(position, convertView, parent, ITEM_LAYOUT, true);
    }

    private View createView(int position, View convertView, ViewGroup parent, int layoutId, boolean dropdownView) {
      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layoutId, parent, false);
      }
      
      TextView itemText = convertView.findViewById(R.id.itemText);
      String item = getItem(position);
      if (item != null) {
        itemText.setText(item);
      }
      
      return convertView;
    }
  }
  
  class SnoozeTimeListAdapter extends ArrayAdapter<String> {
    private static final int ITEM_LAYOUT = R.layout.snooze_time_list_item;
    private static final int SPINNER_LAYOUT = R.layout.snooze_time_list_view;
    private Context context;
    private List<String> items;
    
    public SnoozeTimeListAdapter(Context context, List<String> items) {
      super(context, ITEM_LAYOUT, items);
      this.items = items;
      this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      return createView(position, convertView, parent, SPINNER_LAYOUT, false);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
      return createView(position, convertView, parent, ITEM_LAYOUT, true);
    }

    private View createView(int position, View convertView, ViewGroup parent, int layoutId, boolean dropdownView) {
      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(layoutId, parent, false);
      }
      
      TextView itemText = convertView.findViewById(R.id.itemText);
      String item = getItem(position);
      if (item != null) {
        itemText.setText(item);
      }
      
      return convertView;
    }
  }
  
  
  // --------------------
  @Override
  protected void onStart() {
    Fun.logd("MainActivity.onStart()");
    super.onStart();
  }
  
  @Override
  protected void onPause() {
    Fun.logd("MainActivity.onPause()");
    super.onPause();
  }
  
  @Override
  protected void onStop() {
    Fun.logd("MainActivity.onStop()");
    super.onStop();
    // finish();
  }
  
  @Override
  protected void onRestart() {
    Fun.logd("MainActivity.onRestart()");
    super.onStop();
  }
  
  @Override
  protected void onDestroy() {
    Fun.logd("MainActivity.onDestroy()");
    super.onDestroy();
  }
  // --------------------
  
}
