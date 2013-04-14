package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
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

/**
 * This is the main Activity, it shows tabs (Fragments),
 * which are instances of MainScreenFragment.
 * 
 * @author Stefan Hueske
 */
public class MainScreen extends Activity {
	
	//persistent app settings (eg. current Tabs, open widgets, map position and zoom, ...)
	private SharedPreferences mPrefs;
	
	//Map of all currently open tabs and their IDs
	HashMap<Tab, Integer> tabIDs = new HashMap<Tab, Integer>();
	
	//global executor service which should be used to schedule tasks at any location in this Activity (and MainScreenFragment Tabs)
	public static ScheduledExecutorService executorService;
	//TODO make sure to shutdown executorService when the app stops
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set predefined XML layout
		setContentView(R.layout.main_screen);
		
		//start executor service
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newScheduledThreadPool(2);
		}
		
		//load shared preferences
		mPrefs = getSharedPreferences("pref", 0);
		
		//init action bar
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		
		
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
		
		//at least one tab must always be open
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
				
				//at least one tab must always be open
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
	
	/*
	 * Modified version of example in http://developer.android.com/guide/topics/ui/actionbar.html#Tabs
	 * Manages attaching and detaching of Tabs (MainScreenFragments instances)
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
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//do nothing here, preventing android from saving the state (this is done in onPause())
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		executorService.shutdownNow();
	}
}
