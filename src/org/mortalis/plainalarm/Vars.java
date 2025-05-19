package org.mortalis.plainalarm;

public class Vars {
  
  public static boolean DEBUG_MODE;
  // public static boolean DEBUG_MODE = true;
  
  public static int SNOOZE_TIME_DEBUG = 30;
  
  // -------------------
  
  public enum LogLevel {VERBOSE, DEBUG, INFO, WARN, ERROR};
  public static final LogLevel APP_LOG_LEVEL = LogLevel.DEBUG;
  
  public static final String APP_LOG_TAG = "plain_alarm";
  public static final String NOTIFICATION_TITLE = "PlainAlarm";
  public static final String PLAYER_NOTIFICATION_TITLE = "PlainAlarm";
  public static final int NOTIFICATION_ID = 100;
  public static final String NOTIFICATIONS_CHANNEL_ID = "plainalarm_channel_id";
  
  public static final int APP_PERMISSION_REQUEST_ACCESS_EXTERNAL_STORAGE = 101;
  public static final int APP_PERMISSION_REQUEST_CODE = 102;
  
  public static final String EXTRA_SOUND_PATH = "sound_path";
  public static final String EXTRA_SOUND_FOLDER_PATH = "sound_folder_path";
  public static final String EXTRA_SOUND_FROM_FOLDER = "sound_from_folder";
  public static final String EXTRA_AUDIO_VOLUME = "audio_volume";
  
  public static final String PREFS_FILE = "plain_alarm_prefs";
  
  public static final String PREF_KEY_SOUND_FILE_PATH = "PREF_KEY_SOUND_FILE_PATH";
  public static final String PREF_KEY_SOUND_FOLDER_PATH = "PREF_KEY_SOUND_FOLDER_PATH";
  public static final String PREF_KEY_USE_SOUND_FOLDER = "PREF_KEY_USE_SOUND_FOLDER";
  public static final String PREF_KEY_ALARM_TEXT = "PREF_KEY_ALARM_TEXT";
  public static final String PREF_KEY_ALARM_STARTED = "PREF_KEY_ALARM_STARTED";
  public static final String PREF_KEY_ALARM_PRESET = "PREF_KEY_ALARM_PRESET";
  public static final String PREF_KEY_ALARM_TIME_MILLIS = "PREF_KEY_ALARM_TIME_MILLIS";
  public static final String PREF_KEY_ALARM_VOLUME = "PREF_KEY_ALARM_VOLUME";
  public static final String PREF_KEY_SNOOZE_TIME = "PREF_KEY_SNOOZE_TIME";
  public static final String PREF_KEY_SNOOZE_TIME_POS = "PREF_KEY_SNOOZE_TIME_POS";
  public static final String PREF_KEY_SNOOZE_ON = "PREF_KEY_SNOOZE_ON";
  
  public static final String[] PREF_ALARM_PRESET_TAGS = new String[] {
    "alarmPresetText1",
    "alarmPresetText2",
    "alarmPresetText3",
    "alarmPresetText4"
  };
  
  public static final String DEFAULT_ALARM_TEXT = "00:00";
  
  public static final int HOUR_MIN = 0;
  public static final int HOUR_MAX = 23;
  public static final int MINUTE_MIN = 0;
  public static final int MINUTE_MAX = 59;
  public static final char[] TIME_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
  
  public static int DEFAULT_ALARM_VOLUME = 2;
  public static int DEFAULT_SNOOZE_TIME = 180;
  
  public static final String[] AUDIO_EXTS = new String[] {
    "aac",
    "flac",
    "mp3",
    "ogg",
    "wav",
    "mid",
    "3gp"
  };
  
  
  // ---
  
  public static final boolean DEMO_MODE = false;
  public static final String DEMO_TIME = "06:15";
  public static final String DEMO_PRESET_1 = "09:15";
  public static final String DEMO_PRESET_2 = "10:00";
  public static final String DEMO_PRESET_3 = "12:30";
  public static final String DEMO_PRESET_4 = "19:20";
  public static final String DEMO_SOUND_PATH = "/storage/extSdCard/audio/rings/02. Ride The Lightning.mp3";
  public static final String DEMO_SOUND_FOLDER_PATH = "/storage/extSdCard/audio/rings-2";
  
}
