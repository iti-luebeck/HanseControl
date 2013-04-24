package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment.AddWidgetDialog;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.ActionBar;

/**
 * This is the main Activity, it shows tabs (Fragments),
 * which are instances of MainScreenFragment.
 * 
 * @author Stefan Hueske
 */
public class MainScreen extends RosActivity {
	
	//persistent app settings (eg. current Tabs, open widgets, map position and zoom, ...)
	private SharedPreferences mPrefs;
	
	//Map of all currently open tabs and their IDs
	HashMap<Tab, Integer> tabIDs = new HashMap<Tab, Integer>();
	SparseArray<MainScreenFragment> fragments = new SparseArray<MainScreenFragment>();
	
	//global executor service which should be used to schedule tasks at any location in this Activity (and MainScreenFragment Tabs)
	public static ScheduledExecutorService executorService;
	//TODO make sure to shutdown executorService when the app stops
	
	Node mainNode = new Node();
	ConnectedNode node;
	List<NodeConnectedListener> nodeConnectedListeners = new LinkedList<NodeConnectedListener>();
	
	//workaround: onTabReselected() is called at startup for unknown reason, so rename is started on doubletap
	private static long lastOnTabReselected = 0;
	
	private View activeContextMenuView = null;
	
	public MainScreen() {
		super("HanseControl", "HanseControl");
		//start executor service
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newScheduledThreadPool(2);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set predefined XML layout
		setContentView(R.layout.main_screen);
		
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
					Tab tab = actionBar.newTab().setText(mPrefs.getString(
							MainScreenFragment.TAB_PREFIX + id + "-tabname", "Tab " + id))
							.setTabListener(
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
		Log.d("statemanagement", "MainScreen.onCreate() finished");
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.widget_contextmenu, menu);
	    activeContextMenuView = v;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (activeContextMenuView != null && activeContextMenuView instanceof MapWidget.ShowContextMenuButton) {
			((MapWidget.ShowContextMenuButton) activeContextMenuView).performAction(item);
		}
		return super.onContextItemSelected(item);
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
	
	public MainScreenFragment addNewTab() {
		ActionBar actionBar = getActionBar();
		
		//find lowest free id (starting at 1)
		int id = 1;
		while (tabIDs.values().contains(id)) {
			id++;
		}
		//create new tab
		TabListener<MainScreenFragment> tabListener = new TabListener<MainScreenFragment>(this, "tag" + id, MainScreenFragment.class, id);
		Tab tab = actionBar.newTab().setText("Tab " + id).setTabListener(tabListener);
		actionBar.addTab(tab);
		tabIDs.put(tab, id);
		
		//select new tab
		actionBar.selectTab(tab);
		
		return (MainScreenFragment) tabListener.getFragment();
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
	
	private Tab getTabFromId(int tabID) {
		for (Tab t : tabIDs.keySet()) {
			if (tabIDs.get(t) == tabID) {
				return t;
			}
		}
		return null;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		ActionBar actionBar = getActionBar();
		String openTabs = "";
		SharedPreferences.Editor ed = mPrefs.edit();

		//save tabs
		for (int i = 0; i < actionBar.getTabCount(); i++) {
			int tabID = tabIDs.get(actionBar.getTabAt(i));
			openTabs += tabID;
			if (i != actionBar.getTabCount()-1) {
				openTabs += ",";
			}
			//save tab name
			ed.putString(MainScreenFragment.TAB_PREFIX + tabID + "-tabname", getTabFromId(tabID).getText().toString());
		}
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
	    private boolean isAdded = false;
	    
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
	        
	        //Instantiate fragment
	        Bundle bundle = new Bundle();
        	bundle.putInt("tabid", tabID);
            mFragment = Fragment.instantiate(mActivity, mClass.getName(), bundle);
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        if (!isAdded) {
	            //fragment needs to be added
	        	ft.add(android.R.id.content, mFragment, mTag);
	            isAdded = true;
	        } else {
	            //fragment is added, attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(final Tab tab, FragmentTransaction ft) {
	        if (mFragment instanceof MainScreenFragment
	        		&& System.currentTimeMillis() - MainScreen.lastOnTabReselected < 1000) {
	        	new RenameTabDialog() {
					
					@Override
					public void onRename(String newName) {
						tab.setText(newName);
					}
				}.show(mFragment.getFragmentManager(), "rename_tab");
	        }
	        MainScreen.lastOnTabReselected = System.currentTimeMillis();
	    }
	    
	    public Fragment getFragment() {
			return mFragment;
		}
	    
	    public int getTabID() {
			return tabID;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//start executor service
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newScheduledThreadPool(2);
		}
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

	@Override
	protected void init(NodeMainExecutor nodeMainExecutor) {
		Log.d("ros", "MainScreen.init(): executing node");
		NodeConfiguration nodeConfiguration = NodeConfiguration
				.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
		nodeConfiguration.setMasterUri(getMasterUri());
		nodeMainExecutor.execute(mainNode, nodeConfiguration);
	}
	
	private class Node extends AbstractNodeMain {

		@Override
		public GraphName getDefaultNodeName() {
			return GraphName.of("hanse_control");
		}
		
		@Override
		public void onStart(ConnectedNode node1) {
			node = node1;
			Log.d("ros", "MainScreen.Node.onStart(): node is connected!");
			executorService.execute(new Runnable() {
				
				@Override
				public void run() {
					synchronized (nodeConnectedListeners) {
						for (NodeConnectedListener listener : nodeConnectedListeners) {
							listener.onNodeConnected(node);
						}
					}
				}
			});
			
		}
	}
	
	public static interface NodeConnectedListener {
		public void onNodeConnected(ConnectedNode node);
	}
	
	public void addNodeConnectedListener(final NodeConnectedListener listener) {
		if (node != null) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					listener.onNodeConnected(node);
				}
			});
		} else {
			synchronized (nodeConnectedListeners) {
				nodeConnectedListeners.add(listener);				
			}
		}
	}
	
	public abstract static class RenameTabDialog extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
		    LayoutInflater inflater = getActivity().getLayoutInflater();
			
			builder.setTitle(getResources().getString(R.string.renamedialog_title));

			final View view = inflater.inflate(R.layout.dialog_renametab, null);
			builder.setView(view);
	
			final TextView textView = (TextView) view.findViewById(R.id.editText1);
			
			builder.setNegativeButton(getResources().getString(R.string.renamedialog_cancelbutton), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					RenameTabDialog.this.getDialog().cancel();
				}
			});
			
			builder.setPositiveButton(getResources().getString(R.string.renamedialog_renamebutton), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onRename(textView.getText().toString());
				}
			});
			
			return builder.create();	
		}
		
		public abstract void onRename(String newName);
	}
	
}
