package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import java.util.concurrent.Future;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.android.BitmapFromImage;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
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

public class RosImageWidget extends RosMapWidget implements MessageListener<sensor_msgs.Image> {
	

	String rosTopic;
	Subscriber<sensor_msgs.Image> subscriber;
	
	TextView textView;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	ImageView imageView;	
	
	public RosImageWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment);
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
		
		imageView = new ImageView(getContext());
		
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(imageView);
		
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
		subscriber = node.newSubscriber(rosTopic, sensor_msgs.Image._TYPE);
		subscriber.addMessageListener(this);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}

	//TODO change this
	boolean decodingInProgress = false;
	
	@Override
	public void onNewMessage(final sensor_msgs.Image image) {
		if (decodingInProgress) {
			return; //skip image
		}
		decodingInProgress = true;
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {
				Log.d("rosimagewidget", "decoding image...");
				// Bitmap bitmap = new BitmapFromImage().call(image);
				sensor_msgs.Image message = image;
				Bitmap bitmap = Bitmap.createBitmap((int) message.getWidth(),
						(int) message.getHeight(), Bitmap.Config.ARGB_8888);
				int opCount = 0;
				long timesum = 0;
				int pixelsum = 0;
				ChannelBuffer data = message.getData();
				for (int x = 0; x < message.getWidth(); x++) {
					long start = System.currentTimeMillis();
					for (int y = 0; y < message.getHeight(); y++) {
						byte red = data.getByte((y * message.getStep() + 3 * x));
						byte green = data.getByte((y * message.getStep()
								+ 3 * x + 1));
						byte blue = data.getByte((y * message.getStep()
								+ 3 * x + 2));
						// byte red = 0;
						// byte green = 0;
						// byte blue = 0;
						
						bitmap.setPixel(x, y, Color.argb(255, red & 0xFF,
								green & 0xFF, blue & 0xFF));
						opCount++;
					}
					Log.d("rosimagewidget", "decoding row: " + x
							+ ", current OP-Count: " + opCount);
					Log.d("rosimagewidget",
							"Time per pixel (in ms): "
									+ ((System.currentTimeMillis() - start) / (float) message
											.getHeight())
									+ "Avg: "
									+ ((timesum += (System.currentTimeMillis() - start)) / (float) (pixelsum += message
											.getHeight())));
				}

				Log.d("rosimagewidget", "image received: " + bitmap.getWidth()
						+ "x" + bitmap.getHeight());
				decodingInProgress = false;
			}
		});
		
//		new Thread() {
//			public void run() {
//				
//			}
//		}.start();
	}
	
	@Override
	public WidgetType getWidgetType() {
		return WidgetType.ROS_IMAGE_WIDGET;
	}
	
	@Override
	public String getRosTopic() {
		return rosTopic;
	}
}
