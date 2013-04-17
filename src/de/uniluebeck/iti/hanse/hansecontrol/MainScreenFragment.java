package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;

import org.ros.node.ConnectedNode;

import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.MapLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosTextWidget;
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
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * This class represents a tab in the main activity (MainScreen)
 * 
 * @author Stefan Hueske
 */
public class MainScreenFragment extends Fragment {
	
	//widget bar visibility and positions
	private boolean widgetbar_isvisible = true;
	private RelativeLayout.LayoutParams widgetbar_isvisible_true_params;
	private RelativeLayout.LayoutParams widgetbar_isvisible_false_params;
	
	//persistent app settings (eg. current Tabs, open widgets, map position and zoom, ...)
	private SharedPreferences mPrefs;
	
	//registry in which all widget instances will be created
	MapWidgetRegistry widgetRegistry;	
	
	//Layer on which the full-size widgets will be shown
	WidgetLayer widgetLayer;
	
	//Layer on which the icon-size widgets will be shown
	LinearLayout widgetbarLayout;
	
	//tab ID of this Tab
	private int tabID = -1;
	
	//prefix of all tabs, used when saving preferences in mPref
	public static final String TAB_PREFIX = "MainScreenFragment-";
	
	//when true this prevents saving the state of this tab
	//this is used when the user hits "close tab"
	boolean dontSaveStateFlag = false;
	
	//the menu of the action bar, used in setWidgetBarVisibility() to change text and icons
	private Menu actionBarMenu;
	
	//map of menu item to MapManager.Map
	HashMap<MenuItem, Map> maps = new HashMap<MenuItem, Map>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreate() called.");
		super.onCreate(savedInstanceState);
		mPrefs = getActivity().getSharedPreferences("pref", 0);
		if (getArguments() != null) {
			tabID = getArguments().getInt("tabid", -1);
		}
		if (tabID == -1) {
			Log.e("MainScreenFragment", "Tab ID is -1");
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
		//load MapLayer preferences (map position and zoom)
		((MapLayer)getView().findViewById(R.id.mapLayer1)).loadPrefs(TAB_PREFIX + tabID, mPrefs);
	}
	
	public void setNode(ConnectedNode node) {
		widgetRegistry.setNode(node);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d("statemanagement", "MainScreenFragment.onViewCreated() called.");
		super.onViewCreated(view, savedInstanceState);
				
		widgetLayer = (WidgetLayer) view.findViewById(R.id.widgetLayer);
		widgetbarLayout = (LinearLayout) view.findViewById(R.id.widgetLayout);	
		final HorizontalScrollView widgetbarLayoutScroll = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView1);
		final DragLayer dragLayer = (DragLayer) view.findViewById(R.id.dragLayer1);
		
		widgetRegistry = new MapWidgetRegistry(view.getContext(), dragLayer, mPrefs);
		((MainScreen)getActivity()).addNodeConnectedListener(new MainScreen.NodeConnectedListener() {
			
			@Override
			public void onNodeConnected(ConnectedNode node) {
				widgetRegistry.setNode(node);				
			}
		});
		
		//add widgets
		for (int i = 0; i < widgetRegistry.getAllWidgets().size(); i++) {
			MapWidget w = widgetRegistry.getAllWidgets().get(i);
//			Log.d("ttt1load", TAB_PREFIX + tabID + MapWidget.WIDGET_PREFIX+i+"-currentMode");
			int mode = -1;
			if (w instanceof RosMapWidget) {
				RosMapWidget rw = (RosMapWidget) w;
				mode = mPrefs.getInt(TAB_PREFIX + tabID + MapWidget.WIDGET_PREFIX+rw.getRosTopic()+rw.getWidgetType().name()+"-currentMode", MapWidget.ICON_MODE);
			} else {
				mode = mPrefs.getInt(TAB_PREFIX + tabID + MapWidget.WIDGET_PREFIX+i+"-currentMode", MapWidget.ICON_MODE);
			}
			switch(mode) {
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
		
		//setup add widget button
		Button addWidgetButton = new Button(view.getContext());
		addWidgetButton.setText("+");
		widgetbarLayout.addView(addWidgetButton);
		addWidgetButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				opendialog
			}
		});
		
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
        
        //create maps menu        
        MenuItem mapMenu = actionBarMenu.findItem(R.id.mapmenu);
        
        for (Map map : MapManager.getInstance().getMaps()) {
        	MenuItem mapItem = mapMenu.getSubMenu().add(map.getName()).setCheckable(true);
        	if (map.getConfigPath().equals(mPrefs.getString(TAB_PREFIX + tabID
        			+ MapLayer.MAP_LAYER_PREFIX + "-currentmap", ""))) {
        		 mapItem.setChecked(true);
        	}
        	maps.put(mapItem, map);
        }
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
		
		Map map = maps.get(item);
		if (map != null) {
			for (MenuItem i : maps.keySet()) {
				i.setChecked(false);
			}
			item.setChecked(true);
			MapManager.getInstance().recycleAllMapImages();
			((MapLayer) getView().findViewById(R.id.mapLayer1)).setMap(map);
			return true;
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
		//do nothing here, preventing android from saving the state (this is done in onPause())
	}
}
