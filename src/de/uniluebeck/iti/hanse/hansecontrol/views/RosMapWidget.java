package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ros.node.ConnectedNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen.TopicTree;
//import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
//import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
//import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider.RosDataConnection;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider.RosDataUpdateListener;
//import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
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
import android.widget.TextView;

public abstract class RosMapWidget extends MapWidget implements RosDataUpdateListener {

	private ConnectedNode node;
	RelativeLayout contentLayout; //contains header
	
	String iconText, headerText;
	
//	String rosTopic;
//	WidgetType widgetType;	
	
	private static Registry registry;
	TopicTree topicTree;
		
	RosDataConnection dataConnection;
	
	public RosMapWidget(int defaultWidth, int defaultHeight, int widgetID,
			Context context, DragLayer dragLayer, TopicTree topicTree) {
		super(defaultWidth, defaultHeight, widgetID, context, dragLayer);
		this.topicTree = topicTree;
//		this.widgetType = widgetType;
		
//		iconText = rosTopic;
		headerText = topicTree.getTopic().getFullTopicName();
		
		//add background for ICON and FULLSIZE mode
		final Paint backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLACK);
		backgroundPaint.setAlpha(80);
		backgroundPaint.setStyle(Paint.Style.FILL);
		
		final float textSize = 16;
		final Paint iconTextPaint = new Paint();
		iconTextPaint.setColor(Color.WHITE);
		iconTextPaint.setTextSize(textSize);
		
		addView(new View(context){
			@Override
			protected void onDraw(Canvas canvas) {
//				if (getMode() == FULLSIZE_MODE) {
					canvas.drawRect(new Rect(0,0, getWidth(), getHeight()), backgroundPaint);
//				} else if (getMode() == ICON_MODE) {
//					String iconText = shrinkStringToWidth(iconTextPaint, getWidth(), 
//							RosMapWidget.this.iconText);
//					canvas.drawText(iconText, getWidth() / 2 - iconTextPaint.measureText(iconText) / 2, 
//							textSize, iconTextPaint);
//					Bitmap bitmap = BitmapManager.getInstance().getBitmap(getResources(), 
//							getIconFromWidget(getWidgetType()));
//					canvas.drawBitmap(bitmap, null, 
//							scaleToBox(bitmap.getWidth(), bitmap.getHeight(), 
//									0, textSize + 3, getWidth(), getHeight() - (textSize + 3)), null);
//				}
			}
		}, 0);
		
		//add header
		TextView headerTextView = new TextView(context);
		headerTextView.setText(headerText);
		headerTextView.setGravity(Gravity.CENTER);
		headerTextView.setTextColor(Color.LTGRAY);
		headerTextView.setId(1);
		
		contentLayout = new RelativeLayout(context);
		contentLayout.addView(headerTextView);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) headerTextView.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		headerTextView.setLayoutParams(params);
		
		addView(contentLayout, 1);
		params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		contentLayout.setLayoutParams(params);
	}
	
	public TopicTree getTopicTree() {
		return topicTree;
	}
	
	public void setDataConnection(RosDataConnection dataConnection) {
		this.dataConnection = dataConnection;
		dataConnection.setUpdateListener(this);
	}
	
	public RosDataConnection getDataConnection() {
		return dataConnection;
	}
	
	private static int getIconFromWidget() {
//		switch (widgetType) {
//			case ROS_IMAGE_WIDGET:	return R.drawable.widgeticon_image;
//			case ROS_ORIENTATION_WIDGET: return R.drawable.widgeticon_orientation;
//			case ROS_PLOT_WIDGET__FLOAT64:
//			case ROS_PLOT_WIDGET__PRESSURE:
//			case ROS_PLOT_WIDGET__SOLLSPEED: return R.drawable.widgeticon_plot;
//			case ROS_SONAR_WIDGET: return R.drawable.widgeticon_sonar;
//			case ROS_TEXT_WIDGET__FLOAT64:
//			case ROS_TEXT_WIDGET__PRESSURE:
//			case ROS_TEXT_WIDGET__SOLLSPEED:
//			case ROS_TEXT_WIDGET__STRING: return R.drawable.widgeticon_text;	
//		}
//		Log.e("RosMapWidget", "Error in RosMapWidget.getIconFromWidget()!");
		return R.drawable.widgeticon_text;
	}
	
	public void setContent(View contentView) {
		while(contentLayout.getChildCount() > 1) {
			contentLayout.removeViewAt(1);
		}
		contentLayout.addView(contentView);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentView.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, contentLayout.getChildAt(0).getId());
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		contentView.setLayoutParams(params);
	}
	
	public void setNode(ConnectedNode node) {
		this.node = node;
//		if (getMode() == FULLSIZE_MODE) {
			subscribe(node);
//		}
	}
	
//	@Override
//	public void setMode(int mode) {
//		super.setMode(mode);
//		if (node != null) {
//			if (mode == ICON_MODE) {
//				unsubscribe(node);
//			} else if (mode == FULLSIZE_MODE) {
//				subscribe(node);
//			}
//		}
//		if (mode == ICON_MODE) {
//			removeView(contentLayout);
//		} else if (mode == FULLSIZE_MODE) {
//			addView(contentLayout, 1);
//			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
//			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			contentLayout.setLayoutParams(params);
//		}
//	}
	
	public abstract void subscribe(ConnectedNode node);
	
	public abstract void unsubscribe(ConnectedNode node);
	
//	public WidgetType getWidgetType() {
//		return widgetType;
//	}
	
//	public String getRosTopic() {
//		return rosTopic;
//	}
	
	public static String shrinkStringToWidth(Paint paint, float width, String str) {
		if (!str.isEmpty() && paint.measureText(str) > width) {
			String placeholder = "...";			
			String head = str.substring(0, str.length() / 2);
			String tail = str.substring(str.length() / 2);
			while (paint.measureText(head + placeholder + tail) > width && !head.isEmpty() && !tail.isEmpty()) {
				head = head.substring(0, head.length() - 1);
				tail = tail.substring(1);
			}
			return head + placeholder + tail;
		}
		return str;
	}
	
	public static RectF scaleToBox(float inputWidth, float inputHeight, float x, float y, float width, float height) {
		float ratio = width / height;
		float inputRatio = inputWidth / inputHeight;
		
		float outX;
		float outY;
		float outWidth;
		float outHeight;
		
		if (inputRatio < ratio) {
			outHeight = height;
			outWidth = inputRatio * height;
			outY = y;
			outX = x + (width / 2 - outWidth / 2);
		} else {
			outWidth = width;
			outHeight = (1 / inputRatio) * width;
			outX = x;
			outY = y + (height / 2 - outHeight / 2);
		}
		
		return new RectF(outX, outY, outWidth + outX, outHeight + outY);
	}
	
	public static synchronized Registry getRegistry() {
		if (registry == null) {
			registry = new Registry();
		}
		return registry;
	}
	
	public static class Registry {
		Multimap<String, Class<? extends RosMapWidget>> typeHandler = HashMultimap.create();
		
		public synchronized void register(String rosType, Class<? extends RosMapWidget> widgetClass) {
			typeHandler.put(rosType, widgetClass);
		}
		
		public synchronized Collection<Class<? extends RosMapWidget>> getRosMapWidgetClass(String rosType) {
			return typeHandler.get(rosType);
		}
		
		public boolean hasHandler(String rosType) {
			return typeHandler.containsKey(rosType);
		}
	}
	
}
