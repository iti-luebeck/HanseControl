package de.uniluebeck.iti.hanse.hansecontrol.gui;

import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosService;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class TabManager {
	Tabbar tabbar;
	RelativeLayout tabContainer; 
	List<TabContent> tabContentList = new LinkedList<TabContent>();
	Context context;
	
	Set<Integer> usedIDs = new HashSet<Integer>();
	Random random = new Random();
	
	TabContent selectedTab;
	
	RosService rosService;
	
	public TabManager(Context context, Tabbar tabbar, RelativeLayout tabContainer) {
		this.context = context;
		this.tabbar = tabbar;
		this.tabContainer = tabContainer;
		
		if (tabbar.getTabCount() == 0) {
			addTab();
		}
		
		tabbar.setAddTabListener(new Tabbar.AddTabListener() {
			
			@Override
			public void onAddTab() {
				addTab();
			}
		});
		
	}
	
	private void addTab() {
		final TabContent tab = new TabContent(context, createID());
		tab.setRosService(rosService);
		tabbar.addTab(new Tabbar.Tab() {
			
			@Override
			public void onTabClose() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSelected() {
				selectedTab = tab;
				tabContainer.removeAllViews();
				tabContainer.addView(tab);
//				tabContainer.bringChildToFront(content);
				tabContainer.invalidate();
//				content.invalidate();
			}
			
			@Override
			public void onDeselect() {
				// TODO Auto-generated method stub
				selectedTab = null;
			}
			
			@Override
			public String getTabName() {
				// TODO Auto-generated method stub
				return "Tab " + tabContentList.indexOf(tab);
			}
		});
		tabContentList.add(tab);
	}
	
	private int createID() {
		int id = 0;
		while(usedIDs.contains(id = random.nextInt())) { }
		usedIDs.add(id);
		return id;
	}
	
	public TabContent getCurrentTab() {
		return selectedTab;
	}
	
	public void setRosService(RosService rosService) {
		this.rosService = rosService;
		for (TabContent tab : tabContentList) {
			tab.setRosService(rosService);
		}
	}
}
