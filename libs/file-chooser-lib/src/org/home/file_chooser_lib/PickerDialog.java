package org.home.file_chooser_lib;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PickerDialog {
  
  protected static final int DIALOG_LAYOUT = R.layout.dialog_picker;
  protected static final int ITEM_LAYOUT = R.layout.dialog_list_item;
  protected static final String PARENT_DIR = "..";
  
  protected Context context;
  protected Dialog dialog;
  protected FragmentManager fragmentManager;
  protected FileSelectedListener fileSelectedListener;
  
  protected HorizontalScrollView titleScroller;
  protected TextView dialogTitle;
  protected RecyclerView listItems;
  protected LinearLayoutManager listLayoutManager;
  
  protected FilesAdapter filesAdapter;
  protected List<ListItem> fileList;
  
  protected File currentPath;
  protected int scrollPos;
  protected int titleViewHeight;
  
  protected boolean isFilePicker;
  protected boolean showFiles;
  protected FilenameFilter fileFilter;
  
  
  public interface FileSelectedListener {
    void fileSelected(File file);
  }
  
  
  public PickerDialog() {}
  
  public PickerDialog(Context context, boolean isFilePicker) {
    this(context, null, isFilePicker);
  }
  
  public PickerDialog(Context context, String startPath, boolean isFilePicker) {
    this.isFilePicker = isFilePicker;
    this.context = context;
    Fun.setContext(context);
    
    dialog = new Dialog(context, R.style.DialogTheme_FullScreen);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    Window window = dialog.getWindow();
    dialog.setContentView(DIALOG_LAYOUT);
    
    window.setWindowAnimations(0);
    window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    
    configUI();
    refresh(startPath);
  }
  
  
  protected void configUI() {
    titleScroller = dialog.findViewById(R.id.titleScroller);
    dialogTitle = dialog.findViewById(R.id.dialogTitle);
    listItems = dialog.findViewById(R.id.listItems);
    
    titleScroller.setSmoothScrollingEnabled(false);
    
    fileList = new ArrayList<>();
    filesAdapter = new FilesAdapter(fileList);
    listItems.setAdapter(filesAdapter);
    
    listLayoutManager = new LinearLayoutManager(context);
    listItems.setLayoutManager(listLayoutManager);
    
    titleViewHeight = Fun.measureViewHeight(dialogTitle);
    
    if (isFilePicker) {
      View actionBar = dialog.findViewById(R.id.actionBar);
      actionBar.setVisibility(View.GONE);
    }
  }
  
  
  // ---------------------- Getters ----------------------
  
  public PickerDialog setFileSelectedListener(FileSelectedListener fileSelectedListener) {
    this.fileSelectedListener = fileSelectedListener;
    return this;
  }
  
  public void setFragmentManager(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  
  public void showDialog() {
    refresh(Environment.getExternalStorageDirectory());
    dialog.show();
  }
  
  public void showDialog(String startPath) {
    refresh(startPath);
    dialog.show();
  }
  
  
  protected void refresh(String path) {
    File file = (path == null) ? Environment.getExternalStorageDirectory(): new File(path);
    refresh(file);
  }
  
  protected void refresh(File path) {
    refresh(path, false);
  }
  
  protected void refresh(File path, boolean parentClicked) {
    if (!path.exists()) path = Environment.getExternalStorageDirectory();
    String exitFolder = null;
    if (parentClicked) exitFolder = currentPath.getName();
    this.currentPath = path;
    
    File[] dirs = path.listFiles(Fun.dirFilter);
    if (dirs == null) dirs = new File[0];
    
    fileList.clear();
    if (path.getParentFile() != null) {
      fileList.add(new ListItem(PARENT_DIR, true));
    }
    
    Arrays.sort(dirs, Fun.nocaseComp);
    
    for (File dir: dirs) {
      String name = dir.getName();
      if (parentClicked && name != null && name.equals(exitFolder)) {
        scrollPos = fileList.size();
        parentClicked = false;
      }
      fileList.add(new ListItem(name));
    }
    
    dialogTitle.setText(currentPath.getPath());
    titleScroller.fullScroll(View.FOCUS_LEFT);
    
    if (isFilePicker || showFiles) {
      File[] files = path.listFiles(fileFilter);
      if (files == null) files = new File[0];
      Arrays.sort(files, Fun.nocaseComp);
      for (File file: files) {
        fileList.add(new ListItem(file.getName(), file.getAbsolutePath()));
      }
    }
    
    filesAdapter.notifyDataSetChanged();
    listItems.post(() -> {
      int lastPos = listLayoutManager.findLastCompletelyVisibleItemPosition();
      int mode = View.OVER_SCROLL_IF_CONTENT_SCROLLS;
      if (lastPos == filesAdapter.getItemCount()-1) {
        mode = View.OVER_SCROLL_NEVER;
      }
      listItems.setOverScrollMode(mode);
    });
    listLayoutManager.scrollToPositionWithOffset(0, 0);
  }
  
  protected File getChosenFile(String fileChosen) {
    if (fileChosen.equals(PARENT_DIR)) {
      return currentPath.getParentFile();
    }
    return new File(currentPath, fileChosen);
  }
  
  
  private void itemClick(ListItem item) {
    try {
      String fileChosen = item.text;
      File chosenFile = getChosenFile(fileChosen);
      
      if (chosenFile.isDirectory()) {
        boolean parentClicked = fileChosen.equals(PARENT_DIR);
        refresh(chosenFile, parentClicked);
        
        if (parentClicked) {
          int itemHeight = filesAdapter.itemViewHeight;
          int dialogH = dialog.getWindow().getDecorView().getHeight();
          int listAreaH = dialogH - titleViewHeight;
          
          int visibleRows = listAreaH / itemHeight;
          if (scrollPos > visibleRows) {
            listLayoutManager.scrollToPositionWithOffset(scrollPos, 0);
          }
        }
      }
      else if (isFilePicker) {
        if (fileSelectedListener != null) {
          fileSelectedListener.fileSelected(chosenFile);
        }
        dialog.dismiss();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  protected int getFileIconByType(String path) {
    int result = R.drawable.ic_insert_drive_file_black_24dp;
    for (String ext: Vars.AUDIO_EXTS) {
      if (path.toLowerCase().endsWith(ext)) {
        return R.drawable.ic_volume_up_black_24dp;
      }
    }
    return result;
  }
  
  
  // ---------------------- Classes ----------------------
  
  protected class ListItem {
    int imgId;
    String text;
    String path;
    boolean isFile;
    boolean isControl;
    
    ListItem(String text) {
      this(text, null, false);
    }
    
    ListItem(String text, boolean isFile) {
      this(text, null, isFile);
    }
    
    ListItem(String text, String path) {
      this(text, path, true);
    }
    
    ListItem(String text, String path, boolean isFile) {
      this.text = text;
      this.path = path;
      this.isFile = isFile;
      
      isControl = isFile && path == null;
      
      if (isFile && path != null) {
        imgId = getFileIconByType(path);
      }
      else if (!isFile) {
        imgId = R.drawable.ic_folder_black_36dp;
      }
    }
  }
  
  
  protected class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ItemViewHolder> {
    public List<ListItem> fileList;
    public int itemViewHeight;
    
    public FilesAdapter(List<ListItem> fileList) {
      this.fileList = fileList;
    }
    
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      Context context = parent.getContext();
      LayoutInflater inflater = LayoutInflater.from(context);
      
      View rootView = inflater.inflate(ITEM_LAYOUT, parent, false);
      
      ItemViewHolder viewHolder = new ItemViewHolder(rootView);
      return viewHolder;
    }
    
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
      ListItem item = fileList.get(position);
      holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
      return fileList.size();
    }
    
    
    public class ItemViewHolder extends RecyclerView.ViewHolder {
      ImageView itemIcon;
      TextView itemText;
      LinearLayout iconContainer;
      
      ListItem item;
      
      public ItemViewHolder(View rootView) {
        super(rootView);
        
        itemIcon = rootView.findViewById(R.id.itemIcon);
        itemText = rootView.findViewById(R.id.itemText);
        iconContainer = rootView.findViewById(R.id.iconContainer);
        
        itemView.setOnTouchListener((view, event) -> {
          if (item == null) return false;

          int action = event.getAction();
          if (action == MotionEvent.ACTION_DOWN) {
            view.setPressed(true);
          }
          else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            view.setPressed(false);
            if (action == MotionEvent.ACTION_UP) itemClick(item);
          }
          
          return true;
        });
      }
      
      public void bind(ListItem item) {
        this.item = item;
        if (item == null) return;
        
        itemIcon.setImageResource(item.imgId);
        itemText.setText(item.text);
        
        if (itemViewHeight == 0) {
          itemViewHeight = Fun.measureViewHeight(itemView);
        }
      }
    } // ItemViewHolder
  }
  
}
