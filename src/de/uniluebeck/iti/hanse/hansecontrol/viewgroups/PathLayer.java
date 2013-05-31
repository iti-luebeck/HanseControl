package de.uniluebeck.iti.hanse.hansecontrol.viewgroups;

import geometry_msgs.Point;

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MapSurface;
import de.uniluebeck.iti.hanse.hansecontrol.OverlayRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.RosRobot;
import de.uniluebeck.iti.hanse.hansecontrol.mapeditor.MapEditorMarkerLayer.MarkerPositionListener;
import de.uniluebeck.iti.hanse.hansecontrol.views.AbstractOverlay;


public class PathLayer extends RelativeLayout {
	
	private static List<Target> targetPath = new LinkedList<Target>();
	
	TargetPathListener targetPathListener;
		
	Paint linePaint = new Paint();
	
	View drawView;
	
	RosRobot rosRobot;
	MapSurface mapSurface;
	
	
//	boolean firstrun = true;
	
	public PathLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public PathLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
    public PathLayer(Context context) {
		super(context);
		init(context);
	}
    
    private void init(Context context) {
    	linePaint.setColor(Color.BLACK);
    	linePaint.setAlpha(100);
    	linePaint.setStrokeWidth(6);
    	drawView = new View(getContext()) {
    		@Override
    		protected void onDraw(Canvas canvas) {
    			if (rosRobot.getPosition() != null && mapSurface != null && !targetPath.isEmpty()
    					&& !targetPath.get(0).getAnimationInProgress()) {
					PointF rosRp = rosRobot.getPosition();
					PointF rp = mapSurface.getViewportPosFromPose(rosRp.x, rosRp.y);
					PointF tp = targetPath.get(0).getPos();
					canvas.drawLine(rp.x, rp.y, tp.x, tp.y, linePaint);
				}
				for (int i = 0; i < targetPath.size() - 1; i++) {
					if (targetPath.get(i).getAnimationInProgress()
							|| targetPath.get(i + 1).getAnimationInProgress()) {
						continue;
					}
					PointF from = targetPath.get(i).getPos();
					PointF to = targetPath.get(i + 1).getPos();
					canvas.drawLine(from.x, from.y, to.x, to.y, linePaint);
				}
    		}
    	};
    	addView(drawView);
    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) drawView.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		drawView.setLayoutParams(params);
//		setTargetPathListener(new TargetPathListener() {
//			
//			@Override
//			public void targetPathUpdate() {
//				syncTargetPathToRosRobot();
//			}
//		});
		rosRobot = RosRobot.getInstance();
		rosRobot.addRobotUpdateListener(new RosRobot.RobotUpdateListener() {
			
			@Override
			public void update() {
				drawView.invalidate();
			}
		});
//		MainScreen.getExecutorService().execute(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
		Log.d("err", "before loading");
		loadTargets();
		Log.d("err", "after loading");
		
    }
 
//    private void loadTargetPositions(List<Target> tl) {
//    	//load targets
//		for (Target t : tl) {
//			PointF p = t.getRosPos();
//			PointF vp = mapSurface.getViewportPosFromPose(p.x, p.y);
//			Log.d("pathlayer", "loading: " + vp.x + " / " + vp.y);
//			addTarget(vp.x, vp.y);
//		}
//    }

	private void loadTargets() {
		//load targets
		List<Target> oldTargetPath = targetPath;
		targetPath = new LinkedList<Target>();
		for (final Target t : oldTargetPath) {
			final Target target = createNewTargetInstance();
			targetPath.add(target);
			target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
	        {
	            @Override
	            public void onGlobalLayout()
	            {
	            	Log.d("err", "layoutlistener start, mapsuface == null is " + (mapSurface == null));
	            	target.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	            	if (mapSurface != null && t.getRosPos() != null) {
	            		try {
	            		PointF pos = mapSurface.getViewportPosFromPose(t.getRosPos().x, t.getRosPos().y);
	            		target.setRosPos(t.getRosPos());
	            		setTargetPos(target, pos.x, pos.y);
	            		} catch (Exception e) {
							Log.d("err", "" + (t.getRosPos() == null));
						}
	            	} else {
	            		target.setRosPos(t.getRosPos());
	            		target.setWaitForMapSurfaceFlag(true);
	            	}
	            	target.setAnimationInProgress(false);
	            	Log.d("err", "layoutlistener end");
	            }
	        });
			
		}
		invalidate();
	}
    
//    private void syncTargetPathToRosPositions() {
//    	if (rosRobot != null && mapSurface != null) {
//    		List<PointF> res = new LinkedList<PointF>();
//    		for (Target target : targetPath) {
//    			PointF p = target.getPos();
//    			res.add(mapSurface.getPoseFromViewportPos(p.x, p.y));
//    		}
//    		rosRobot.setTargetPath(res);
//    	}
//    }
//    
//    private void syncRosPositionsToTargetPath() {
//		if (rosRobot != null && mapSurface != null) {
//			for (Target target : targetPath) {
//				removeView(target);
//			}
//			targetPath.clear();
//			for (PointF target : rosRobot.getTargetPath()) {
//				PointF p = mapSurface.getViewportPosFromPose(target.x, target.y);
//				Log.d("pathlayer", "Adding target at " + p.x + "/" + p.y);
//				addTarget(p.x, p.y, false);
//			}    		
//		}
//    } 
    
    public void onLongPress(MotionEvent event) {
    	Log.d("pathlayer", "longpress");
    	boolean robotPos = rosRobot.getPosition() != null && mapSurface != null;    	
    	for (int i = robotPos ? -1 : 0; i < targetPath.size() - 1; i++) {
    		PointF from = null;
    		PointF to = targetPath.get(i + 1).getPos();
    		if (i == -1) {
    			//workaround for path segment between current robot position and first target
    			PointF rosRp = rosRobot.getPosition();
    			from = mapSurface.getViewportPosFromPose(rosRp.x, rosRp.y);
    		} else {
    			from = targetPath.get(i).getPos();
    		}
    		double lineDist = calcDistToLine(new PointF(event.getX(), event.getY()), from, to);
    		Log.d("pathlayer", "Line dist between " + i + " and " + (i+1) + " is " + lineDist);
    		if (lineDist < 50) {
    			Target t = addTarget(event.getX(), event.getY());
    			targetPath.remove(targetPath.size() - 1);
    			targetPath.add(i + 1, t);
    			return;
    		}
    	}
    	addTarget(event.getX(), event.getY());
    }
    
    private Target createNewTargetInstance() {
    	Bitmap image = BitmapManager.getInstance().getBitmap(getResources(), R.drawable.target_mapicon);
    	Target target = new Target(getContext(), image);
    	addView(target);
		target.setVisibility(View.INVISIBLE);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) target.getLayoutParams();
		params.width = image.getWidth();
		params.height = image.getHeight();
		target.setLayoutParams(params);
    	return target;
    }
    
    public synchronized Target addTarget(final float x, final float y) {
    	final Target target = createNewTargetInstance();
//		if (target.getWidth() == 0) {
			//addTarget was called before layout finished
			target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
	        {
	            @Override
	            public void onGlobalLayout()
	            {
	            	target.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	            	setTargetPos(target, x, y);
	            	target.setAnimationInProgress(true);
            		target.post(new Runnable() {
	    				@Override
	    				public void run() {
	    					target.playScaleAnimation(10, 1, 500).setAnimationListener(new Animation.AnimationListener() {
								
								@Override
								public void onAnimationStart(Animation animation) {}
								
								@Override
								public void onAnimationRepeat(Animation animation) {}
								
								@Override
								public void onAnimationEnd(Animation animation) {
									target.setAnimationInProgress(false);
									drawView.invalidate();
								}
							});
	    				}
	    			});
            		Log.d("err", "addtarget after posting runnable, mapsuface == null is " + (mapSurface == null));
            		if (mapSurface != null) {
            			Log.d("err", "setting ros pos start");
    					target.setRosPos(mapSurface.getPoseFromViewportPos(x, y));
    					Log.d("err", "setting ros pos end");
    				}
	            }
	        });
//		} 
//		else {
//			Log.d("pathlayer", "direct layout!");
//			setTargetPos(target, x, y);
//			target.post(new Runnable() {
//				@Override
//				public void run() {
//					target.playScaleAnimation(10, 1, 500);	
//				}
//			});
//			if (mapSurface != null) {
//				target.setRosPos(mapSurface.getPoseFromViewportPos(x, y));
//			}
//		}
		targetPath.add(target);
		
		if (targetPathListener != null) {
			targetPathListener.targetPathUpdate();
		}
		
		return target;
	}
    
    public List<Target> getTargetPath() {
    	return targetPath;
    }	
    
    public void setTargetPos(final Target target, float x, float y) {
		Log.d("err", "setting target pos x=" + x + " y=" + y);
    	if (x == Float.NaN || y == Float.NaN) {
			x = -100;
			y = -100;
		}
    	
    	final float left = x - target.getWidth() / 2;
		final float top = y - target.getHeight() / 2;
		final float right = x + target.getWidth() / 2;
		final float bottom = y + target.getHeight() / 2;		
		
		target.post(new Runnable() {
			@Override
			public void run() {
				if (left < 0 || top < 0 || right >= getWidth() || bottom >= getHeight()) {
					target.setVisibility(View.INVISIBLE);
				} else {
					target.setVisibility(View.VISIBLE);
				}
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) target.getLayoutParams();
				params.leftMargin = (int) left;
				params.topMargin = (int) top;
				target.setLayoutParams(params);
				target.invalidate();
				drawView.invalidate();
			}
		});	
    }
    
    private class TargetAction {
    	String name;
    	
    	public TargetAction(String name) {
			this.name = name;
		}
    }
    
    private class Target extends View {
		Bitmap image;
		
		float mx;
		float my;
		
		private final static int YOFFSET = 50;
		
		private PointF rosPos;
		
		private boolean isAnimationInProgress = false;
		private boolean waitForMapSurfaceFlag = false;
		
		private List<TargetAction> actions;
		
		public Target(Context context, Bitmap image) {
			super(context);
			this.image = image;
		}
		
		public void setWaitForMapSurfaceFlag(boolean isWaiting) {
			waitForMapSurfaceFlag = isWaiting;
		}
		
		public boolean getWaitForMapSurfaceFlag() {
			return waitForMapSurfaceFlag;
		}

		public ScaleAnimation playScaleAnimation(float from, float to, long duration) {
			ScaleAnimation animation = new ScaleAnimation(from, to, from, to, 
					Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
			animation.setDuration(duration);
//			LayoutAnimationController animationController = new LayoutAnimationController(animation);
//			animationController.start();
			Target.this.startAnimation(animation);
			return animation;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(image, null, new RectF(0, 0, getWidth(), getHeight()), null);
		}
		
		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				mx = event.getX();
				my = event.getY();
//				playScaleAnimation(1, 1.5f, 200);
				return true;
			}
			if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				Log.d("errfind", "mx=" + mx + " eventx=" + event.getX());
//				setX(getX() - mx + (event.getX()));
//				setY(getY() - my - YOFFSET + (event.getY()));
				
				if (Math.abs(mx - event.getX()) < 4 || Math.abs(my - event.getY()) < 4) {
					return true;
				}
				
				float x = getX() - mx + (event.getX());
				float y = getY() - my - YOFFSET + (event.getY());
				
				//prevent target from dragging offscreen
				x = Math.max(getWidth() / 2, x);
				x = Math.min(PathLayer.this.getWidth() - getWidth() / 2 - 2, x);
				y = Math.max(getHeight() / 2, y);
				y = Math.min(PathLayer.this.getHeight() - getHeight() / 2 - 2, y);
				
				if (mapSurface != null) {
					setRosPos(mapSurface.getPoseFromViewportPos(x, y));
				}
				setTargetPos(this, x, y);
				
				
//				invalidate();
				return true;
			}
			if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
				if (targetPathListener != null) {
					targetPathListener.targetPathUpdate();
				}
				if (Math.abs(mx - event.getX()) < 4 || Math.abs(my - event.getY()) < 4) {
					//single tap on target
					showTargetOptions(this);
//					playScaleAnimation(10, 1, 100000);
					return true;
				} else {
//					playScaleAnimation(1.5f, 1, 200);
				}
				return true;
			}
			return false;
		}
		
		public PointF getPos() {
			return new PointF(getX() + getWidth() / 2, getY() + getHeight() / 2);
		}
		
		public PointF getRosPos() {
			return rosPos;
		}
		
		public void setRosPos(PointF rosPos) {
			this.rosPos = rosPos;
		}
		
		public void setAnimationInProgress(boolean isAnimationInProgress) {
			this.isAnimationInProgress = isAnimationInProgress;
		}
		
		public boolean getAnimationInProgress() {
			return this.isAnimationInProgress;
		}
		
		public List<TargetAction> getActions() {
			return actions;
		}
		
		public void setActions(List<TargetAction> actions) {
			this.actions = actions;
		}
	}
    
    
	public static interface TargetPathListener {
		public void targetPathUpdate();
	}
	
	public void setTargetPathListener(TargetPathListener targetPathListener) {
		this.targetPathListener = targetPathListener;
	}
	
	private double calculateDistance(PointF p1, PointF p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
	
//	private double calcDistToLine(PointF pos, PointF lineStart, PointF lineEnd) {
//		double a = calculateDistance(lineStart, pos);
//		double b = calculateDistance(pos, lineEnd);
//		double c = calculateDistance(lineStart, lineEnd);
//		
//		//return triangle height h_c
//		double a2 = a * a;
//		double b2 = b * b;
//		double c2 = c * c;
//		
//		return Math.sqrt(2*(a2*b2+b2*c2+c2*a2)-(Math.pow(a, 4)+Math.pow(b, 4)+Math.pow(c, 4)))/(2*c);
//	}
	
	private double calcDistToLine(PointF point, PointF lineStart, PointF lineEnd) {
		double lineLength = calculateDistance(lineStart, lineEnd);
		if (calculateDistance(point, lineStart) > lineLength || calculateDistance(point, lineEnd) > lineLength) {
			return Double.MAX_VALUE;
		} else {
			double length = Math.sqrt((lineEnd.x-lineStart.x)*(lineEnd.x-lineStart.x)
					+(lineEnd.y-lineStart.y)*(lineEnd.y-lineStart.y));
		    return Math.abs((point.x-lineStart.x)*(lineEnd.y-lineStart.y)
		    		-(point.y-lineStart.y)*(lineEnd.x-lineStart.x))/length;
		}
	}
	
	public void setMapSurface(final MapSurface mapSurface) {
		Log.d("err", "setting mapsurface!");
		this.mapSurface = mapSurface;
		mapSurface.addListener(new MapSurface.MapSurfaceListener() {

			@Override
			public void mapSurfaceRedraw() {
//				if (firstrun) {
//					firstrun = false;
//					final List<Target> tl = targetPath;
//					targetPath = new LinkedList<Target>();
//					MainScreen.getExecutorService().execute(new Runnable() {
//						
//						@Override
//						public void run() {
//							loadTargetPositions(tl); //load targets from previous tab
//						}
//					});
//					return;
//				}
				for (Target t : targetPath) {
					if (t.getRosPos() != null && mapSurface != null) {
						PointF ros = t.getRosPos();
						PointF p = mapSurface.getViewportPosFromPose(ros.x, ros.y);
						setTargetPos(t, p.x, p.y);
					}
					if (t.getRosPos() != null && t.getWaitForMapSurfaceFlag()) {
						PointF pos = mapSurface.getViewportPosFromPose(t.getRosPos().x, t.getRosPos().y);	            		
	            		setTargetPos(t, pos.x, pos.y);
	            		t.setWaitForMapSurfaceFlag(false);
					}
				}
			}
		});
	}
	
	public void showTargetOptions(Target target) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		CharSequence[] items = {"a", "b", "c"};
		builder.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("pathlayer", which + "");
			}
		});
		builder.create().show();
	}
}
