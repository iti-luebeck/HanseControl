package de.uniluebeck.iti.hanse.hansecontrol;

import de.uniluebeck.iti.hanse.hansecontrol.map.MapLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidgetRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.views.WidgetLayer;
import android.app.ActionBar;
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
import android.view.ViewTreeObserver;
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
		
	private int tabID = -1;
	public static final String TAB_PREFIX = "MainScreenFragment-";
	
	boolean dontSaveStateFlag = false;
	
	private Menu actionBarMenu;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreate() called.");
		super.onCreate(savedInstanceState);
		mPrefs = getActivity().getSharedPreferences("pref", 0);
		if (getArguments() != null) {
			tabID = getArguments().getInt("tabid", -1);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreateView() called.");
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.main_screen_fragment, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onResume");
		setWidgetBarVisibility(mPrefs.getBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", true));
		//setup MapLayer prefs
		((MapLayer)getView().findViewById(R.id.mapLayer1)).loadPrefs(TAB_PREFIX + tabID, mPrefs);
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
//			Log.d("ttt1load", TAB_PREFIX + tabID + MapWidget.WIDGET_PREFIX+i+"-currentMode");
			switch(mPrefs.getInt(TAB_PREFIX + tabID + MapWidget.WIDGET_PREFIX+i+"-currentMode", MapWidget.ICON_MODE)) {
				case MapWidget.ICON_MODE:
					widgetbarLayout.addView(w);
					break;
				case MapWidget.FULLSIZE_MODE:
					widgetLayer.addView(w);
					break;
			}
			//loading of layout params must take place after adding widget to a layout
			w.loadPrefs(TAB_PREFIX + tabID, mPrefs);			
		}
		
		//init widget bar params
		widgetbar_isvisible_true_params = new RelativeLayout.LayoutParams(widgetbarLayoutScroll.getLayoutParams());
		widgetbar_isvisible_true_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		widgetbar_isvisible_false_params = new RelativeLayout.LayoutParams(widgetbar_isvisible_true_params);
		widgetbar_isvisible_false_params.height = 0;
		
		widgetbar_isvisible = mPrefs.getBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", true);
		
//		if (actionBarMenu != null) {
//			MenuItem item = actionBarMenu.findItem(R.id.showhidewidgetbar);
//			if (widgetbar_isvisible) {
//				item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_hide));
//			} else {
//				item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_show));
//			}
//		}
		
//		View dragview = getView().findViewById(R.id.dragLayer1);
//		dragview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//			@Override
//			public void onGlobalLayout() {
//				// TODO Auto-generated method stub
//				setWidgetBarVisibility(mPrefs.getBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", true));
//			}
//		});
		
		
		
		
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
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_screen, menu);
    	actionBarMenu = menu;
    	MenuItem item = actionBarMenu.findItem(R.id.showhidewidgetbar);
		if (widgetbar_isvisible) {
			item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_hide));
		} else {
			item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_show));
		}
	    
        Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreateOptionsMenu");
//        setWidgetBarVisibility(mPrefs.getBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", true));
        
//        try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		setWidgetBarVisibility(mPrefs.getBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", true));
	}
	
//	public void externalOnViewCreated
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("actionbar", "MainScreenFragment" + tabID + ": Menu action activated! s'" + item.getTitle() + "'");
		
		switch(item.getItemId()) {
			case R.id.showhidewidgetbar:
				setWidgetBarVisibility(!widgetbar_isvisible);
				widgetbar_isvisible = !widgetbar_isvisible;
				return true;
			case R.id.resetsettings:
			case R.id.tabClose:
				dontSaveStateFlag = true;
				return false;
		}
		return false;
	}
		
		
	@Override
	public void onPause() {
		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onPause() called.");
		super.onPause();		
		if (dontSaveStateFlag) {
			Log.d("statemanagement", "MainScreenFragment" + tabID + ".onPause(): dontSaveStateFlag active");
			return;
		}
		SharedPreferences.Editor ed = mPrefs.edit();
		for (MapWidget w : widgetRegistry.getAllWidgets()) {
			w.savePrefs(TAB_PREFIX + tabID, ed);
		}
		ed.putBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", widgetbar_isvisible);
		((MapLayer)getView().findViewById(R.id.mapLayer1)).savePrefs(TAB_PREFIX + tabID, ed);
		ed.commit();
	}
	
	public void setWidgetBarVisibility(boolean isvisible) {
		HorizontalScrollView widgetLayoutScroll = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView1);
		Log.d("statemanagement", "MainScreenFragment" + tabID + ", actionBarMenu is null = " + (actionBarMenu == null));
		Log.d("statemanagement", "MainScreenFragment" + tabID + ", widgetandayoutScroll is null = " + (widgetLayoutScroll == null));
		
//		Menu menu = (Menu) getActivity().findViewById(R.menu.main_screen);
//		Log.d("errfind", "menu is null = " + (menu == null));
		if (actionBarMenu != null) {
			//dirty, workaround for startup problem
			MenuItem item = actionBarMenu.findItem(R.id.showhidewidgetbar);
			if (isvisible) {
				item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_hide));
			} else {
				item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_show));
			}
		}
		if (isvisible) {
			widgetLayoutScroll.setLayoutParams(widgetbar_isvisible_true_params);
		} else {
			widgetLayoutScroll.setLayoutParams(widgetbar_isvisible_false_params);
		}
	}

	public int getTabID() {
		return tabID;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
	}
}
