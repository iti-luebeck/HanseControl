package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.smtp.RelayPath;
import org.ros.node.ConnectedNode;

import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.OverlayRegistry.OverlayType;
import de.uniluebeck.iti.hanse.hansecontrol.mapeditor.MapEditor;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.MapLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.OverlayLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.PathLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.AbstractOverlay;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosTextWidget;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

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
	
	OverlayLayer overlayLayer;
	MapLayer mapLayer;
	PathLayer pathLayer;
	
	//tab ID of this Tab
	private int tabID = -1;
	
	//prefix of all tabs, used when saving preferences in mPref
	public static final String TAB_PREFIX = "MainScreenFragment-";
	
	//when true this prevents saving the state of this tab
	//this is used when the user hits "close tab"
	boolean dontSaveStateFlag = false;
	
	//the menu of the action bar, used in setWidgetBarVisibility() to change text and icons
	private Menu actionBarMenu;
	
	//map: menu item --> MapManager.Map
	HashMap<MenuItem, Map> maps = new HashMap<MenuItem, Map>();
	
	//map: menu item --> AbstractOverlay
	HashMap<MenuItem, AbstractOverlay> overlaysMap = new HashMap<MenuItem, AbstractOverlay>();
	MenuItem addNewOverlayMenuItem;
	MenuItem removeOverlayMenuItem;
	MenuItem overlayMenu;
	
	PostConfigurationListener postConfigurationListener = null;
	
	//request code to associate a response with its request
	public static final int MAP_EDITOR_REQUEST = 1;
	
	public static final String MAP_TO_EDIT_MESSAGE = "de.uniluebeck.iti.hanse.hansecontrol.MAP_TO_EDIT_MESSAGE";
	
	MenuItem editCurrentMapMenuItem;
	MenuItem addNewMapMenuItem;
	MenuItem removeMapMenuItem;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreate() called.");
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
//		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreateView() called.");
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.main_screen_fragment, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
//		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onResume");
		setWidgetBarVisibility(mPrefs.getBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", true));
		//load MapLayer preferences (map position and zoom)
		((MapLayer)getView().findViewById(R.id.mapLayer1)).loadPrefs(TAB_PREFIX + tabID, mPrefs);
	}
	
	public void setNode(ConnectedNode node) {
		widgetRegistry.setNode(node);
		overlayLayer.getOverlayRegistry().setNode(node);
		RosRobot.getInstance().setNode(node);
		pathLayer.setNode(node);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
//		Log.d("statemanagement", "MainScreenFragment.onViewCreated() called.");
		super.onViewCreated(view, savedInstanceState);
				
		widgetLayer = (WidgetLayer) view.findViewById(R.id.widgetLayer);
		widgetbarLayout = (LinearLayout) view.findViewById(R.id.widgetLayout);	
		((HorizontalScrollView) view.findViewById(R.id.widgetScrollView)).setBackgroundColor(Color.parseColor("#7907121d"));
		overlayLayer = (OverlayLayer) view.findViewById(R.id.overlayLayer);
//		Log.d("statemanagement", "overLayer is " + (overlayLayer == null ? "NULL" : "not null"));
		mapLayer = (MapLayer) view.findViewById(R.id.mapLayer1);
		mapLayer.setMapLayerListener(new MapLayer.MapLayerListener() {
			
			@Override
			public void onMapSurfaceCreated(MapSurface mapSurface) {
				overlayLayer.getOverlayRegistry().setMapSurface(mapSurface);
				pathLayer.setMapSurface(mapLayer.getMapSurface());
			}
		});
		pathLayer = (PathLayer) view.findViewById(R.id.pathLayer);
		pathLayer.setFragmentManager(getFragmentManager());
		
		for (AbstractOverlay overlay : overlayLayer.getOverlayRegistry().getAllOverlays()) {
			overlay.setMode( mPrefs.getBoolean(TAB_PREFIX + tabID 
					+ overlay.getOverlayType().name() + ":" + overlay.getRosTopic(), true)
					? AbstractOverlay.VISIBLE : AbstractOverlay.INVISIBLE);
		}
		
		final HorizontalScrollView widgetbarLayoutScroll = (HorizontalScrollView) view.findViewById(R.id.widgetScrollView);
		final DragLayer dragLayer = (DragLayer) view.findViewById(R.id.dragLayer1);
		
		//TODO change static topic!
		
		widgetRegistry = new MapWidgetRegistry(view.getContext(), dragLayer, mPrefs, this);
		((MainScreen)getActivity()).addNodeConnectedListener(new MainScreen.NodeConnectedListener() {
			
			@Override
			public void onNodeConnected(ConnectedNode node) {
				setNode(node);
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
		addWidgetButton.setTextSize(30);
		widgetbarLayout.addView(addWidgetButton);
		
		float pixSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
		float pixMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) addWidgetButton.getLayoutParams();
		params.width = (int) pixSize / 2;
		params.height = (int) pixSize;
		params.setMargins((int)pixMargin, 0, (int)pixMargin, (int)pixMargin);
		addWidgetButton.setLayoutParams(params);
		
		addWidgetButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AddWidgetDialog(){
					
					@Override
					public void onAdd(WidgetType widgetType, String topic) {
//						Log.d("addwidget", widgetType.name() + ": " + topic);
						RosMapWidget widget = widgetRegistry.createWidget(widgetType, topic);
						widgetbarLayout.addView(widget, 0);
						widget.setMode(MapWidget.ICON_MODE);
//						widgetbarLayout.invalidate();
					}			
				}.show(getFragmentManager(), "add_widget");
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
		
		widgetLayer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            	if (postConfigurationListener != null) {
        			postConfigurationListener.onPostViewCreated(MainScreenFragment.this);
        			postConfigurationListener = null;
        		}
            }
        });
		
		/*
		 * This is a workaround for the following error:
		 * The order in which onViewCreated() and onCreateOptionsMenu() is called is undetermined.
		 * To create the overlay menu, a instance of overlayLayer is needed.
		 * If actionBarMenu is not null the Menu instance is available, so onViewCreated was called last.
		 */
		if(actionBarMenu != null) {
			initOverlayMenu();
		}
		
		//direct onLongPress from MapLayer to PathLayer
		mapLayer.setOnLongPressListener(new MapLayer.OnLongPressListener() {
			
			@Override
			public void onLongPress(MotionEvent event) {
				pathLayer.onLongPress(event);
			}
		});
	}
	
	public void setPostConfigurationListener(
			PostConfigurationListener postConfigurationListener) {
		this.postConfigurationListener = postConfigurationListener;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        Log.d("statemanagement", "MainScreenFragment" + tabID + ".onCreateOptionsMenu");
        inflater.inflate(R.menu.main_screen, menu);
    	actionBarMenu = menu;
    	MenuItem item = actionBarMenu.findItem(R.id.showhidewidgetbar);
		if (widgetbar_isvisible) {
			item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_hide));
		} else {
			item.setTitle(getResources().getString(R.string.action_showhidewidgetbar_show));
		}
	    
        
        //create maps menu        
        initMapMenu();
        
        /*
		 * This is a workaround for the following error:
		 * The order in which onViewCreated() and onCreateOptionsMenu() is called is undetermined.
		 * To create the overlay menu, a instance of overlayLayer is needed.
		 * If overlayLayer is not null the overlayLayer instance is available, so onCreateOptionsMenu() was called last.
		 */
		if (overlayLayer != null) {
        	initOverlayMenu();
        }
        
	}

	private void initMapMenu() {
		MenuItem mapMenu = actionBarMenu.findItem(R.id.mapmenu);
		
        for (Map map : MapManager.getInstance().getMaps()) {
        	MenuItem mapItem = mapMenu.getSubMenu().add(map.getName()).setCheckable(true);
        	if (map.getConfigPath().equals(mPrefs.getString(TAB_PREFIX + tabID
        			+ MapLayer.MAP_LAYER_PREFIX + "-currentmap", ""))) {
        		 mapItem.setChecked(true);
        	}
        	maps.put(mapItem, map);
        }
        
        editCurrentMapMenuItem = mapMenu.getSubMenu().add("Edit current map...");
        removeMapMenuItem = mapMenu.getSubMenu().add("Remove map...");
        addNewMapMenuItem = mapMenu.getSubMenu().add("Add new map...");
	}

	private void initOverlayMenu() {
		overlayMenu = actionBarMenu.findItem(R.id.overlayMenu);
		
		for (AbstractOverlay overlay : overlayLayer.getOverlayRegistry().getAllOverlays()) {
        	MenuItem overlayItem = overlayMenu.getSubMenu().add(
        			overlay.getOverlayType().name() + ": " + overlay.getRosTopic()).setCheckable(true);
//        	Log.d("menutest", "loading: " + TAB_PREFIX + tabID + overlay.getOverlayType().name() + ":" + overlay.getRosTopic());
//        	Log.d("menutest", "entry exists: " + mPrefs.contains(TAB_PREFIX + tabID + overlay.getOverlayType().name() + ":" + overlay.getRosTopic()));
//        	Log.d("menutest", "entry value: " + mPrefs.getBoolean(TAB_PREFIX + tabID + overlay.getOverlayType().name() + ":" + overlay.getRosTopic(), true));
        	
        	if (overlay.isVisible()) {
        		overlayItem.setChecked(true);
//        		Log.d("menutest", overlayItem.getTitle().toString());
        	} else {
        		overlayItem.setChecked(false);
        	}
        	overlaysMap.put(overlayItem, overlay);
        }
        
        removeOverlayMenuItem = overlayMenu.getSubMenu().add("Remove overlay...");
        addNewOverlayMenuItem = overlayMenu.getSubMenu().add("Add overlay...");
	}
	
//	public void externalOnViewCreated
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		Log.d("actionbar", "MainScreenFragment" + tabID + ": Menu action activated! s'" + item.getTitle() + "'");
		
		switch(item.getItemId()) {
			case R.id.showhidewidgetbar:
				setWidgetBarVisibility(!widgetbar_isvisible);
//				widgetbar_isvisible = !widgetbar_isvisible;
				return true;
			case R.id.resetsettings:
			case R.id.tabClose:
				dontSaveStateFlag = true;
				return false;
		}
		
		if (item == editCurrentMapMenuItem) {
			Intent intent = new Intent(getActivity(), MapEditor.class);
			intent.putExtra(MAP_TO_EDIT_MESSAGE, mapLayer.getMap().getConfigPath());
			startActivityForResult(intent, MAP_EDITOR_REQUEST);
		}
		
		if (item == addNewMapMenuItem) {
			Intent intent = new Intent(getActivity(), MapEditor.class);
			startActivityForResult(intent, MAP_EDITOR_REQUEST);
		}
		
		if (item == addNewOverlayMenuItem) {
			new AddOverlayDialog() {
				@Override
				public void onAdd(OverlayType overlayType, String topic) {
					AbstractOverlay overlay = overlayLayer.getOverlayRegistry().createOverlay(overlayType, topic);
					MenuItem item = overlayMenu.getSubMenu().add(overlayType.name() + ": " + topic).setCheckable(true).setChecked(true);
					overlayLayer.addOverlay(overlay);
					overlaysMap.put(item, overlay);
					overlay.setMode(AbstractOverlay.VISIBLE);
					
					//a bit dirty workaround to position the remove- and add overlay items at be bottom
					removeOverlayMenuItem.setVisible(false);
					removeOverlayMenuItem = overlayMenu.getSubMenu().add("Remove overlay...");
					addNewOverlayMenuItem.setVisible(false);
					addNewOverlayMenuItem = overlayMenu.getSubMenu().add("Add overlay...");
				}
			}.show(getFragmentManager(), "add_layer");
			return true;
		}

		if (item == removeMapMenuItem) {
			showRemoveMapDialog();
			return true;
		}
		
		if (item == removeOverlayMenuItem) {
			showRemoveWidgetLayerDialog();
			return true;
		}
		
		AbstractOverlay overlay = overlaysMap.get(item);
		if (overlay != null) {
			overlay.setMode(overlay.isVisible() ? AbstractOverlay.INVISIBLE : AbstractOverlay.VISIBLE);
			item.setChecked(overlay.isVisible());
			return true;
		}
		
		Map map = maps.get(item);
		if (map != null) {
			for (MenuItem i : maps.keySet()) {
				i.setChecked(false);
			}
			item.setChecked(true);
			MapManager.getInstance().recycleAllMapImages();	
			mapLayer.setMap(map);
			editCurrentMapMenuItem.setVisible(true);
			//if empty map, disable the edit map item
			if (map.getConfigPath().isEmpty()) {
				editCurrentMapMenuItem.setVisible(false);
			}
			return true;
		}
		return false;
	}
	
	private void showRemoveMapDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//empty map cannot be deleted, therefore -1
		List<Map> mapList = MapManager.getInstance().getMaps();
		final CharSequence[] items = new CharSequence[mapList.size() - 1];
		final Map[] mapArr = new Map[items.length];
//		final MenuItem[] menuItems = new MenuItem[items.length];
		final boolean[] itemsChecked = new boolean[items.length]; //default value is false for all entries
		int i = 0;
		for (Map map : mapList) {
//			Map map = maps.get(item);
			if (!map.getConfigPath().isEmpty()) {
				items[i] = map.getName();
				mapArr[i] = map;
//				menuItems[i] = item;
				i += 1;
			}
		}
		builder.setTitle("Remove Map");
		builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itemsChecked[which] = isChecked;
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = items.length - 1; i >= 0; i--) {
					if (itemsChecked[i]) {
						Map mapToDelete = mapArr[i];
						if (mapToDelete.getConfigPath().equals(mapLayer.getMap().getConfigPath())) {
							mapLayer.setMap(MapManager.getInstance().getEmptyMap());
							editCurrentMapMenuItem.setVisible(false);
							for (MenuItem item : maps.keySet()) {
								if (maps.get(item) == MapManager.getInstance().getEmptyMap()) {
									item.setChecked(true);
									break;
								}
							}
						}
						for (MenuItem item : maps.keySet()) { 
//							Log.d("ttttt", maps.get(item).getName());
							if (mapToDelete.getConfigPath().equals(maps.get(item).getConfigPath())) {
//								Log.d("ttttt", "sel " + maps.get(item).getName());
								item.setVisible(false);
								maps.remove(item);
								break;
							}
						}
						MapManager.getInstance().deleteMap(mapToDelete);
					}
				}
			}
		});
		builder.create().show();
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MAP_EDITOR_REQUEST && resultCode == Activity.RESULT_OK) {
			//refresh all items in the maps menu
			for (MenuItem item : maps.keySet()) {
				item.setVisible(false);
			}
			editCurrentMapMenuItem.setVisible(false);
			addNewMapMenuItem.setVisible(false);
			removeMapMenuItem.setVisible(false);
			maps.clear();
			initMapMenu();
			mapLayer.setMap(mapLayer.getMap()); //forces a refresh of the map image
		}
	}
	
	private void showRemoveWidgetLayerDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final CharSequence[] items = new CharSequence[overlayLayer.getOverlayRegistry().getAllOverlays().size()];
		final boolean[] itemsChecked = new boolean[items.length]; //default value is false for all entries
		int i = 0;
		for (AbstractOverlay overlay : overlayLayer.getOverlayRegistry().getAllOverlays()) {
			items[i++] = overlay.getOverlayType().name() + ": " + overlay.getRosTopic();
		}
		builder.setTitle("Remove Overlay");
		builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				itemsChecked[which] = isChecked;
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = items.length - 1; i >= 0; i--) {
					if (itemsChecked[i]) {
						AbstractOverlay overlay = overlayLayer.getOverlayRegistry().getAllOverlays().get(i);
						overlayLayer.deleteOverlay(overlay);
						//remove item from optionsmenu
						MenuItem itemToDelete = null;
						for (MenuItem item : overlaysMap.keySet()) {
							if (overlaysMap.get(item) == overlay) {
								item.setVisible(false);
								itemToDelete = item;
								break;
							}
						}
						overlaysMap.remove(itemToDelete);
					}
				}
				
				
			}
		});
		builder.create().show();
		
	}
	
	@Override
	public void onPause() {
//		Log.d("statemanagement", "MainScreenFragment" + tabID + ".onPause() called.");
		super.onPause();		
		if (dontSaveStateFlag) {
//			Log.d("statemanagement", "MainScreenFragment" + tabID + ".onPause(): dontSaveStateFlag active");
			return;
		}
		SharedPreferences.Editor ed = mPrefs.edit();
		for (MapWidget w : widgetRegistry.getAllWidgets()) {
			w.savePrefs(TAB_PREFIX + tabID, ed);
		}
//		widgetRegistry.savePrefs(ed);
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {	
				widgetRegistry.saveWidgetsToFile();
				widgetRegistry.unsubscribeAll();
				overlayLayer.getOverlayRegistry().saveOverlaysToFile();
				overlayLayer.getOverlayRegistry().unsubscribeAll();
			}
		});
		
		for (AbstractOverlay overlay : overlayLayer.getOverlayRegistry().getAllOverlays()) {
//			Log.d("menutest", "saving: " + TAB_PREFIX + tabID + overlay.getOverlayType().name() + ":" + overlay.getRosTopic() + " value:"+ overlay.isVisible());
			ed.putBoolean(TAB_PREFIX + tabID + overlay.getOverlayType().name() + ":" + overlay.getRosTopic(), overlay.isVisible());
		}
		
		ed.putBoolean(TAB_PREFIX + tabID + "_widgetbarisvisible", widgetbar_isvisible);
		((MapLayer)getView().findViewById(R.id.mapLayer1)).savePrefs(TAB_PREFIX + tabID, ed);
		ed.commit();
	}
	
	public void setWidgetBarVisibility(boolean isvisible) {
		HorizontalScrollView widgetLayoutScroll = (HorizontalScrollView) getView().findViewById(R.id.widgetScrollView);
//		Log.d("statemanagement", "MainScreenFragment" + tabID + ", actionBarMenu is null = " + (actionBarMenu == null));
//		Log.d("statemanagement", "MainScreenFragment" + tabID + ", widgetandayoutScroll is null = " + (widgetLayoutScroll == null));
		
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
		widgetbar_isvisible = isvisible;
	}

	public int getTabID() {
		return tabID;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		//do nothing here, preventing android from saving the state (this is done in onPause())
	}
	
	public abstract static class AddWidgetDialog extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			// Get the layout inflater
		    LayoutInflater inflater = getActivity().getLayoutInflater();
			
//			builder.setMessage("testmessage!");
			
			builder.setTitle(getResources().getString(R.string.add_widget_title));

			final View view = inflater.inflate(R.layout.dialog_addwidget, null);
			builder.setView(view);
	
			//fill spinner with widget types
			ArrayAdapter<WidgetType> adapter = new ArrayAdapter<MapWidgetRegistry.WidgetType>(getActivity(), android.R.layout.simple_spinner_item);
			adapter.addAll(WidgetType.values());
			final Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);	
			final TextView topicTextView = (TextView) view.findViewById(R.id.mapName);
			spinner.setAdapter(adapter);
			
			builder.setNegativeButton(getResources().getString(R.string.add_widget_cancelbutton), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AddWidgetDialog.this.getDialog().cancel();
				}
			});
			
			builder.setPositiveButton(getResources().getString(R.string.add_widget_addbutton), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//TODO refactor IDs
					
					onAdd((WidgetType)spinner.getSelectedItem(), topicTextView.getText().toString());
				}
			});
			
			return builder.create();	
			
		}
		
		public abstract void onAdd(WidgetType widgetType, String topic);
		
//		@Override
//		public void onResume() {
//			super.onResume();
////			super.onViewCreated(view, savedInstanceState);
//			
//			//fill spinner with widget types
//			Spinner spinner = (Spinner) getView().findViewById(R.id.spinner1);
//			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getView().getContext(), android.R.layout.simple_spinner_item, new String[]{"a", "b"});
////			adapter.add("WIDGET_TYPE_1");
////			adapter.add("WIDGET_TYPE_2");
////			adapter.add("WIDGET_TYPE_3");
//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			spinner.setAdapter(adapter);
////			spinner.invalidate();
//		}
	}
	
	public abstract static class AddOverlayDialog extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			
			builder.setTitle(getResources().getString(R.string.add_layer_title));

			final View view = inflater.inflate(R.layout.dialog_addlayer, null);
			builder.setView(view);
	
			//fill spinner with widget types
			ArrayAdapter<OverlayType> adapter = new ArrayAdapter<OverlayType>(getActivity(), 
					android.R.layout.simple_spinner_item);
			adapter.addAll(OverlayType.values());
			final Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);	
			final TextView topicTextView = (TextView) view.findViewById(R.id.mapName);
			spinner.setAdapter(adapter);
			
			builder.setNegativeButton(getResources().getString(R.string.add_widget_cancelbutton), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AddOverlayDialog.this.getDialog().cancel();
				}
			});
			
			builder.setPositiveButton(getResources().getString(R.string.add_widget_addbutton), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					onAdd((OverlayType)spinner.getSelectedItem(), topicTextView.getText().toString());
				}
			});
			
			return builder.create();	
			
		}
		
		public abstract void onAdd(OverlayType overlayType, String topic);
		
	}
	
	public MapWidgetRegistry getWidgetRegistry() {
		return widgetRegistry;
	}
	
	public void closeAllOtherMapWidgets(MapWidget widget) {
		for (int i = widgetLayer.getChildCount() - 1; i >= 0; i--) {
			if (widgetLayer.getChildAt(i) instanceof MapWidget
					&& widgetLayer.getChildAt(i) != widget) {
				widgetLayer.removeWidget((MapWidget)widgetLayer.getChildAt(i));
			}
		}
	}
	
	public void closeMapWidgetAndMoveToNewTab(final RosMapWidget widget) {
		widgetLayer.removeWidget(widget);
		MainScreenFragment newTab = ((MainScreen)getActivity()).addNewTab();
		newTab.setPostConfigurationListener(new PostConfigurationListener() {
			
			@Override
			public void onPostViewCreated(MainScreenFragment mainScreenFragment) {
				mainScreenFragment.openWidgetInFullscreen(widget.getRosTopic(), widget.getWidgetType());
				Map emptyMap = MapManager.getInstance().getEmptyMap();
				((MapLayer) mainScreenFragment.getView().findViewById(R.id.mapLayer1)).setMap(emptyMap);
			}
		});
	}
	
	public void openWidgetInFullscreen(String topic, WidgetType widgetType) {
		RosMapWidget widget = widgetRegistry.getRosMapWidget(topic, widgetType);
//		closeAllOtherMapWidgets(widget);
		if (widget.getParent() != widgetLayer) {
			widgetbarLayout.removeView(widget);
			widgetLayer.addView(widget);
			widget.setMode(MapWidget.FULLSIZE_MODE);
		}
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) widget.getLayoutParams();
		params.width = widgetLayer.getWidth();
		params.height = widgetLayer.getHeight();
		params.topMargin = 0;
		params.leftMargin = 0;
		widget.setLayoutParams(params);
		
		setWidgetBarVisibility(false);
	}
	
	public static interface PostConfigurationListener {
		public void onPostViewCreated(MainScreenFragment mainScreenFragment);
	}
}
