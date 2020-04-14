package org.home.file_chooser_lib;

import android.content.Context;


public class FilePickerDialog extends PickerDialog {
  
  public FilePickerDialog(Context context) {
    super(context, true);
  }
  
  
  public void setExtensionFilter(String[] exts) {
    fileFilter = (dirName, fileName) -> {
      for (String ext: exts) {
        if (fileName.toLowerCase().endsWith(ext)) return true;
      }
      return false;
    };
  }
  
}
