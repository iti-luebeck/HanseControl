package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

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
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;

public class RosTextWidget extends RosMapWidget implements MessageListener<std_msgs.String> {
	
	String rosTopic;
	Subscriber<std_msgs.String> subscriber;
	
	TextView textView;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	public RosTextWidget(int widgetID,	Context context, final String rosTopic, DragLayer dragLayer) {
		super(300, 200, widgetID, context, dragLayer);
		this.rosTopic = rosTopic;
		textView = new TextView(context);
//		removeAllViews();
		
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
	
	@Override
	public void subscribe(ConnectedNode node) {
		if (node != null) {
			subscriber = node.newSubscriber(rosTopic, std_msgs.String._TYPE);
			subscriber.addMessageListener(this);
		}
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}

	@Override
	public void onNewMessage(final std_msgs.String msg) {
		textView.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				textView.setText(msg.getData() + "\n" + textView.getText());
			}
		});
		Log.d("ros", "RosTextWidget" + getWidgetID() + " received: " + msg.getData());
	}
	
	@Override
	public WidgetType getWidgetType() {
		return WidgetType.ROS_TEXT_WIDGET;
	}
	
	@Override
	public String getRosTopic() {
		return rosTopic;
	}
}
