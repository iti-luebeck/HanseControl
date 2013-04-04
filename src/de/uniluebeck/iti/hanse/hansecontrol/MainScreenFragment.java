package de.uniluebeck.iti.hanse.hansecontrol;

import de.uniluebeck.iti.hanse.hansecontrol.views.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidgetRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.views.WidgetLayer;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainScreenFragment extends Fragment {
	
	private boolean widgetbar_isvisible = true;
	private RelativeLayout.LayoutParams widgetbar_isvisible_true_params;
	private RelativeLayout.LayoutParams widgetbar_isvisible_false_params;
	
	private SharedPreferences mPrefs;
	MapWidgetRegistry widgetRegistry;	
	
	WidgetLayer widgetLayer;
	LinearLayout widgetbarLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment.onCreate() called.");
		super.onCreate(savedInstanceState);
		mPrefs = getActivity().getSharedPreferences("pref", 0);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment.onCreateView() called.");
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.main_screen_fragment, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment.onViewCreated() called.");
		super.onViewCreated(view, savedInstanceState);
		
		
		widgetLayer = (WidgetLayer) view.findViewById(R.id.widgetLayer);
		widgetbarLayout = (LinearLayout) view.findViewById(R.id.widgetLayout);	
		final HorizontalScrollView widgetbarLayoutScroll = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView1);
		final DragLayer dragLayer = (DragLayer) view.findViewById(R.id.dragLayer1);
		
		widgetRegistry = new MapWidgetRegistry(view.getContext(), dragLayer);
		
		//add widgets
		for (int i = 0; i < widgetRegistry.getAllWidgets().size(); i++) {
			MapWidget w = widgetRegistry.getAllWidgets().get(i);
			Log.d("ttt1load", MapWidget.PREF_PREFIX+i+"-currentMode");
			switch(mPrefs.getInt(MapWidget.PREF_PREFIX+i+"-currentMode", MapWidget.ICON_MODE)) {
				case MapWidget.ICON_MODE:
					widgetbarLayout.addView(w);
					break;
				case MapWidget.FULLSIZE_MODE:
					widgetLayer.addView(w);
					break;
			}
			//loading of layout params must take place after adding widget to a layout
			w.loadPrefs(mPrefs);			
		}
		
		//init widget bar params
		widgetbar_isvisible_true_params = new RelativeLayout.LayoutParams(widgetbarLayoutScroll.getLayoutParams());
		widgetbar_isvisible_true_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		widgetbar_isvisible_false_params = new RelativeLayout.LayoutParams(widgetbar_isvisible_true_params);
		widgetbar_isvisible_false_params.height = 0;
		
		
		
		
//		//create colored test widgets
//		int hueSteps = 30;
//		float[] hsv = new float[] {0,1,1};
//		int color = Color.HSVToColor(hsv);
//		
//		for (int i = 0; i < 20; i++) {
//			MapWidget widget = new MapWidget(view.getContext());
//			widget.getDebugPaint().setColor(color);
//			//assign unique widget ID
//			widget.setId(i); //TODO widget Registry class?
//			
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
//			params.setMargins(5, 0, 5, 8);
//			widget.setLayoutParams(params);
//			widget.setDragLayer(dragLayer);
//			widgetLayout.addView(widget);
//			
//			hsv[0] = (hsv[0] + hueSteps) % 360;
//			color = Color.HSVToColor(hsv);
//		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_screen, menu);
        super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("actionbar", "MainScreenFragment: Menu action activated! '" + item.getTitle() + "'");
		
		switch(item.getItemId()) {
			case R.id.showhidewidgetbar:
				if (widgetbar_isvisible) {
					item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_show));
				} else {
					item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_hide));
				}
				setWidgetBarVisibility(!widgetbar_isvisible);
				widgetbar_isvisible = !widgetbar_isvisible;
				return true;
			case R.id.resetsettings:
				SharedPreferences.Editor ed = mPrefs.edit();
				ed.clear();
				ed.commit();
				for (MapWidget w : widgetRegistry.getAllWidgets()) {
					if (w.getMode() == MapWidget.FULLSIZE_MODE) {
						widgetLayer.removeView(w);
						widgetbarLayout.addView(w, w.getWidgetID());
						w.setMode(MapWidget.ICON_MODE);
					}					
				}
		}
		return false;
	}
	
	@Override
	public void onPause() {
		Log.d("statemanagement", "MainScreenFragment.onPause() called.");
		super.onPause();		
		SharedPreferences.Editor ed = mPrefs.edit();
		for (MapWidget w : widgetRegistry.getAllWidgets()) {
			w.savePrefs(ed);
		}
		ed.commit();
	}
	
	public void setWidgetBarVisibility(boolean isvisible) {
		HorizontalScrollView widgetLayoutScroll = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView1);
		if (isvisible) {
			widgetLayoutScroll.setLayoutParams(widgetbar_isvisible_true_params);
		} else {
			widgetLayoutScroll.setLayoutParams(widgetbar_isvisible_false_params);
		}
	}	
	
}
