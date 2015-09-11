/*
* Copyright (c) 2015, Institute of Computer Engineering, University of Lübeck
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 
* * Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
* 
* * Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* 
* * Neither the name of the copyright holder nor the names of its
*   contributors may be used to endorse or promote products derived from
*   this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package de.uniluebeck.iti.hanse.hansecontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.ros.address.InetAddressFactory;
import org.ros.android.NodeMainExecutorService;
import org.ros.android.RosActivity;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import com.google.common.base.Preconditions;

//import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment.AddWidgetDialog;
//import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.gui.ChooseWidgetClassLayer;
import de.uniluebeck.iti.hanse.hansecontrol.gui.Sidebar;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TabContent;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TabManager;
import de.uniluebeck.iti.hanse.hansecontrol.gui.Tabbar;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.Tree;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.TreeValue;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeListDragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeListDragLayer.TreeListDragLayerListener;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosService;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider.Topic;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider.TopicsResultCallback;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosService.RosConnectionException;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosPlotWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosTextWidget;
import android.media.audiofx.AcousticEchoCanceler;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;

/**
 * This is the main Activity, it displays and manages tabs (Fragments), which
 * are instances of MainScreenFragment.
 * 
 * @author Stefan Hueske
 */
public class MainScreen extends Activity {

	private final ServiceConnection nodeMainExecutorServiceConnection;
	private RosService rosService;

	private Handler handler;
	
	private final class NodeMainExecutorServiceConnection implements
			ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			rosService = ((RosService.LocalBinder) binder).getService();
			rosService.addRosServiceListener(new RosService.RosServiceListener() {
				@Override
				public void onShutdown() {
					MainScreen.this.finish();
				}

				@Override
				public void onConnected() {
					Log.d("nodetest", "rosnode.onConnected()");
					tabManager.setRosService(rosService);
					updateTreeList();
				}

				@Override
				public void onError() {
					showChooseMasterUriDialog();
				}
			});
			String savedMasterUri = mPrefs.getString(MASTER_URI_PREF, "");
			
			if (savedMasterUri.isEmpty()) {
				showChooseMasterUriDialog();
			} else {
				try {
					rosService.connect(savedMasterUri);
				} catch (RosConnectionException e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							showChooseMasterUriDialog();
						}
					});
				}				
			}
				
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	private void parseSubTrees(Topic parentTopic, Tree<TopicTree> parentTree) {
		for (Topic subTopic : parentTopic.getSubTopics()) {
			Tree<TopicTree> subTree = new Tree<TopicTree>();
			subTree.setValue(new TopicTree(subTopic));
			parentTree.getSubTrees().add(subTree);
			parseSubTrees(subTopic, subTree);
		}
	}
	
	//TODO remove this
	private List<Topic> testfilter(List<Topic> topics) {
//		List<Topic> res = new LinkedList<Topic>();
//		for (int i = 0; i < 10; i++) {
//			res.add(topics.get(i));
//		}
//		return res;
		List<Topic> toRemove = new LinkedList<Topic>();		
		for (Topic t : topics) {
			recursiveFilter(t, null, toRemove);
		}
		topics.removeAll(toRemove);
		
		return topics;
	}
	
	private boolean recursiveFilter(Topic topic, Topic parent, List<Topic> toRemove) {
		for (Topic subTopic : topic.getSubTopics()) {
			if (recursiveFilter(subTopic, topic, toRemove)) {
				return true;
			}
		}
		topic.getSubTopics().removeAll(toRemove);
		if (!RosMapWidget.getRegistry().hasHandler(topic.getType())) {
			toRemove.add(topic);
			return false;
		}
		return true;
	}
	
	private List<Tree<TopicTree>> convertTopicListToTreeList(List<Topic> topics) {
		Log.d("topicdiscoveryTest", "converting " + topics.size());
		List<Tree<TopicTree>> res = new LinkedList<TreeList.Tree<TopicTree>>();
		
		for (Topic topic : topics) {
			Tree<TopicTree> tree = new Tree<TopicTree>();
			tree.setValue(new TopicTree(topic));
			parseSubTrees(topic, tree);
			res.add(tree);
		}
		
		return res;
	}
	
	public class TopicTree implements TreeValue {
		Topic topic;
		
		public TopicTree(Topic topic) {
			this.topic = topic;
		}
		
		@Override
		public View getView() {
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.treelist_item, null);
			String topicText = topic.getAttributePath().isEmpty() ? topic.getTopicName() 
					: topic.getAttributePath().get(topic.getAttributePath().size()-1);
			if (topicText.startsWith("get")) {
				topicText = topicText.substring(3);
			}
			((TextView) view.findViewById(R.id.topic)).setText(topicText);
			((TextView) view.findViewById(R.id.type)).setText(topic.getType());
			
			return view;
		}
		
		public Topic getTopic() {
			return topic;
		}
	}
	
	
	
	
	// workaround to get access to NodeMainExecutorService
	// public static class CustomNodeMainExecutorService extends
	// NodeMainExecutorService {
	// LocalBinder binder;
	//
	// public CustomNodeMainExecutorService() {
	// super();
	// binder = new LocalBinder();
	// }
	//
	// class LocalBinder extends Binder {
	// NodeMainExecutorService getService() {
	// return CustomNodeMainExecutorService.this;
	// }
	// }
	//
	// @Override
	// public IBinder onBind(Intent intent) {
	// return binder;
	// }
	//
	// @Override
	// public void shutdown() {
	// super.shutdown();
	// // MainScreen.this.finish();
	// }
	// }
	// FIXME dirty, copied from NodeMainExecutorService
	static final String ACTION_START = "org.ros.android.ACTION_START_NODE_RUNNER_SERVICE";
	static final String ACTION_SHUTDOWN = "org.ros.android.ACTION_SHUTDOWN_NODE_RUNNER_SERVICE";
	static final String EXTRA_NOTIFICATION_TITLE = "org.ros.android.EXTRA_NOTIFICATION_TITLE";
	static final String EXTRA_NOTIFICATION_TICKER = "org.ros.android.EXTRA_NOTIFICATION_TICKER";

	public MainScreen() {
		handler = new Handler();
		nodeMainExecutorServiceConnection = new NodeMainExecutorServiceConnection();
		initRosMapWidgetRegistry();
	}

	private void initRosMapWidgetRegistry() {
		
		RosMapWidget.Registry registry = RosMapWidget.getRegistry();
		registry.register("double", RosPlotWidget.class);
		registry.register("byte", RosPlotWidget.class);
		registry.register("int", RosPlotWidget.class);
		registry.register("float", RosPlotWidget.class);
		
//		registry.register("double", RosTextWidget.class);
	}

	private void startNodeMainExecutorService() {
		Intent intent = new Intent(this, RosService.class);
		intent.setAction(ACTION_START);
		intent.putExtra(EXTRA_NOTIFICATION_TICKER, "HanseControl");
		intent.putExtra(EXTRA_NOTIFICATION_TITLE, "HanseControl");
		startService(intent);
		Preconditions.checkState(
				bindService(intent, nodeMainExecutorServiceConnection,
						BIND_AUTO_CREATE),
				"Failed to bind NodeMainExecutorService.");
	}

	@Override
	protected void onStart() {
		super.onStart();
		startNodeMainExecutorService();
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		nodeMainExecutorService.shutdown();
//		finish();
//	}

	// ---------------------------------------------------------------------------------------

	// persistent app settings (eg. current Tabs, open widgets, map position and
	// zoom, ...)
	private SharedPreferences mPrefs;

	public static final String MASTER_URI_PREF = "MasterURI";

	// Map of all currently open tabs and their IDs
	HashMap<Tab, Integer> tabIDs = new HashMap<Tab, Integer>();
//	SparseArray<MainScreenFragment> fragments = new SparseArray<MainScreenFragment>();

	// global executor service which should be used to schedule tasks at any
	// location in this Activity (and MainScreenFragment Tabs)
	private static ScheduledExecutorService executorService;
	// TODO make sure to shutdown executorService when the app stops

//	Node mainNode = new Node();
	ConnectedNode node;
	List<NodeConnectedListener> nodeConnectedListeners = new LinkedList<NodeConnectedListener>();

	// workaround: onTabReselected() is called at startup for unknown reason, so
	// rename is started on doubletap
	private static long lastOnTabReselected = 0;

	private View activeContextMenuView = null;

	

	private Sidebar sidebarLeft;
	TreeList treeList;
	Tabbar tabbar;
	RelativeLayout tabContainer;
	TabManager tabManager;
	
	// public MainScreen() {
	// super("HanseControl", "HanseControl");
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set predefined XML layout
		setContentView(R.layout.main_screen);

		// load shared preferences
		mPrefs = getSharedPreferences("pref", 0);
		
		tabbar = (Tabbar) findViewById(R.id.tabbar);
		tabContainer = (RelativeLayout) findViewById(R.id.tabcontainer);
		sidebarLeft = (Sidebar) findViewById(R.id.sidebarLeft);
		TreeListDragLayer treeListDragLayer = (TreeListDragLayer) findViewById(R.id.treeListDragLayer);
		ChooseWidgetClassLayer chooseWidgetTypeLayer = (ChooseWidgetClassLayer) findViewById(R.id.chooseWidgetTypeLayer);
		chooseWidgetTypeLayer.setListener(new ChooseWidgetClassLayer.Listener() {
			
			@Override
			public void onWidgetClassChosen(float x, float y,
					Class<? extends RosMapWidget> widgetClass, TopicTree topicTree) {
				TabContent tab = tabManager.getCurrentTab();
				if (tab != null) {
					tab.widgetLayer.createWidget(x, y, widgetClass, topicTree);
				}
			}
		});
		
		
		treeList = new TreeList(this);
		treeList.setElementDragOutListener(treeListDragLayer);
		sidebarLeft.setContent(treeList);
		// treeList.setDragOrientation(TreeList.DRAG_RIGHT);
		treeList.setSwipeInListener(new TreeList.SwipeInListener() {
			@Override
			public void onSwipeIn() {
				sidebarLeft.setState(Sidebar.FOLDED);
			}
		});
		treeListDragLayer.setListener(chooseWidgetTypeLayer);

		
		
//		if (tabbar.getTabCount() == 0) {
//			tabbar.addTab(new TabContainer(this, 1));			
//		}
		
		tabManager = new TabManager(this, tabbar, tabContainer);
		
		
		updateTreeList();
		
		// init action bar
		// ActionBar actionBar = getActionBar();
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// actionBar.setDisplayShowTitleEnabled(true);

		// restore tabs
		// String openTabs = mPrefs.getString("global_opentabs", "");
		// if (!openTabs.isEmpty()) {
		// try {
		// int selectedTab = mPrefs.getInt("global_selectedTab", 0);
		// String[] d = openTabs.split(",");
		// for (int i = 0; i < d.length; i++) {
		// //TODO names for tabs
		// int id = Integer.parseInt(d[i]);
		// Tab tab = actionBar.newTab().setText(mPrefs.getString(
		// MainScreenFragment.TAB_PREFIX + id + "-tabname", "Tab " + id))
		// .setTabListener(
		// new TabListener<MainScreenFragment>(this, "tag" + id,
		// MainScreenFragment.class, id));
		// actionBar.addTab(tab);
		// tabIDs.put(tab, id);
		//
		// if (selectedTab == id) {
		// actionBar.selectTab(tab);
		// }
		// }
		// } catch (Exception e) {
		// Log.e("encoding", "Error while trying to decode opentabs!");
		// }
		// }

		// at least one tab must always be open
		// if (actionBar.getTabCount() == 0) {
		// addNewTab();
		// }
		Log.d("statemanagement", "MainScreen.onCreate() finished");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		activeContextMenuView = v;
		MenuInflater inflater = getMenuInflater();
		if (activeContextMenuView instanceof MapWidget.ShowContextMenuButton) {
			inflater.inflate(R.menu.widget_contextmenu, menu);
			((MapWidget) activeContextMenuView.getParent())
					.setConfigDialogItem(menu.add("Configure widget"));
		}
	}
	
	private void updateTreeList() {
		if (rosService == null || !rosService.isConnected() || treeList == null) {
			return;
		}
		rosService.getRosDataProvider().getAvailableTopics(new TopicsResultCallback() {
			@Override
			public void resultCallback(List<Topic> topics) {
				treeList.setData(convertTopicListToTreeList(testfilter(topics)));
			}
		});
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (activeContextMenuView != null
				&& activeContextMenuView instanceof MapWidget.ShowContextMenuButton) {
			((MapWidget.ShowContextMenuButton) activeContextMenuView)
					.performAction(item, getFragmentManager());
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("actionbar",
				"MainScreen: Menu action activated! '" + item.getTitle() + "'");

		switch (item.getItemId()) {
		case R.id.tabCreate:
//			addNewTab();
			return true;
		case R.id.tabClose:
			if (getActionBar().getSelectedTab() != null) {
				closeTab(getActionBar().getSelectedTab());
			}
			return false; // tabClose must also be received by the fragment to
							// prevent it from saving the state
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

//	public MainScreenFragment addNewTab() {
//		ActionBar actionBar = getActionBar();
//
//		// find lowest free id (starting at 1)
//		int id = 1;
//		while (tabIDs.values().contains(id)) {
//			id++;
//		}
//		// create new tab
//		TabListener<MainScreenFragment> tabListener = new TabListener<MainScreenFragment>(
//				this, "tag" + id, MainScreenFragment.class, id);
//		Tab tab = actionBar.newTab().setText("Tab " + id)
//				.setTabListener(tabListener);
//		actionBar.addTab(tab);
//		tabIDs.put(tab, id);
//
//		// select new tab
//		actionBar.selectTab(tab);
//
//		return (MainScreenFragment) tabListener.getFragment();
//	}

	public boolean closeTab(int tabID) {
		for (Tab t : tabIDs.keySet()) {
			if (tabIDs.get(t) == tabID) {
				getActionBar().removeTab(t);
				tabIDs.remove(t);
				clearTabPrefs(tabID);

				// at least one tab must always be open
				if (getActionBar().getTabCount() == 0) {
//					addNewTab();
				}
				return true;
			}
		}
		return false;
	}

	private void clearTabPrefs(int tabID) {
//		// find related entries
//		List<String> keysToDelete = new LinkedList<String>();
//		for (String key : mPrefs.getAll().keySet()) {
//			if (key.startsWith(MainScreenFragment.TAB_PREFIX + tabID)) {
//				keysToDelete.add(key);
//				Log.d("statemanagement", "Deleting key: " + key);
//			}
//		}
//		SharedPreferences.Editor ed = mPrefs.edit();
//		for (String key : keysToDelete) {
//			ed.remove(key);
//		}
//		ed.commit();
	}

	public void closeTab(Tab tab) {
		int tabID = tabIDs.get(tab);
		getActionBar().removeTab(tab);
		tabIDs.remove(tab);
		clearTabPrefs(tabID);

		if (getActionBar().getTabCount() == 0) {
//			addNewTab();
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
		// ActionBar actionBar = getActionBar();
		// String openTabs = "";
		// SharedPreferences.Editor ed = mPrefs.edit();
		//
		// //save tabs
		// for (int i = 0; i < actionBar.getTabCount(); i++) {
		// int tabID = tabIDs.get(actionBar.getTabAt(i));
		// openTabs += tabID;
		// if (i != actionBar.getTabCount()-1) {
		// openTabs += ",";
		// }
		// //save tab name
		// ed.putString(MainScreenFragment.TAB_PREFIX + tabID + "-tabname",
		// getTabFromId(tabID).getText().toString());
		// }
		// ed.putString("global_opentabs", openTabs);
		//
		// if (actionBar.getSelectedTab() != null) {
		// //should never be null though
		// ed.putInt("global_selectedTab",
		// tabIDs.get(actionBar.getSelectedTab()));
		// }
		// ed.commit();

	}

	/*
	 * Modified version of example in
	 * http://developer.android.com/guide/topics/ui/actionbar.html#Tabs Manages
	 * attaching and detaching of Tabs (MainScreenFragments instances)
	 */
	public static class TabListener<T extends Fragment> implements
			ActionBar.TabListener {
		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		private int tabID;
		private boolean isAdded = false;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public TabListener(Activity activity, String tag, Class<T> clz,
				int tabID) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
			this.tabID = tabID;

			// Instantiate fragment
			Bundle bundle = new Bundle();
			bundle.putInt("tabid", tabID);
			mFragment = Fragment.instantiate(mActivity, mClass.getName(),
					bundle);
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (!isAdded) {
				// fragment needs to be added
				ft.add(android.R.id.content, mFragment, mTag);
				isAdded = true;
			} else {
				// fragment is added, attach it in order to show it
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
//			if (mFragment instanceof MainScreenFragment
//					&& System.currentTimeMillis()
//							- MainScreen.lastOnTabReselected < 1000) {
//				new RenameTabDialog() {
//
//					@Override
//					public void onRename(String newName) {
//						tab.setText(newName);
//					}
//				}.show(mFragment.getFragmentManager(), "rename_tab");
//			}
//			MainScreen.lastOnTabReselected = System.currentTimeMillis();
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
		// start executor service
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newScheduledThreadPool(4);
		}
		Log.d("statemanagement", "MainScreen.onResume");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// do nothing here, preventing android from saving the state (this is
		// done in onPause())
	}

	@Override
	protected void onStop() {
		super.onStop();
		executorService.shutdownNow();
	}

	
//	private void initService(String uri) {
//		if (nodeMainExecutorService.getConnectionState() == 
//				CustomNodeMainExecutorService.DISCONNECTED) {
//			String savedMasterUri = mPrefs.getString(MASTER_URI_PREF, "");
//			// String savedMasterUri = "";
//	
//			if (savedMasterUri.isEmpty()) {
//				showChooseMasterUriDialog();
//			} else {
//				try {
//					nodeMainExecutorService.connect(savedMasterUri);
//					
//				} catch (URISyntaxException e) {
//					showChooseMasterUriDialog();
//				}
//			}
//		}
//	}
	
//	protected void onServiceConnected(CustomNodeMainExecutorService service) {
//		
//
//		Log.d("ros", "MainScreen.init(): executing node");
//		NodeConfiguration nodeConfiguration = NodeConfiguration
//				.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//		nodeConfiguration.setMasterUri(nodeMainExecutorService.getMasterUri());
//		// try {
//		// nodeConfiguration.setMasterUri(new URI("http://ROS:11311/"));
//		// } catch (URISyntaxException e) {
//		// Log.e("MainScreen","init() error",e);
//		// }
//
//		String savedMasterUri = mPrefs.getString(MASTER_URI_PREF, "");
//		// String savedMasterUri = "";
//
//		if (savedMasterUri.isEmpty()) {
//			showChooseMasterUriDialog();
//		} else {
//			try {
//				nodeMainExecutorService.setMasterUri(new URI(savedMasterUri));
//				executeMainNode();
//			} catch (URISyntaxException e) {
//				showChooseMasterUriDialog();
//			}
//		}
//
//	}

//	private void executeMainNode() {
//		
//	}

	private void showChooseMasterUriDialog() {
		new ChooseMasterUriDialog() {

			@Override
			public void saveURI(String masterUri) {
				mPrefs.edit().putString(MASTER_URI_PREF, masterUri).commit();
			}

			@Override
			public void onConnect(String masterUri) {
				try {
					rosService.connect(masterUri);
				} catch (RosConnectionException e) {
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							showChooseMasterUriDialog();
						}
					});
				}
			}

			@Override
			public void clearURI() {
				mPrefs.edit().remove(MASTER_URI_PREF);
			}
		}.show(getFragmentManager(), null);
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

			builder.setTitle(getResources().getString(
					R.string.renamedialog_title));

			final View view = inflater.inflate(R.layout.dialog_renametab, null);
			builder.setView(view);

			final TextView textView = (TextView) view
					.findViewById(R.id.mapName);

			builder.setNegativeButton(
					getResources()
							.getString(R.string.renamedialog_cancelbutton),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							RenameTabDialog.this.getDialog().cancel();
						}
					});

			builder.setPositiveButton(
					getResources()
							.getString(R.string.renamedialog_renamebutton),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							onRename(textView.getText().toString());
						}
					});

			return builder.create();
		}

		public abstract void onRename(String newName);
	}

	public synchronized static ScheduledExecutorService getExecutorService() {
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newScheduledThreadPool(4);
		}
		return executorService;
	}

	public abstract static class ChooseMasterUriDialog extends DialogFragment {
		// TextView errorText;
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			LayoutInflater inflater = getActivity().getLayoutInflater();

			builder.setTitle("Choose Master-URI");

			final View view = inflater.inflate(
					R.layout.dialog_choose_master_uri, null);
			builder.setView(view);

			final CheckBox rememberCheckBox = (CheckBox) view
					.findViewById(R.id.rememberMasterUriCheckBox);
			final EditText masterUriEditText = (EditText) view
					.findViewById(R.id.masterUriEditText);
			// errorText = (TextView) view.findViewById(R.id.errorTextView);

			// builder.setNegativeButton(getResources().getString(R.string.renamedialog_cancelbutton),
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// RenameTabDialog.this.getDialog().cancel();
			// }
			// });

			builder.setPositiveButton("Connect",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String masterUri = masterUriEditText.getText()
									.toString();

							if (rememberCheckBox.isChecked()) {
								saveURI(masterUri);
							} else {
								clearURI();
							}
							onConnect(masterUri);
						}
					});

			return builder.create();
		}

		// private void displayErrorHint() {
		// errorText.setText("URI is invalid!");
		// errorText.setVisibility(View.VISIBLE);
		// }

		public abstract void clearURI();

		public abstract void saveURI(String masterUri);

		public abstract void onConnect(String masterUri);
	}
}
