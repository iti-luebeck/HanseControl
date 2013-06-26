package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.android.BitmapFromImage;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.RosRobot;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;

public class RosOrientationWidget extends RosMapWidget {
	String rosTopic;
	Subscriber<hanse_msgs.ScanningSonar> subscriber;
	
	TextView textView;
	
	Paint backgroundPaint = new Paint();
	Paint linePaint = new Paint();
	
	LinearLayout linearLayout;
	
	View view;
	
//	Thread redrawThread;
	
	public RosOrientationWidget(int widgetID, Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(250, 250, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment);
		setRatio(1f);
		this.rosTopic = rosTopic;
		textView = new TextView(context);
		
		textView.setTextSize(18);
		textView.setTextColor(Color.WHITE);
		
		linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView topicHeader = new TextView(context);
		topicHeader.setText("Orientation");
		topicHeader.setGravity(Gravity.CENTER);
		topicHeader.setTextColor(Color.LTGRAY);
		
		view = new View(getContext()) {
			@Override
			protected void onDraw(Canvas canvas) {
//				canvas.drawLine(0, 0, getWidth(), getHeight(), new Paint());
				RosRobot r = RosRobot.getInstance();
				if (r.getRoll() != null && r.getPitch() != null && r.getYaw() != null) {
					double triLen = (Math.min(getHeight(), getWidth()) / 2) / Math.sin(Math.PI / 3);
					double dx = (triLen / 2) * Math.cos(r.getPitch());
					double dy = (triLen / 2) * Math.sin(r.getYaw());
					
					double cx = getWidth() / 2d;
					double cy = getHeight() / 2d;
					
					canvas.drawLine((float)(cx - dx), (float)(cy + dy), (float)(cx + dx), (float)(cy - dy), linePaint);
					
				}
				
			}
		};
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(view);
		
//		addView(linearLayout, 0);
		
		backgroundPaint.setColor(Color.BLACK);
		backgroundPaint.setAlpha(80);
		backgroundPaint.setStyle(Paint.Style.FILL);
		
		
		final Paint iconTextPaint = new Paint();
		iconTextPaint.setColor(Color.WHITE);
		final float textSize = 16;
		iconTextPaint.setTextSize(textSize);
		
		addView(new View(context){
			@Override
			protected void onDraw(Canvas canvas) {
				if (getMode() == FULLSIZE_MODE) {
					canvas.drawRect(new Rect(0,0, getWidth(), getHeight()), backgroundPaint);
				} else if (getMode() == ICON_MODE) {
					String iconText = shrinkStringToWidth(iconTextPaint, getWidth(), "Orientation");
					canvas.drawText(iconText, getWidth() / 2 - iconTextPaint.measureText(iconText) / 2, textSize, iconTextPaint);
					Bitmap bitmap = BitmapManager.getInstance().getBitmap(getResources(), 
							R.drawable.rostextwidget);
					canvas.drawBitmap(bitmap, null, 
							scaleToBox(bitmap.getWidth(), bitmap.getHeight(), 
									0, textSize + 3, getWidth(), getHeight() - (textSize + 3)), null);
				}
			}
		}, 0);
		
		
	}
	
	private String shrinkStringToWidth(Paint paint, float width, String str) {
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
	
	private RectF scaleToBox(float inputWidth, float inputHeight, float x, float y, float width, float height) {
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
	
	@Override
	public void setMode(int mode) {
		super.setMode(mode);
		if (mode == ICON_MODE) {
			removeView(linearLayout);
		} else if (mode == FULLSIZE_MODE && linearLayout.getParent() != this) {
			addView(linearLayout, 1);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			linearLayout.setLayoutParams(params);
		}
	}
	
//	@Override
//	public void subscribe(ConnectedNode node) {
////		subscriber = node.newSubscriber(rosTopic, hanse_msgs.ScanningSonar._TYPE);
////		subscriber.addMessageListener(this);
//		
//		
//		//workaround for drawing issue, apparently canvas.draw methods perform 
//		//the actual drawing into the bitmap in background
////		if (redrawThread == null || !redrawThread.isAlive()) {
////			redrawThread = new Thread() {
////				public void run() {
////					while (!isInterrupted()) {
////						redraw();
////						try {
////							Thread.sleep(500);
////						} catch (InterruptedException e) {
////							interrupt();
////						}
////					}
////				}
////			};
////			redrawThread.start();
////		}
//	}

//	@Override
//	public void unsubscribe(ConnectedNode node) {
//		if (subscriber != null) {
//			subscriber.shutdown();
//		}
////		plotView.clearValues();
////		if (redrawThread != null && redrawThread.isAlive()) {
////			redrawThread.interrupt();
////		}
//	}

	
//	@Override
//	public abstract WidgetType getWidgetType();
	
	@Override
	public String getRosTopic() {
		return rosTopic;
	}
	
	public void redraw() {
		view.post(new Runnable() {
			
			@Override
			public void run() {
				view.invalidate();
			}
		});
	}

	@Override
	public WidgetType getWidgetType() {
		return WidgetType.ROS_ORIENTATION_WIDGET;
	}

	@Override
	public void subscribe(ConnectedNode node) {
		
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		
	}	
} 
