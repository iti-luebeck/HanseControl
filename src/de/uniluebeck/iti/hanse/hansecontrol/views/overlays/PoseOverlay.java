package de.uniluebeck.iti.hanse.hansecontrol.views.overlays;

import geometry_msgs.PoseStamped;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MapSurface;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.OverlayRegistry.OverlayType;
import de.uniluebeck.iti.hanse.hansecontrol.RosRobot;
import de.uniluebeck.iti.hanse.hansecontrol.views.AbstractOverlay;


public class PoseOverlay extends AbstractOverlay implements MessageListener<geometry_msgs.PoseStamped> {

	String topic;
	Subscriber<geometry_msgs.PoseStamped> subscriber;
	geometry_msgs.Point lastPos;
	View view;
	
	public PoseOverlay(Context context, String topic) {
		super(context);
		this.topic = topic;
		view = new View(context) {
			@Override
			protected void onDraw(Canvas canvas) {
				
				//TODO remove this
//				Bitmap img = BitmapManager.getInstance().getBitmap(getResources(), 
//						R.drawable.position_mapicon);
//				PointF p2 = getMapSurface().getPosOnViewport(50, 50);
//				RectF dst = new RectF(p2.x - img.getWidth() / 2, p2.y - img.getHeight() / 2,
//						p2.x + img.getWidth() / 2, p2.y + img.getHeight() / 2);
//				canvas.drawBitmap(img, null, dst, null);
				
//				MapSurface.drawMarker(20, 20, canvas);
				if (lastPos != null && getMapSurface() != null && isVisible()) {
//					PointF pos = getMapSurface().getPosOnViewport((float)lastPos.getX(), (float)lastPos.getY());
//					MapSurface.drawMarker(pos.x, pos.y, canvas);
					Bitmap img = BitmapManager.getInstance().getBitmap(getResources(), 
							R.drawable.position_mapicon);
					Bitmap imgPointer = BitmapManager.getInstance().getBitmap(getResources(),
							R.drawable.position_orientation_mapicon);
					PointF pos = getMapSurface().getViewportPosFromPose((float)lastPos.getX(), (float)lastPos.getY());
					RectF dstRect = new RectF(pos.x - img.getWidth() / 2, pos.y - img.getHeight() / 2,
							pos.x + img.getWidth() / 2, pos.y + img.getHeight() / 2);
					
					RectF dst2Rect = new RectF(pos.x - imgPointer.getWidth() / 2, pos.y - imgPointer.getHeight() / 2,
							pos.x + imgPointer.getWidth() / 2, pos.y + imgPointer.getHeight() / 2);
					
					if (RosRobot.getInstance().getRoll() != null) {
						double rot = RosRobot.getInstance().getRoll();
						canvas.save();
						canvas.rotate((float)((360/(2*Math.PI))*(-rot + Math.PI * 0.75f)), pos.x, pos.y);
						canvas.drawBitmap(imgPointer, null, dst2Rect, null);
//						canvas.drawLine(pos.x, pos.y - 100, pos.x, pos.y + 100, new Paint());
						
						canvas.restore();
					}
					
					canvas.drawBitmap(img, null, dstRect, null);
					
				}
			}
		};
		addView(view);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		view.setLayoutParams(params);
	}
	
	@Override
	public void subscribe(ConnectedNode node) {
		subscriber = node.newSubscriber(topic, geometry_msgs.PoseStamped._TYPE);
		subscriber.addMessageListener(this, MainScreen.MESSAGE_QUEUE);
	}

	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}

	@Override
	public OverlayType getOverlayType() {
		return OverlayType.POSE_OVERLAY;
	}

	@Override
	public String getRosTopic() {
		return topic;
	}

	@Override
	public void onNewMessage(PoseStamped pose) {
		// TODO Auto-generated method stub
//		Log.d("poseoverlay", "Pose reveiced: x:" + pose.getPose().getPosition().getX() + " y:" + pose.getPose().getPosition().getY());
		lastPos = pose.getPose().getPosition();
		redraw();
	}
	
	@Override
	public void redraw() {
		view.post(new Runnable() {
			
			@Override
			public void run() {
				view.invalidate();
			}
		});
	}
}
