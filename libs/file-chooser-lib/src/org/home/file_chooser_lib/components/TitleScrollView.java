package org.home.file_chooser_lib.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class TitleScrollView extends HorizontalScrollView {
  
  public TitleScrollView(Context context) {
    this(context, null);
  }
  
  public TitleScrollView(Context context, AttributeSet attrs) {
    super(context, attrs, 0);
  }
  
  @Override
  public void fling(int velocityX) {
    // Disabled fling animation (scroll animation based on touch movement velocity)
  }
  
}
