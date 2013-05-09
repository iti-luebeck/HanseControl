package de.uniluebeck.iti.hanse.hansecontrol.views;

import geometry_msgs.PoseStamped;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import de.uniluebeck.iti.hanse.hansecontrol.MapSurface;
import de.uniluebeck.iti.hanse.hansecontrol.OverlayRegistry.OverlayType;

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
//				MapSurface.drawMarker(20, 20, canvas);
				if (lastPos != null && getMapSurface() != null && isVisible()) {
					PointF pos = getMapSurface().getPosOnViewport((float)lastPos.getX(), (float)lastPos.getY());
					MapSurface.drawMarker(pos.x, pos.y, canvas);
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
		subscriber.addMessageListener(this);
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
		Log.d("poseoverlay", "Pose reveiced: x:" + pose.getPose().getPosition().getX() + " y:" + pose.getPose().getPosition().getY());
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
