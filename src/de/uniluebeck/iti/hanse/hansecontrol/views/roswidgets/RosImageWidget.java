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
	
	Subscriber<sensor_msgs.CompressedImage> subscriber;
	
	ImageSurface imageSurface;
	boolean drawingInProgress = false;
	
	Bitmap bitmap;	
	
	//offset, jpeg image starts at bytes "FF D8", see http://de.wikipedia.org/wiki/JPEG_File_Interchange_Format
	static final int byteArrayOffset = 34;
	
	public RosImageWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment,
				rosTopic, WidgetType.ROS_IMAGE_WIDGET);
		
		imageSurface = new ImageSurface(getContext());
		imageSurface.setZOrderOnTop(true);
		setContent(imageSurface);
		setControlsListener(new ControlsListener() {
			
			@Override
			public void onControlsVisible() {
				imageSurface.setVisibility(View.INVISIBLE);
			}
			@Override
			public void onControlsInvisible() {}
		});
	}
	
	@Override
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(getRosTopic(), sensor_msgs.CompressedImage._TYPE);
		subscriber.addMessageListener(this);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}
	
	private void drawImage() {
//		Log.d("RosImageWidget", Thread.currentThread().getName() + "drawImage()...");
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
//			Log.d("RosImageWidget", Thread.currentThread().getName() + "...drawImage() finished drawing!");
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
	}
}

class ImageSurface extends SurfaceView implements SurfaceHolder.Callback {

	SurfaceHolder surfaceHolder = null;
		
	public ImageSurface(Context context) {
		super(context);
		getHolder().addCallback(this);
//		Log.d("RosImageWidget", "SurfaceView created...");
	}
	
	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
//		Log.d("RosImageWidget", "SurfaceHolder ready...");
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) { }

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = null;
	}
	
}

