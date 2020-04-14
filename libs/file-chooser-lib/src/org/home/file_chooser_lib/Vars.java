package org.home.file_chooser_lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Environment;

public class Vars {
  
  public enum LogLevel {VERBOSE, DEBUG, INFO, WARN, ERROR};
  
  public static final LogLevel APP_LOG_LEVEL = LogLevel.DEBUG;
  
  public static final String APP_LOG_TAG = "file_chooser_lib";
  
  public static final String[] AUDIO_EXTS = new String[] {
    "aac",
    "flac",
    "mp3",
    "ogg",
    "wav",
    "mid",
    "3gp"
  };
  
}
