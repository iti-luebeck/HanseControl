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
