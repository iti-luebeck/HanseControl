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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import de.uniluebeck.iti.hanse.hansecontrol.PerformanceBenchmark;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry.WidgetType;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;

public abstract class RosPlotWidget<T> extends RosMapWidget implements MessageListener<T> {
	Subscriber<T> subscriber;
	
	PlotView plotView;
	
	ScheduledFuture redrawFuture;
	
	private int redrawInterval = 500; //in ms, must be a value available in the config dialog!
	private long lastdraw = System.currentTimeMillis();
	
	public RosPlotWidget(int widgetID,	Context context, final String rosTopic, 
			DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment,
			WidgetType widgetType) {
		super(300, 200, widgetID, context, dragLayer, mapWidgetRegistry, mainScreenFragment,
				rosTopic, widgetType);
		plotView = new PlotView(getContext());	
		setContent(plotView);
	}
	
	public abstract String getDataTypeString();
	public abstract float getValue(T msg);
	
	@Override
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(getRosTopic(), getDataTypeString());
		subscriber.addMessageListener(this, MainScreen.MESSAGE_QUEUE);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
		plotView.clearValues();
	}

	@Override
	public void onNewMessage(final T msg) {
//		Log.d("plotwidget", getValue(msg) + "");
		MainScreen.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				plotView.addValue(getValue(msg));				
				
				if (System.currentTimeMillis() - lastdraw > redrawInterval) { //TODO schedule drawing instead
					//draw immediately
					redraw();
					lastdraw = System.currentTimeMillis();
				} else if (redrawFuture == null) {
					//schedule drawing at next possible moment with respect to the redrawInterval
					redrawFuture = MainScreen.getExecutorService().schedule(new Runnable() {
						
						@Override
						public void run() {
							redraw();
							lastdraw = System.currentTimeMillis();
							redrawFuture = null;
						}
					}, redrawInterval - (System.currentTimeMillis() - lastdraw), TimeUnit.MILLISECONDS);
				}
			}
		});
	}
	
	public void redraw() {
		plotView.post(new Runnable() {
			
			@Override
			public void run() {
				plotView.invalidate();
			}
		});
	}
	
	@Override
	public void showConfigDialog(FragmentManager fragmentManager) {
		ConfigDialog configDialog = new ConfigDialog(plotView.getTimeSpan(), redrawInterval, plotView.isConnectDots()) {
			
			@Override
			public void updateSettigs(long timespan, int redrawInterval,
					boolean connectDots) {
				plotView.setTimeSpan(timespan);
				RosPlotWidget.this.redrawInterval = redrawInterval;
				plotView.setConnectDots(connectDots);
				redraw();
			}
		};
		configDialog.show(fragmentManager, null);
	}
	
	public abstract static class ConfigDialog extends DialogFragment {

		private long timespan;
		private int redrawInterval;
		private boolean connectDots;
		
		public ConfigDialog(long timespan, int redrawInterval,
				boolean connectDots) {
			super();
			this.timespan = timespan;
			this.redrawInterval = redrawInterval;
			this.connectDots = connectDots;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			LayoutInflater inflater = getActivity().getLayoutInflater();

			builder.setTitle("Plotwidget Settings");

					
			final View view = inflater.inflate(R.layout.dialog_config_plotwidget, null);
			builder.setView(view);
			
			final SeekBar timespanSeekBar = (SeekBar) view.findViewById(R.id.timespanSeekBar);
			final TextView timespanTextView = (TextView) view.findViewById(R.id.timespanValueTextView);

			final SeekBar redrawIntervalSeekBar = (SeekBar) view.findViewById(R.id.redrawintervalSeekBar);
			final TextView redrawIntervalTextView = (TextView) view.findViewById(R.id.redrawintervalValueTextView);

			final Switch connectDotsSwitch = (Switch) view.findViewById(R.id.connectDotsSwitch);
			
			final String[] timespanStrArr = new String[]{
					"5s", "10s", "15s", "20s", "25s", "30s", "35s", "40s", "45s", "50s", "55s", 
					"1m", "2m", "3m", "4m", "5m"
			}; 
			final long[] timespanValArr = new long[]{
					5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 
					1*60000, 2*60000, 3*60000, 4*60000, 5*60000
			};
			
			final String[] redrawIntervalStrArr = new String[]{
					"100ms", "200ms", "300ms", "400ms", "500ms", "600ms", "700ms", "800ms", "900ms", 
					"1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s"
			};
			final int[] redrawIntervalValArr = new int[]{
					100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000 
			};
					
			timespanSeekBar.setMax(timespanStrArr.length - 1);
			timespanSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { }
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { }
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					timespanTextView.setText(timespanStrArr[progress]);
				}
			});
			
			redrawIntervalSeekBar.setMax(redrawIntervalStrArr.length - 1);
			redrawIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) { }
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) { }
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					redrawIntervalTextView.setText(redrawIntervalStrArr[progress]);
				}
			});
			
			//load current values
			timespanSeekBar.setProgress(Arrays.binarySearch(timespanValArr, this.timespan));
			redrawIntervalSeekBar.setProgress(Arrays.binarySearch(redrawIntervalValArr, this.redrawInterval));
			timespanTextView.setText(timespanStrArr[timespanSeekBar.getProgress()]);
			redrawIntervalTextView.setText(redrawIntervalStrArr[redrawIntervalSeekBar.getProgress()]);
			
			connectDotsSwitch.setChecked(this.connectDots);
			
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							ConfigDialog.this.getDialog().cancel();
						}
					});

			builder.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							updateSettigs(timespanValArr[timespanSeekBar.getProgress()],
									redrawIntervalValArr[redrawIntervalSeekBar.getProgress()], connectDotsSwitch.isChecked());
						}
					});

			return builder.create();

		}

		public abstract void updateSettigs(long timespan, int redrawInterval, boolean connectDots);

	}
	
	@Override
	public void loadPrefs(String tabPrefix, SharedPreferences prefs) {
		super.loadPrefs(tabPrefix, prefs);
		redrawInterval = prefs.getInt(tabPrefix + getRosTopic() + this.getClass().getName() + "redrawInterval", redrawInterval);
		plotView.setTimeSpan(prefs.getLong(tabPrefix + getRosTopic() + this.getClass().getName() + "timespan", plotView.getTimeSpan()));
		plotView.setConnectDots(prefs.getBoolean(tabPrefix + getRosTopic() + this.getClass().getName() + "connectDots", plotView.isConnectDots()));
	}
	
	@Override
	public void savePrefs(String tabPrefix, Editor ed) {
		super.savePrefs(tabPrefix, ed);
		ed.putInt(tabPrefix + getRosTopic() + this.getClass().getName() + "redrawInterval", redrawInterval);
		ed.putLong(tabPrefix + getRosTopic() + this.getClass().getName() + "timespan", plotView.getTimeSpan());
		ed.putBoolean(tabPrefix + getRosTopic() + this.getClass().getName() + "connectDots", plotView.isConnectDots());
	}
	
}

class PlotView extends View {
	
	private Paint backgroundPaint = new Paint();
	private Paint linePaint = new Paint();
	private Paint markerPaint = new Paint();
	private Paint textPaint = new Paint();
	private Paint textDebugInfoPaint = new Paint();
	private Paint connectDotsPaint = new Paint();
	
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
	
	private long timeSpan = 30000; //in ms, must be a value available in the config dialog!
	
	boolean connectDots = true;
	
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
	
	public void clearValues() {
		synchronized (values) {
			minValue = null;
			maxValue = null;
			minTime = null;
			maxTime = null;
			values.clear();
			times.clear();
		}
	}
	
	private void init() {
		//init colors
		backgroundPaint.setColor(Color.BLACK);
		backgroundPaint.setAlpha(160);
		
		linePaint.setColor(Color.WHITE);
		markerPaint.setColor(Color.RED);
		
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(13);
		
		textDebugInfoPaint.setColor(Color.WHITE);
		textDebugInfoPaint.setTextSize(10);
		textDebugInfoPaint.setAlpha(200);
		
		connectDotsPaint.setColor(Color.WHITE);
		connectDotsPaint.setAlpha(150);
		connectDotsPaint.setStrokeWidth(1);
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
	
	private void updateMinMaxValue() {
		if (values.size() == 0) {
			return;
		}
		minValue = values.get(0);
		maxValue = values.get(0);
		for (Float val : values) {
			if (val < minValue) {
				minValue = val;
			} else if (val > maxValue) {
				maxValue = val;
			}
		}
	}
	
	public void clearValuesOutsideTimeSpan() {
		long spanStart = times.get(times.size() - 1) - timeSpan;
		boolean updateMinMax = false;
		while (times.get(0) < spanStart) {
			times.remove(0);
			float removedValue = values.remove(0);
			if (!updateMinMax && (minValue == removedValue || maxValue == removedValue)) {
				updateMinMax = true;
			}
		}
		if (values.size() == 0) {
			return;
		}
		if (updateMinMax) {
			updateMinMaxValue();
		}
		minTime = times.get(0);
	}
	
	public void addValue(Float value) {
		synchronized (values) {
			values.add(value);
			long now = System.currentTimeMillis();
			times.add(now);
			clearValuesOutsideTimeSpan();
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
	PerformanceBenchmark benchmark = new PerformanceBenchmark("RosPlotWidget", getContext());
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
		
		//used to draw lines between dots
		float lastX = -1, lastY = -1;
		
		//calc each pixel in graph
		for (int i = 0; i < values.size(); i++) {
			float x = ((times.get(i) - minTime) * timeFactor); 
			float y = vHeight - ((values.get(i) - minValue) * valueFactor) + topBottomPadding;
			if (connectDots && i != 0) {
				canvas.drawLine(lastX, lastY, x, y, connectDotsPaint);
			}
			canvas.drawCircle(x, y, 2, linePaint);
			
//			canvas.drawPoint(x, y, markerPaint);
			if (i == values.size() - 1) {
				//most recent value
				String text = dateFormatter.format(new Date(times.get(i))) + " / " + values.get(i);
				if (y < getHeight() / 2) {
					drawText(x - 3, y + 20, text, TOP_RIGHT, canvas, textPaint);
				} else {
					drawText(x - 3, y - 20, text, BOTTOM_RIGHT, canvas, textPaint);				
				}
			} else if (i == 0) {
				//first value
				String text = dateFormatter.format(new Date(times.get(i)));
				drawText(0 + 3, getHeight() - 3, text, BOTTOM_LEFT, canvas, textPaint);
			}
			lastX = x;
			lastY = y;
		}
//		Log.d("plotwidget", "Datapoints: " + values.size());
		//draw max value text
		String text = "max: " + maxValue;
		drawText(getWidth() - 1 - 3, 3, text, TOP_RIGHT, canvas, textPaint);
		//draw max time / min value text		
		text = dateFormatter.format(new Date(maxTime)) + " / min: " + minValue;
		drawText(getWidth() - 1 - 3, getHeight() - 3, text, BOTTOM_RIGHT, canvas, textPaint);
		
		//draw debug info (values count)
		text = "values: " + values.size();
		drawText(3, 3, text, TOP_LEFT, canvas, textDebugInfoPaint);
		
		benchmark.log();
	}
	
	private void drawText(float x, float y, String text, int alignTo, Canvas canvas, Paint paint) {
		//String text = dateFormatter.format(new Date(time)) + " / " + value;
		float textWidth = paint.measureText(text);
		float textHeight = paint.getTextSize();
		switch (alignTo) {
//			case TOP_LEFT: canvas.drawText(text, x, y, textPaint); break;
//			case TOP_RIGHT: canvas.drawText(text, x - textWidth, y, textPaint); break; 
//			case BOTTOM_LEFT: canvas.drawText(text, x, y - textHeight, textPaint); break; 
//			case BOTTOM_RIGHT: canvas.drawText(text, x - textWidth, y - textHeight, textPaint); break; 
			case TOP_LEFT: canvas.drawText(text, x, y + textHeight, paint); break;
			case TOP_RIGHT: canvas.drawText(text, x - textWidth, y + textHeight, paint); break; 
			case BOTTOM_LEFT: canvas.drawText(text, x, y, paint); break; 
			case BOTTOM_RIGHT: canvas.drawText(text, x - textWidth, y, paint); break; 
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
	
	public void setTimeSpan(long timeSpan) {
		this.timeSpan = timeSpan;
	}
	
	public long getTimeSpan() {
		return timeSpan;
	}
	
	public void setConnectDots(boolean connectDots) {
		this.connectDots = connectDots;
	}
	
	public boolean isConnectDots() {
		return connectDots;
	}
}
