package de.uniluebeck.iti.hanse.hansecontrol.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

public class GuiTools {
	public static int dipToPixels(int dip, Context context) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
	}
	
	public static void addLayoutListener(final View view, final Runnable action) {
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				removeOnGlobalLayoutListener(view, this);
				action.run();
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
	    if (Build.VERSION.SDK_INT < 16) {
	        v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
	    } else {
	        v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
	    }
	}
}
