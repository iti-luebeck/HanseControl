package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.uniluebeck.iti.hanse.hansecontrol.views.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.app.ActionBar;

public class MainScreen extends Activity {
	
//	private MainScreenFragment frag1 = new MainScreenFragment();
//	private MainScreenFragment frag2 = new MainScreenFragment();
	
	private SharedPreferences mPrefs;
	HashMap<Tab, Integer> tabIDs = new HashMap<Tab, Integer>();
	
	public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		
//		if (executorService.isShutdown()) {
//			executorService = Executors.newScheduledThreadPool(2);
//		}
		
		mPrefs = getSharedPreferences("pref", 0);
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		
		
		
//		Tab tab = actionBar.newTab().setText("Testtab 1").setTabListener(
//				new TabListener<MainScreenFragment>(this, "testtag1", MainScreenFragment.class, 1));
//		actionBar.addTab(tab);
//		
////		tab = actionBar.newTab().setText("Testtab 2").setTabListener(
////				new TabListener<MapWidgetFragment>(this, "testtag2", MapWidgetFragment.class));
////		actionBar.addTab(tab);
//		
//		tab = actionBar.newTab().setText("Testtab 3").setTabListener(
//				new TabListener<MainScreenFragment>(this, "testtag3", MainScreenFragment.class, 2));
//		actionBar.addTab(tab);
		
		//restore tabs
		
		String openTabs = mPrefs.getString("global_opentabs", "");
		if (!openTabs.isEmpty()) {
			try {
				int selectedTab = mPrefs.getInt("global_selectedTab", 0);
				String[] d = openTabs.split(",");
				for (int i = 0; i < d.length; i++) {
					//TODO names for tabs
					int id = Integer.parseInt(d[i]);
					Tab tab = actionBar.newTab().setText("Tab " + id).setTabListener(
					new TabListener<MainScreenFragment>(this, "tag" + id, MainScreenFragment.class, id));
					actionBar.addTab(tab);
					tabIDs.put(tab, id);
					
					if (selectedTab == id) {
						actionBar.selectTab(tab);
					}
				}
			} catch (Exception e) {
				Log.e("encoding", "Error while trying to decode opentabs!");
			}
		}
		if (actionBar.getTabCount() == 0) {
			addNewTab();
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("actionbar", "MainScreen: Menu action activated! '" + item.getTitle() + "'");
		
		switch(item.getItemId()) {
			case R.id.tabCreate:
				addNewTab();
				return true;
			case R.id.tabClose:
				if (getActionBar().getSelectedTab() != null) {
					closeTab(getActionBar().getSelectedTab());
				}
				return false; //tabClose must also be received by the fragment to prevent it from saving the state
			case R.id.resetsettings:
				SharedPreferences.Editor ed = mPrefs.edit();
				ed.clear();
				ed.commit();
				for (int i = getActionBar().getTabCount() - 1; i >= 0; i--) {
					closeTab(getActionBar().getTabAt(i));
				}
				return false; 
		}
		return false;
	}
	
	public void addNewTab() {
		ActionBar actionBar = getActionBar();
		//find lowest free id (starting at 1)
		int id = 1;
		while (tabIDs.values().contains(id)) {
			id++;
		}
		//create new tab
		Tab tab = actionBar.newTab().setText("Tab " + id).setTabListener(
		new TabListener<MainScreenFragment>(this, "tag" + id, MainScreenFragment.class, id));
		actionBar.addTab(tab);
		tabIDs.put(tab, id);
		
		//select new tab
		actionBar.selectTab(tab);
	}
	
	public boolean closeTab(int tabID) {
		for (Tab t : tabIDs.keySet()) {
			if (tabIDs.get(t) == tabID) {
				getActionBar().removeTab(t);
				tabIDs.remove(t);
				clearTabPrefs(tabID);
				if (getActionBar().getTabCount() == 0) {
					addNewTab();
				}
				return true;
			}
		}
		return false;
	}
	
	private void clearTabPrefs(int tabID) {
		//find related entries
		List<String> keysToDelete = new LinkedList<String>();
		for (String key : mPrefs.getAll().keySet())	{
			if (key.startsWith(MainScreenFragment.TAB_PREFIX + tabID)) {
				keysToDelete.add(key);
				Log.d("statemanagement", "Deleting key: " + key);
			}
		}
		SharedPreferences.Editor ed = mPrefs.edit();
		for (String key : keysToDelete) {
			ed.remove(key);
		}
		ed.commit();
	}
	
	public void closeTab(Tab tab) {
		int tabID = tabIDs.get(tab);
		getActionBar().removeTab(tab);
		tabIDs.remove(tab);
		clearTabPrefs(tabID);
		
		if (getActionBar().getTabCount() == 0) {
			addNewTab();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		ActionBar actionBar = getActionBar();
		String openTabs = "";
		//save tabs
		for (int i = 0; i < actionBar.getTabCount(); i++) {
			int tabID = tabIDs.get(actionBar.getTabAt(i));
			openTabs += tabID;
			if (i != actionBar.getTabCount()-1) {
				openTabs += ",";
			}
		}
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString("global_opentabs", openTabs);
		if (actionBar.getSelectedTab() != null) {
			//should never be null though
			ed.putInt("global_selectedTab", tabIDs.get(actionBar.getSelectedTab()));
		}
		ed.commit();
	}

//	@Override
//	public void onTabReselected(Tab tab, FragmentTransaction ft) {
//		Log.d("actionbar", String.format("onTabReselected: tabtext=%s", tab.getText()));
//	}
//
//	@Override
//	public void onTabSelected(Tab tab, FragmentTransaction ft) {
//		Log.d("actionbar", String.format("onTabSelected: tabtext=%s", tab.getText()));
//		//TODO change this, see http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
//		if (tab.getText().equals("Testtab 1")) {
//			ft.attach(frag1);
//		} else {
//			ft.attach(frag2);					
//		}
//	}
//
//	@Override
//	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
//		Log.d("actionbar", String.format("onTabUnselected: tabtext=%s", tab.getText()));
//		if (tab.getText().equals("Testtab 1")) {
//			ft.detach(frag1);
//		} else {
//			ft.detach(frag2);					
//		}
//	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main_screen, menu);
//		return true;
//	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		Log.d("actionbar", "MainScreen: Menu action activated! '" + item.getTitle() + "'");
//		return false;
//	}
	
	
	
	/*
	 * Modified version of example in http://developer.android.com/guide/topics/ui/actionbar.html#Tabs
	 */
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;
	    
	    private int tabID;
	    
	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(Activity activity, String tag, Class<T> clz, int tabID) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	        this.tabID = tabID;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	        	Bundle bundle = new Bundle();
	        	bundle.putInt("tabid", tabID);
	            mFragment = Fragment.instantiate(mActivity, mClass.getName(), bundle);
	            ft.add(android.R.id.content, mFragment, mTag);
	            
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("statemanagement", "MainScreen.onResume");
	}
	
//	@Override
//	protected void onStop() {
//		super.onStop();
//		executorService.shutdown();
//	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
	}
}
