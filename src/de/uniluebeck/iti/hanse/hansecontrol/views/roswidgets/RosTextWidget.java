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
	
	String rosTopic;
	Subscriber<T> subscriber;
	
	TextView textView;
	
	String rosTtype;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	int bufferedLines = 200;
	
	List<String> lines = new LinkedList<String>();
	
	public RosTextWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, 
			MainScreenFragment mainScreenFragment, String rosTtype) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment);
		this.rosTopic = rosTopic;
		this.rosTtype = rosTtype;
		textView = new TextView(context);
//		removeAllViews();
		
		Log.d("textwidgeterrfind", rosTopic + " : " + rosTtype);
		
		textView.setTextSize(18);
		textView.setTextColor(Color.WHITE);
		
		linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView topicHeader = new TextView(context);
		topicHeader.setText(rosTopic);
		topicHeader.setGravity(Gravity.CENTER);
		topicHeader.setTextColor(Color.LTGRAY);
		
		ScrollView scroll = new ScrollView(context);
//		scroll.setBackgroundColor(android.R.color.transparent);
//		scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//                LayoutParams.FILL_PARENT));
		scroll.addView(textView);
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(scroll);
		
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
							R.drawable.widgeticon_text);
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
		subscriber = node.newSubscriber(rosTopic, rosTtype);
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
				while (lines.size() > bufferedLines) {
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
	
	@Override
	public String getRosTopic() {
		return rosTopic;
	}
}
