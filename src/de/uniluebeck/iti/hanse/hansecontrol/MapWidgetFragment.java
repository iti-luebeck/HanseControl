package de.uniluebeck.iti.hanse.hansecontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapWidgetFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		Log.d("statemanagement", "MapWidgetFragment.onCreateView() called.");
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.map_widget_fragment, null);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		Log.d("statemanagement", "MapWidgetFragment.onSaveInstanceState() called.");
		super.onSaveInstanceState(outState);
	}
}
