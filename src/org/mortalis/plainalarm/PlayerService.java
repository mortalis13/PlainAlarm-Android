package org.mortalis.plainalarm;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
  
  private MediaPlayer mediaPlayer;
  
  private boolean soundFromFolder;
  private String soundPath;
  private String soundFolderPath;
  private int audioVolume;
  
  private Iterator<String> filesIter;
  
  private AudioManager audioManager;
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Fun.logd("PlayerService.onStartCommand()");
    
    try {
      soundFromFolder = intent.getBooleanExtra(Vars.EXTRA_SOUND_FROM_FOLDER, false);
      soundPath = intent.getStringExtra(Vars.EXTRA_SOUND_PATH);
      soundFolderPath = intent.getStringExtra(Vars.EXTRA_SOUND_FOLDER_PATH);
      audioVolume = intent.getIntExtra(Vars.EXTRA_AUDIO_VOLUME, 0);
      
      if (mediaPlayer != null) mediaPlayer.release();
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.setOnErrorListener(this);
      mediaPlayer.setOnCompletionListener(this);
      
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
      mediaPlayer.setVolume(0.1f, 0.1f);
      
      createFilesIterator();
      playSound();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return START_STICKY;
  }
  
  @Override
  public void onCreate() {
    Fun.logd("PlayerService.onCreate()");
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    super.onCreate();
  }
  
  
  @Override
  public void onDestroy() {
    Fun.logd("PlayerService.onDestroy()");
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
    Fun.logd("Player started");
  }
  
  // -- MediaPlayer.OnErrorListener
  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Fun.logd("MediaPlayer Error: " + what + "; " + extra);
    return true;
  }
  
  // -- MediaPlayer.OnCompletionListener
  @Override
  public void onCompletion(MediaPlayer mp) {
    Fun.logd("MediaPlayer.onCompletion()");
    mediaPlayer.stop();
    mediaPlayer.reset();
    playSound();
  }
  
  
  private void playSound() {
    if (filesIter != null && !filesIter.hasNext()) {
      createFilesIterator();
    }
    
    if (filesIter != null) {
      soundPath = filesIter.next();
      Fun.logd("Random audio file: \"" + soundPath + "\"");
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
  
  private void createFilesIterator() {
    if (soundFolderPath != null) {
      Fun.logd("Shuffling the audio files");
      List<String> soundFiles = Fun.getSoundFiles(soundFolderPath);
      if (soundFiles.size() > 0) {
        Collections.shuffle(soundFiles);
        filesIter = soundFiles.stream().iterator();
      }
    }
  }
  
}
