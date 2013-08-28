package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import geometry_msgs.Quaternion;

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

import sensor_msgs.Imu;

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
import android.graphics.Paint.Style;
import android.graphics.Path;
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

public class RosOrientationWidget extends RosMapWidget implements MessageListener<sensor_msgs.Imu> {
	Subscriber<sensor_msgs.Imu> subscriber;

	Paint linePaint = new Paint();
	Paint polyFillPaint = new Paint();
	Paint thinLinePaint = new Paint();
	
	View view;
	
	Float roll;
	Float pitch;
	
	public RosOrientationWidget(int widgetID, Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(250, 250, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment, rosTopic,
				WidgetType.ROS_ORIENTATION_WIDGET);
		setRatio(1f);
		
		view = new View(getContext()) {
			@Override
			protected void onDraw(Canvas canvas) {
				float triRad = Math.min(getHeight(), getWidth()) / 2f;
				float cx = (getWidth() - 1)  / 2f;
				float cy = (getHeight() - 1) / 2f;
				canvas.drawLine(cx - triRad, cy, cx + triRad, cy, thinLinePaint);
				canvas.drawCircle(cx, cy, triRad, thinLinePaint);
				
				if (roll != null && pitch != null) {
//					roll = (float) Math.PI * 0.25f;
//					pitch = (float) Math.PI * 0.03f;// + (float) Math.PI;	
					
					//change color
					float pitchSin = (float) Math.sin(pitch);
					int minVal = 20;
					int maxVal = 255;
					int range = maxVal - minVal;
					if (pitchSin > 0) {
						polyFillPaint.setColor(Color.argb(100, 0, (int)(range * pitchSin) + minVal, 0));
						linePaint.setColor(Color.argb(180, 0, (int)(range * pitchSin) + minVal, 0));						
					} else {
						polyFillPaint.setColor(Color.argb(100, (int)(range * -pitchSin) + minVal, 0, 0));
						linePaint.setColor(Color.argb(180, (int)(range * -pitchSin) + minVal, 0, 0));
					}
					
					triRad -= 2;
					//roll
					float dx = triRad * (float) Math.cos(roll);
					float dy = triRad * (float) Math.sin(roll);
					//pitch
					float pitchLen = triRad * pitchSin;
					float pdx = pitchLen * (float) Math.cos(roll + Math.PI / 2);
					float pdy = pitchLen * (float) Math.sin(roll + Math.PI / 2);
					
					//draw polygon
					Path path = new Path();
					path.moveTo(cx - dx, cy + dy);
					path.lineTo(cx + dx, cy - dy);
					path.lineTo(cx + pdx, cy - pdy);
					path.lineTo(cx - dx, cy + dy);
					canvas.drawPath(path, polyFillPaint);
					canvas.drawPath(path, linePaint);
					
				}
				
			}
		};
		setContent(view);
		
		linePaint.setAntiAlias(true);
		linePaint.setStrokeWidth(2f);
		linePaint.setStyle(Style.STROKE);
		linePaint.setStrokeCap(Paint.Cap.ROUND);
		thinLinePaint.setAntiAlias(true);
		thinLinePaint.setStrokeWidth(0.5f);
		thinLinePaint.setStyle(Style.STROKE);
		polyFillPaint.setStyle(Style.FILL);
		polyFillPaint.setAlpha(100);
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
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(getRosTopic(), sensor_msgs.Imu._TYPE);
		subscriber.addMessageListener(this, MainScreen.MESSAGE_QUEUE);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}

	@Override
	public void onNewMessage(final Imu imu) {
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {
				update(imu.getOrientation());				
			}
		});
	}	
	
	private void update(Quaternion quaternion) {
		double q0 = quaternion.getX();
		double q1 = quaternion.getY();
		double q2 = quaternion.getZ();
		double q3 = quaternion.getW();
		
//		roll = Math.atan2(2*(q0*q1+q2*q3), 1 - 2*(q1*q1 + q2*q2));
//		pitch = Math.asin(2*(q0*q2 - q3*q1));
//		yaw = Math.atan2(2*(q0*q3+q1*q2), 1 - 2*(q2*q2 + q3*q3));
		
		roll = (float) Math.atan2(2*(q0*q3+q1*q2), 1 - 2*(q2*q2 + q3*q3));
		pitch = (float) Math.asin(2*(q0*q2 - q3*q1));
//		Log.d("orientationwidget", String.format("Roll: %f, Pitch: %f", roll, pitch));
		redraw();
	}
} 
