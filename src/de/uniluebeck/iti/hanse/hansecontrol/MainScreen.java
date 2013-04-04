package de.uniluebeck.iti.hanse.hansecontrol;

import de.uniluebeck.iti.hanse.hansecontrol.views.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.app.ActionBar;

public class MainScreen extends Activity {
	
//	private MainScreenFragment frag1 = new MainScreenFragment();
//	private MainScreenFragment frag2 = new MainScreenFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		
		Tab tab = actionBar.newTab().setText("Testtab 1").setTabListener(
				new TabListener<MainScreenFragment>(this, "testtag1", MainScreenFragment.class));
		actionBar.addTab(tab);
		
		tab = actionBar.newTab().setText("Testtab 2").setTabListener(
				new TabListener<MapWidgetFragment>(this, "testtag2", MapWidgetFragment.class));
		actionBar.addTab(tab);
		
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
	 * Copied from http://developer.android.com/guide/topics/ui/actionbar.html#Tabs
	 */
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
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
	
}
