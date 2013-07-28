package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
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
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
	
	ImageSurface imageSurface;
	boolean drawingInProgress = false;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	Bitmap bitmap;	
	
	//offset, jpeg image starts at bytes "FF D8", see http://de.wikipedia.org/wiki/JPEG_File_Interchange_Format
	static final int byteArrayOffset = 34;
	
	public RosImageWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment);
		Log.d("compressedImageTest", "Creating widget instance");
		this.rosTopic = rosTopic;
		
		linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		
		TextView topicHeader = new TextView(context);
		topicHeader.setText(rosTopic);
		topicHeader.setGravity(Gravity.CENTER);
		topicHeader.setTextColor(Color.LTGRAY);
		
		imageSurface = new ImageSurface(getContext());
		
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(imageSurface);
		
		imageSurface.setZOrderOnTop(true);
		setControlsListener(new ControlsListener() {
			
			@Override
			public void onControlsVisible() {
				imageSurface.setVisibility(View.INVISIBLE);
			}
			@Override
			public void onControlsInvisible() {}
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
		
//		imageSurface.setVisibility(View.INVISIBLE);
		
//		new Thread() {
//			public void run() {
//				while(true) {
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					Log.d("RosImageWidget", Thread.currentThread().getName() + " h:" + imageSurface.getHeight() + " w:" + imageSurface.getWidth() 
//							+ " isInvisible:" + (imageSurface.getVisibility() == View.INVISIBLE));
//					drawImage();
//					imageSurface.post(new Runnable() {
//						
//						@Override
//						public void run() {
//							imageSurface.setVisibility(View.VISIBLE);
//							imageSurface.invalidate();
//						}
//					});
//				}
//			};
//		}.start();
		
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
	
	private void drawImage() {
		Log.d("RosImageWidget", Thread.currentThread().getName() + "drawImage()...");
		try {
//			imageSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
			if (imageSurface.getSurfaceHolder() == null) {
				return;
			}
			Canvas canvas = imageSurface.getHolder().lockCanvas(null);
			if (canvas == null) {
				Log.e("RosImageWidget", ".getHolder().lockCanvas() returned null!");
				return;
			}
			canvas.drawBitmap(bitmap, null, scaleToBox(bitmap.getWidth(), bitmap.getHeight(), 0, 0,
					imageSurface.getWidth()-1, imageSurface.getHeight()-1) , null);		
//			Paint white = new Paint();
//			white.setColor(Color.WHITE);
//			canvas.drawRect(new Rect(0, 0, 50, 50), white);
//			
//			canvas.drawLine(0, 0, 50, 50, new Paint());
			
			imageSurface.getHolder().unlockCanvasAndPost(canvas);	
			Log.d("RosImageWidget", Thread.currentThread().getName() + "...drawImage() finished drawing!");
		} catch (Exception e) {
			Log.e("RosImageWidget", "Error while drawing image", e);
			try {
				imageSurface.getSurfaceHolder().unlockCanvasAndPost(null);				
			} catch (Exception e1) { }
		}
	}
	
//	@Override
//	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//		super.onSizeChanged(w, h, oldw, oldh);
//		imageSurface.setVisibility(View.INVISIBLE);	
////		MainScreen.getExecutorService().schedule(new Runnable() {
////			
////			@Override
////			public void run() {
////				drawImage();
////			}
////		}, 100, TimeUnit.MILLISECONDS);
//	}
	
	@Override
	public void onNewMessage(final sensor_msgs.CompressedImage image) {
//		Log.d("RosImageWidget", "OnNewMsg...");
		
		
		
		
		if (imageSurface.getVisibility() != View.VISIBLE && !isControlsVisible()) {
			imageSurface.post(new Runnable() {
				@Override
				public void run() {
					imageSurface.setVisibility(View.VISIBLE);					
				}
			});
		} else if (isControlsVisible()) {
			return;
		}
		
		synchronized (imageSurface) {
			if (drawingInProgress) {
				return;
			}
			drawingInProgress = true;
		}
		final Bitmap oldBitmap = bitmap;
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {
//				Log.d("RosImageWidget", "Drawing image...");
				try {
					ChannelBuffer cb = image.getData();
					if (oldBitmap != null) {
						oldBitmap.recycle();							
					}
					bitmap = BitmapFactory.decodeByteArray(cb.array(), cb.readerIndex() + byteArrayOffset, cb.readableBytes());
					if (bitmap != null && !bitmap.isRecycled()) {
						setRatio(bitmap.getWidth() / (float) bitmap.getHeight());
					}
					drawImage();
				} catch (Exception e) {
					Log.e("RosImageWidget", "Error while decoding image", e);	
				} finally {
					drawingInProgress = false;
				}
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

class ImageSurface extends SurfaceView implements SurfaceHolder.Callback {

	SurfaceHolder surfaceHolder = null;
		
	public ImageSurface(Context context) {
		super(context);
		getHolder().addCallback(this);
		Log.d("RosImageWidget", "SurfaceView created...");
	}
	
	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		Log.d("RosImageWidget", "SurfaceHolder ready...");
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = null;
	}
	
}

