package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import java.util.LinkedList;
import java.util.List;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;

public abstract class RosTextWidget<T> extends RosMapWidget implements MessageListener<T> {
	
	Subscriber<T> subscriber;
	
	TextView textView;
	
	String rosTtype;
	
	int bufferedLinesCount = 200;
	
	List<String> lines = new LinkedList<String>();
	
	public RosTextWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, 
			MainScreenFragment mainScreenFragment, String rosTtype, WidgetType widgetType) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment,
				rosTopic, widgetType);
		this.rosTtype = rosTtype;
		textView = new TextView(context);
		
		textView.setTextSize(18);
		textView.setTextColor(Color.WHITE);
		
		ScrollView scroll = new ScrollView(context);
//		scroll.setBackgroundColor(android.R.color.transparent);
//		scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//                LayoutParams.FILL_PARENT));
		scroll.addView(textView);
		
		setContent(scroll);
		
//		addView(linearLayout, 0);
		
	}
	
	@Override
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(getRosTopic(), rosTtype);
		subscriber.addMessageListener(this);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}

	public abstract String getStringFromMsg(T msg);
	
	@Override
	public void onNewMessage(final T msg) {
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {
				lines.add(getStringFromMsg(msg));
				final StringBuilder s = new StringBuilder();
				for (int i = lines.size() - 1; i >= 0; i--) {
					s.append(lines.get(i) + "\n");
				}
				while (lines.size() > bufferedLinesCount) {
					lines.remove(0);
				}
				textView.post(new Runnable() {	
					@Override
					public void run() {
						textView.setText(s);
					}
				});				
			}
		});
		
//		Log.d("ros", "RosTextWidget" + getWidgetID() + " received: " + msg.getData());
	}
}
