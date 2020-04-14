package org.home.file_chooser_lib;

import android.content.Context;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


public class DirectoryPickerDialog extends PickerDialog {
  
  private boolean addFolderEnabled;
  
  
  public DirectoryPickerDialog(Context context) {
    this(context, false);
  }
  
  public DirectoryPickerDialog(Context context, boolean showFiles) {
    super(context, false);
    this.showFiles = showFiles;
  }
  
  
  public void setAddFolderEnabled(boolean addFolderEnabled) {
    this.addFolderEnabled = addFolderEnabled;
  }
  
  @Override
  protected void configUI() {
    super.configUI();
    
    if (!addFolderEnabled) {
      LinearLayout actionPanel1 = dialog.findViewById(R.id.actionPanel1);
      actionPanel1.setVisibility(View.GONE);
    }
    else {
      Button bAddFolder = dialog.findViewById(R.id.bAddFolder);
      bAddFolder.setOnClickListener(v -> {
      });
    }
    
    Button bSelect = dialog.findViewById(R.id.bSelect);
    bSelect.setOnClickListener(v -> {
      if (fileSelectedListener != null) {
        fileSelectedListener.fileSelected(currentPath);
      }
      dialog.dismiss();
    });
  }
  
}
