package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.android.BitmapFromImage;
import org.ros.android.view.RosImageView;
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

public class RosImageWidget extends RosMapWidget implements MessageListener<sensor_msgs.CompressedImage> {
	
	String rosTopic;
	Subscriber<sensor_msgs.CompressedImage> subscriber;
	
	TextView textView;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	Bitmap bitmap;	
	View imageView;
	
	public RosImageWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment);
		Log.d("compressedImageTest", "Creating widget instance");
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
		
		imageView = new ImageView(getContext());
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(imageView = new View(getContext()) {
			@Override
			public void draw(Canvas canvas) {
				synchronized (this) {					
					if (bitmap != null && !bitmap.isRecycled()) {
						canvas.drawBitmap(bitmap, null, scaleToBox(bitmap.getWidth(), bitmap.getHeight(), 0, 0, getWidth()-1, getHeight()-1) , null);
					}
				}
			}
		});
		
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
							R.drawable.widgeticon_image);
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
		Log.d("compressedImageTest", "subscribing... " + sensor_msgs.CompressedImage._TYPE);
		subscriber = node.newSubscriber(rosTopic, sensor_msgs.CompressedImage._TYPE);
		subscriber.addMessageListener(this);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}
	
	@Override
	public void onNewMessage(final sensor_msgs.CompressedImage image) {
		ChannelBuffer cb = image.getData();
		
		
		//offset, jpeg image starts at bytes "FF D8", see http://de.wikipedia.org/wiki/JPEG_File_Interchange_Format
		int byteArrayOffset = 34;

		final Bitmap oldBitmap = bitmap;
		MainScreen.getExecutorService().schedule(new Runnable() {
			
			@Override
			public void run() {
				synchronized (imageView) {
					if (oldBitmap != null) {
						oldBitmap.recycle();
					}
				}
			}
		}, 500, TimeUnit.MILLISECONDS);
		
		synchronized (imageView) {
			bitmap = BitmapFactory.decodeByteArray(cb.array(), cb.readerIndex() + byteArrayOffset, cb.readableBytes());
		}
		
		if (bitmap != null && !bitmap.isRecycled()) {
			setRatio(bitmap.getWidth() / (float) bitmap.getHeight());
		}
		
		imageView.post(new Runnable() {
			
			@Override
			public void run() {
				imageView.invalidate();
			}
		});
		
		//TODO remove debugging code
//		try {
//			FileOutputStream fout = new FileOutputStream(new File("/sdcard/imagefile"));
//			fout.write(cb.array(), cb.readerIndex() + byteArrayOffset, cb.readableBytes());
//			fout.flush();
//			fout.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
