package org.mortalis.plainalarm;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import java.util.List;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
  
  private MediaPlayer mediaPlayer;
  
  private boolean soundFromFolder;
  private String soundPath;
  private String soundFolderPath;
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(Vars.APP_LOG_TAG, "PlayerService.onStartCommand()");
    
    try {
      soundFromFolder = intent.getBooleanExtra(Vars.EXTRA_SOUND_FROM_FOLDER, false);
      soundPath = intent.getStringExtra(Vars.EXTRA_SOUND_PATH);
      soundFolderPath = intent.getStringExtra(Vars.EXTRA_SOUND_FOLDER_PATH);
      
      if (mediaPlayer != null) mediaPlayer.release();
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.setOnErrorListener(this);
      mediaPlayer.setOnCompletionListener(this);
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      
      loadPlayerSound();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return START_STICKY;
  }
  
  
  @Override
  public void onCreate() {
    super.onCreate();
  }
  
  @Override
  public void onDestroy() {
    Log.d(Vars.APP_LOG_TAG, "PlayerService.onDestroy()");
    super.onDestroy();
    if (mediaPlayer != null) mediaPlayer.release();
  }
  
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  
  // -- MediaPlayer.OnPreparedListener
  @Override
  public void onPrepared(MediaPlayer player) {
    player.start();
    Log.d(Vars.APP_LOG_TAG, "Player started");
  }
  
  // -- MediaPlayer.OnErrorListener
  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.d(Vars.APP_LOG_TAG, "MediaPlayer Error: " + what + "; " + extra);
    return true;
  }
  
  // -- MediaPlayer.OnCompletionListener
  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.d(Vars.APP_LOG_TAG, "MediaPlayer.onCompletion()");
    mediaPlayer.stop();
    mediaPlayer.reset();
    loadPlayerSound();
  }
  
  
  private void loadPlayerSound() {
    if (soundFromFolder) {
      soundPath = getRandomFile(soundFolderPath);
    }
    
    MainService.showPlayerNotification(Fun.getBaseFileName(soundPath));
    
    try {
      if (soundPath != null && Fun.fileExists(soundPath)) {
        mediaPlayer.setDataSource(soundPath);
      }
      else {
        mediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
      }
      
      mediaPlayer.prepareAsync();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private String getRandomFile(String soundFolderPath) {
    String soundPath = null;
    
    if (soundFolderPath != null && Fun.fileExists(soundFolderPath)) {
      List<String> soundFiles = Fun.getSoundFiles(soundFolderPath);
      int len = soundFiles.size();
      if (len > 0) {
        soundPath = soundFiles.get(Fun.getRandomInt(0, len - 1));
      }
      Log.d(Vars.APP_LOG_TAG, "Random audio file: \"" + soundPath + "\"");
    }
    else {
      Log.d(Vars.APP_LOG_TAG, "Sound Folder does not exist");
    }
    
    return soundPath;
  }
  
}
