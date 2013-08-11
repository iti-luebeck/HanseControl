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
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;

public class RosSonarWidget extends RosMapWidget implements MessageListener<hanse_msgs.ScanningSonar> {
	String rosTopic;
	Subscriber<hanse_msgs.ScanningSonar> subscriber;
	
	TextView textView;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	SonarView sonarView;
	
	Thread redrawThread;
	
	public RosSonarWidget(int widgetID, Context context, final String rosTopic, 
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
		topicHeader.setText(rosTopic);
		topicHeader.setGravity(Gravity.CENTER);
		topicHeader.setTextColor(Color.LTGRAY);
		
		sonarView = new SonarView(getContext());
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(sonarView);
		
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
					String iconText = shrinkStringToWidth(iconTextPaint, getWidth(), rosTopic);
					canvas.drawText(iconText, getWidth() / 2 - iconTextPaint.measureText(iconText) / 2, textSize, iconTextPaint);
					Bitmap bitmap = BitmapManager.getInstance().getBitmap(getResources(), 
							R.drawable.widgeticon_sonar);
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
	
	@Override
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(rosTopic, hanse_msgs.ScanningSonar._TYPE);
		subscriber.addMessageListener(this);
		
		
		//workaround for drawing issue, apparently canvas.draw methods perform 
		//the actual drawing into the bitmap in background
		if (redrawThread == null || !redrawThread.isAlive()) {
			redrawThread = new Thread() {
				public void run() {
					while (!isInterrupted()) {
						redraw();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							interrupt();
						}
					}
				}
			};
			redrawThread.start();
		}
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
//		plotView.clearValues();
		if (redrawThread != null && redrawThread.isAlive()) {
			redrawThread.interrupt();
		}
	}

	
	@Override
	public void onNewMessage(final hanse_msgs.ScanningSonar msg) {
//		Log.d("sonar", msg.getHeadPosition() + "");
		sonarView.addValue(msg);
	}
	
//	@Override
//	public abstract WidgetType getWidgetType();
	
	@Override
	public String getRosTopic() {
		return rosTopic;
	}
	
	public void redraw() {
		sonarView.post(new Runnable() {
			
			@Override
			public void run() {
				sonarView.invalidate();
			}
		});
	}

	@Override
	public WidgetType getWidgetType() {
		return WidgetType.ROS_SONAR_WIDGET;
	}	
}

class SonarView extends View {

	List<hanse_msgs.ScanningSonar> values = new LinkedList<hanse_msgs.ScanningSonar>();
	Bitmap bitmap = null;
	int lastPaintedIndex = -1;
	
	ScheduledFuture drawingFuture;
	boolean interrupt = false;	
	float angle = 0.07854f;
	
	
	public SonarView(Context context) {
		super(context);
	}
	
	public Rect getViewportRect() {
		Rect res = new Rect();
		if (getWidth() / (float)getHeight() > 1) {
			res.top = 0;
			res.bottom = getHeight(); 
			res.left = getWidth() / 2 - res.height() / 2;
			res.right = res.left + res.height();
		} else {
			res.left = 0;
			res.right = getWidth(); 
			res.top = getHeight() / 2 - res.width() / 2;
			res.bottom = res.top + res.width();
		}
		
		return res;
	}
	
	public void createNewBitmap(Rect r) {
		interrupt = true;
		synchronized (values) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
			bitmap = Bitmap.createBitmap(r.width(), r.height(), Config.ARGB_8888);
			lastPaintedIndex = -1;
		}
	}
	
	Paint flipPaint = new Paint();
	
	private float getAngleDistance(float a1, float a2) {
		return (float)Math.min((a1 + Math.PI / 2) + (Math.PI * 1.5 - a2), a2 - a1);
	}
	
	private float getAngleMid(float a1, float a2) {
		return getAngleDistance(a1, a2) / 2 + a1;
	}
	
	private void drawValues() {
		interrupt = false;
		Rect viewportRect = getViewportRect();
		RectF rect = new RectF(0, 0, viewportRect.width(), viewportRect.height());
		
//		Log.d("viewrect", "----");
//		Log.d("viewrect", String.format("left: %d, top: %d, right: %d, bottom", viewportRect.left, viewportRect.top,
//				viewportRect.right, viewportRect.bottom));

		float toDegrees = (float)(360 / (2*Math.PI));

//		canvas.drawLine(0, 0, 100, 100, new Paint());
		
		Paint segmentPaint = new Paint();
		segmentPaint.setAntiAlias(true);
		
		
		
		
		
		synchronized (values) {
			Canvas canvas = new Canvas(bitmap);
			for (int i = lastPaintedIndex + 1; i < values.size(); i++) {
				if (i % 2 == 0) {
					flipPaint.setColor(Color.WHITE);
				} else {
					flipPaint.setColor(Color.BLACK);
				}
//				Log.d("sonar", "" + i);
				hanse_msgs.ScanningSonar val = values.get(i);
				
//				canvas.drawArc(rect, toDegrees * (float)val.getHeadPosition(), toDegrees * 0.08f, true, flipPaint);
				
				float startangle = toDegrees * ((float)(val.getHeadPosition() + Math.PI));
				float sweep = toDegrees * angle;
				
				byte[] data = val.getEchoData().array();
				
				RectF r = new RectF(rect);
				
				float segmentSize = (r.width() / 2f) / data.length;
				
//				Log.d("sonardebug", String.format("StartAngle: %f, HeadPos: %f", startangle, (float)val.getHeadPosition()));
				
				for (int i2 = data.length - 1; i2 >= 0; i2--) {
					byte b = data[i2];
					if (interrupt) {
						return;
					}
					int c = 0x000000FF & b;
					segmentPaint.setColor(0xFF << 24 | c << 16 | c << 8 | c);
					
					canvas.drawArc(r, startangle, sweep, true, segmentPaint);
					
					r.right -= segmentSize;
					r.top += segmentSize;
					r.bottom -= segmentSize;
					r.left += segmentSize;
				}
				
				
				
//				canvas.
				
				
//				canvas.drawArc(rect, , , true, flipPaint);
				
//				Log.d("", "");
				
				
				
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				SonarView.this.invalidate();				
				
			}
			lastPaintedIndex = values.size() - 1;
		}
		
		
		
		
//		canvas.drawLine(0, (values.size() * 3) % bitmap.getHeight(), bitmap.getWidth() -1, (values.size() * 3) % bitmap.getHeight(), new Paint());
	}
	
	private synchronized void scheduleDrawing() {
//		synchronized (this) {			
//			Log.d("sonartasks", MainScreen.getExecutorService().
			Log.d("sonartasks", (drawingFuture == null || drawingFuture.isDone()) + "");
			if (drawingFuture == null || drawingFuture.isDone()) {
				drawingFuture = MainScreen.getExecutorService().schedule(new Runnable() {
					
					@Override
					public void run() {
						try {
							drawValues();
						} catch (Exception e) {
							Log.e("sonar", "Error while drawing sonar bitmap!", e);
						}
//						SonarView.this.invalidate();
					}
				}, 0, TimeUnit.MILLISECONDS);
			}
//		}
	}
	
	private void checkBitmapSize() {
		Rect r = getViewportRect();
		if (bitmap == null || bitmap.isRecycled()
				|| bitmap.getWidth() != r.width() || bitmap.getHeight() != r.height()) {
			createNewBitmap(r);
		}
		scheduleDrawing();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
//		MainScreen.getExecutorService().execute(new Runnable() {
//			
//			@Override
//			public void run() {
//				checkBitmapSize();
//			}
//		});
		 
		
		if (bitmap != null && !bitmap.isRecycled()) {
			canvas.drawBitmap(bitmap, null, getViewportRect(), null);
		}
			
			
			
			
//		float x = getWidth() / 2;
//		float y = getHeight() / 2;
//		
//		RectF r = new RectF(0, 0, getWidth(), getHeight());
//		
//		int parts = 10;
//		float partsize = (getWidth() / 2f) / parts;
//		
//		Paint paint = new Paint();
//		
//		for (int i = 0; i < parts; i++) {
//			
//			if (i % 2 == 0) {
//				paint.setColor(Color.BLACK);				
//			} else {
//				paint.setColor(Color.WHITE);
//			}
//
//			canvas.drawArc(r, 0, 45, true, paint);
//			
//			r.right -= partsize;
//			r.top += partsize;
//			r.bottom -= partsize;
//			r.left += partsize;
//			
//		}
//		
//		Log.d("tttsonar", getViewportRect().width() + " , " + getViewportRect().height());
		
//		canvas.drawLine(0, 0, getWidth(), getHeight(), new Paint());
	}	
	
	
	public void addValue(final hanse_msgs.ScanningSonar val) {
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {
				synchronized (values) {
					values.add(val);			
					while ((Math.PI*2) / angle < values.size()) {
						values.remove(0);
						if (lastPaintedIndex > -1) {
							lastPaintedIndex--;
						}
					}
				}
				checkBitmapSize();				
			}
		});
	}
}
