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
//package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import org.ros.message.MessageListener;
//import org.ros.node.ConnectedNode;
//import org.ros.node.topic.Subscriber;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.ScrollView;
//import android.widget.TextView;
//import android.widget.RelativeLayout.LayoutParams;
//
//import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
//import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
////import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
////import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry;
//import de.uniluebeck.iti.hanse.hansecontrol.R;
////import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
//import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
//import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
//
//public abstract class RosTextWidget<T> extends RosMapWidget implements MessageListener<T> {
//	
//	Subscriber<T> subscriber;
//	
//	TextView textView;
//	
//	String rosTtype;
//	
//	int bufferedLinesCount = 200;
//	
//	List<String> lines = new LinkedList<String>();
//	
//	public RosTextWidget(int widgetID,	Context context, final String rosTopic, 
//			DragLayer dragLayer, String rosTtype) {
//		super(300, 200, widgetID, context, dragLayer, rosTopic);
//		this.rosTtype = rosTtype;
//		textView = new TextView(context);
//		
//		textView.setTextSize(18);
//		textView.setTextColor(Color.WHITE);
//		
//		ScrollView scroll = new ScrollView(context);
////		scroll.setBackgroundColor(android.R.color.transparent);
////		scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
////                LayoutParams.FILL_PARENT));
//		scroll.addView(textView);
//		
//		setContent(scroll);
//		
////		addView(linearLayout, 0);
//		
//	}
//	
//	@Override
//	public void subscribe(ConnectedNode node) {
//		subscriber = node.newSubscriber(getRosTopic(), rosTtype);
//		subscriber.addMessageListener(this, MainScreen.MESSAGE_QUEUE);
//	}
//
//	@Override
//	public void unsubscribe(ConnectedNode node) {
//		if (subscriber != null) {
//			subscriber.shutdown();
//		}
//	}
//
//	public abstract String getStringFromMsg(T msg);
//	
//	@Override
//	public void onNewMessage(final T msg) {
//		MainScreen.getExecutorService().execute(new Runnable() {
//			
//			@Override
//			public void run() {
//				lines.add(getStringFromMsg(msg));
//				final StringBuilder s = new StringBuilder();
//				for (int i = lines.size() - 1; i >= 0; i--) {
//					s.append(lines.get(i) + "\n");
//				}
//				while (lines.size() > bufferedLinesCount) {
//					lines.remove(0);
//				}
//				textView.post(new Runnable() {	
//					@Override
//					public void run() {
//						textView.setText(s);
//					}
//				});				
//			}
//		});
//		
////		Log.d("ros", "RosTextWidget" + getWidgetID() + " received: " + msg.getData());
//	}
//}
