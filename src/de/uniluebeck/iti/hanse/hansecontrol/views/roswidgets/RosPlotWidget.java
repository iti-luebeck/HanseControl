package de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
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
import android.text.format.DateFormat;
import android.util.AttributeSet;
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

public abstract class RosPlotWidget<T> extends RosMapWidget implements MessageListener<T> {
	//hanse_msgs.pressure
	String rosTopic;
	Subscriber<T> subscriber;
	
	TextView textView;
	
	Paint backgroundPaint = new Paint();
	
	LinearLayout linearLayout;
	
	PlotView plotView;
	
	public RosPlotWidget(int widgetID,	Context context, final String rosTopic, 
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
		
		plotView = new PlotView(getContext());
		
		linearLayout.addView(topicHeader);
		linearLayout.addView(plotView);
		
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
	
	public abstract String getDataTypeString();
	public abstract float getValue(T msg);
	
	@Override
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(rosTopic, getDataTypeString());
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
	long lastdraw = System.currentTimeMillis();

	@Override
	public void onNewMessage(final T msg) {
//		Log.d("plotwidget", getValue(msg) + "");
		MainScreen.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				plotView.addValue(getValue(msg));				
				if (System.currentTimeMillis() - lastdraw > 500) { //TODO schedule drawing instead
					redraw();
					lastdraw = System.currentTimeMillis();
				}
			}
		});
	}
	
//	@Override
//	public abstract WidgetType getWidgetType();
	
	@Override
	public String getRosTopic() {
		return rosTopic;
	}
	
	public void redraw() {
		plotView.post(new Runnable() {
			
			@Override
			public void run() {
				plotView.invalidate();
			}
		});
	}
}

class PlotView extends View {
	
	private Paint backgroundPaint = new Paint();
	private Paint linePaint = new Paint();
	private Paint markerPaint = new Paint();
	private Paint textPaint = new Paint();
	
	private Long minTime, maxTime;
	private List<Long> times = new LinkedList<Long>();
	private Float minValue, maxValue;
	private List<Float> values = new LinkedList<Float>();
	
	SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
	
	private int lastDataReduction = 0; //counts adding of points
	
	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;
	
	public PlotView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PlotView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PlotView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		//init colors
		backgroundPaint.setColor(Color.BLACK);
		backgroundPaint.setAlpha(160);
		
		linePaint.setColor(Color.WHITE);
		markerPaint.setColor(Color.RED);
		
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(13);
	}
	
//	public void setData(List<Short> data) {
//		this.data = data;
//	}
	
	private void scheduleDataReduction() {
		MainScreen.getExecutorService().execute(new Runnable() {
			
			@Override
			public void run() {
				synchronized (values) {
					//value constants
					int topBottomPadding = 20;
					float valueRange = maxValue - minValue;
					int vHeight = getHeight() - topBottomPadding * 2;
					float valueFactor = vHeight / (float) valueRange;
					
					//time constants
					long timeRange = maxTime - minTime;
					int vWidth = getWidth();
					float timeFactor = vWidth / (float) timeRange;
					
					//minimum distance for two points to be merged
					float distance = 4;
					
					//iterate over data
					for (int i = values.size() - 1; i >= 1 ; i--) {
						float x1 = ((times.get(i) - minTime) * timeFactor); 
						float y1 = vHeight - ((values.get(i) - minValue) * valueFactor) + topBottomPadding;
						float x2 = ((times.get(i-1) - minTime) * timeFactor); 
						float y2 = vHeight - ((values.get(i-1) - minValue) * valueFactor) + topBottomPadding;
						
						if (Math.abs(x1 - x2) < distance && Math.abs(y1 - y2) < distance) {
							//merge data points
							times.set(i, (times.get(i) + times.get(i-1)) / 2);
							values.set(i, ((values.get(i) + values.get(i-1)) / 2));
							times.remove(i-1);
							values.remove(i-1);
							i--;
							//i++;
						}
					}
				}
			}
		});
	}
	
	public void addValue(Float value) {
		synchronized (values) {
			values.add(value);
			long now = System.currentTimeMillis();
			times.add(now);
			minTime = minTime == null ? now : minTime;
			maxTime = now;
			minValue = minValue == null ? value : (value < minValue ? value : minValue);
			maxValue = maxValue == null ? value : (value > maxValue ? value : maxValue);
			if (lastDataReduction++ > 50) {
				lastDataReduction = 0;
				scheduleDataReduction();
			}
		}
	}
	
	private void drawPlot(Canvas canvas) {
		//value constants
		int topBottomPadding = 20;
		float valueRange = maxValue - minValue;
		int vHeight = getHeight() - topBottomPadding * 2;
		float valueFactor = vHeight / valueRange;
		
		//time constants
		long timeRange = maxTime - minTime;
		int vWidth = getWidth();
		float timeFactor = vWidth / (float) timeRange;
		
		//calc each pixel in graph
		for (int i = 0; i < values.size(); i++) {
			float x = ((times.get(i) - minTime) * timeFactor); 
			float y = vHeight - ((values.get(i) - minValue) * valueFactor) + topBottomPadding;
			canvas.drawCircle(x, y, 2, linePaint);
//			canvas.drawPoint(x, y, markerPaint);
			if (i == values.size() - 1) {
				//most recent value
				String text = dateFormatter.format(new Date(times.get(i))) + " / " + values.get(i);
				if (y < getHeight() / 2) {
					drawText(x - 3, y + 20, text, TOP_RIGHT, canvas);
				} else {
					drawText(x - 3, y - 20, text, BOTTOM_RIGHT, canvas);				
				}
			} else if (i == 0) {
				//first value
				String text = dateFormatter.format(new Date(times.get(i)));
				drawText(0 + 3, getHeight() - 3, text, BOTTOM_LEFT, canvas);
			}
		}
//		Log.d("plotwidget", "Datapoints: " + values.size());
		//draw max value text
		String text = "max: " + maxValue;
		drawText(getWidth() - 1 - 3, 3, text, TOP_RIGHT, canvas);
		//draw max time / min value text		
		text = dateFormatter.format(new Date(maxTime)) + " / min: " + minValue;
		drawText(getWidth() - 1 - 3, getHeight() - 3, text, BOTTOM_RIGHT, canvas);
		
		//draw debug info (values count)
		text = "values: " + values.size();
		drawText(3, 3, text, TOP_LEFT, canvas);
	}
	
	private void drawText(float x, float y, String text, int alignTo, Canvas canvas) {
		//String text = dateFormatter.format(new Date(time)) + " / " + value;
		float textWidth = textPaint.measureText(text);
		float textHeight = textPaint.getTextSize();
		switch (alignTo) {
//			case TOP_LEFT: canvas.drawText(text, x, y, textPaint); break;
//			case TOP_RIGHT: canvas.drawText(text, x - textWidth, y, textPaint); break; 
//			case BOTTOM_LEFT: canvas.drawText(text, x, y - textHeight, textPaint); break; 
//			case BOTTOM_RIGHT: canvas.drawText(text, x - textWidth, y - textHeight, textPaint); break; 
			case TOP_LEFT: canvas.drawText(text, x, y + textHeight, textPaint); break;
			case TOP_RIGHT: canvas.drawText(text, x - textWidth, y + textHeight, textPaint); break; 
			case BOTTOM_LEFT: canvas.drawText(text, x, y, textPaint); break; 
			case BOTTOM_RIGHT: canvas.drawText(text, x - textWidth, y, textPaint); break; 
		}
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (values) {			
//			Log.d("plotwidget", "Redrawing! Last value: " + (data.size() > 0 ? data.get(data.size() - 1) : "NONE"));
//			canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
//			canvas.drawLine(0, 0, getWidth(), getHeight(), new Paint());
			if (values.size() > 0) {
				drawPlot(canvas);
			}
		}
	}
}
