/*
* Copyright (c) 2015, Institute of Computer Engineering, University of Lübeck
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 
* * Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
* 
* * Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* 
* * Neither the name of the copyright holder nor the names of its
*   contributors may be used to endorse or promote products derived from
*   this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package de.uniluebeck.iti.hanse.hansecontrol.views.overlays;

import hanse_msgs.WallDetection;

import java.util.LinkedList;
import java.util.List;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import de.uniluebeck.iti.hanse.hansecontrol.views.AbstractOverlay;
import de.uniluebeck.iti.hanse.hansecontrol.RosRobot;


public class WallDetectionOverlay extends AbstractOverlay implements MessageListener<hanse_msgs.WallDetection> {

	String topic;
	Subscriber<hanse_msgs.WallDetection> subscriber;
	List<WallDetection> detections = new LinkedList<WallDetectionOverlay.WallDetection>();
	
	View view;
	
	Paint circlePaint = new Paint();
	Paint linePaint = new Paint();
	
	public WallDetectionOverlay(Context context, String topic) {
		super(context);
		this.topic = topic;
		view = new View(context) {
			@Override
			protected void onDraw(Canvas canvas) {
//				canvas.drawLine(0, 0, getWidth(), getHeight(), new Paint());
				//TODO remove this
//				Bitmap img = BitmapManager.getInstance().getBitmap(getResources(), 
//						R.drawable.position_mapicon);
//				PointF p2 = getMapSurface().getPosOnViewport(50, 50);
//				RectF dst = new RectF(p2.x - img.getWidth() / 2, p2.y - img.getHeight() / 2,
//						p2.x + img.getWidth() / 2, p2.y + img.getHeight() / 2);
//				canvas.drawBitmap(img, null, dst, null);
				
//				MapSurface.drawMarker(20, 20, canvas);
//				if (lastPos != null && getMapSurface() != null && isVisible()) {
////					PointF pos = getMapSurface().getPosOnViewport((float)lastPos.getX(), (float)lastPos.getY());
////					MapSurface.drawMarker(pos.x, pos.y, canvas);
//					Bitmap img = BitmapManager.getInstance().getBitmap(getResources(), 
//							R.drawable.position_mapicon);
//					PointF pos = getMapSurface().getViewportPosFromPose((float)lastPos.getX(), (float)lastPos.getY());
//					RectF dstRect = new RectF(pos.x - img.getWidth() / 2, pos.y - img.getHeight() / 2,
//							pos.x + img.getWidth() / 2, pos.y + img.getHeight() / 2);
//					canvas.drawBitmap(img, null, dstRect, null);
//				}
				drawOverlay(canvas);
			}
		};
		addView(view);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		view.setLayoutParams(params);
		
		circlePaint.setColor(Color.WHITE);
		circlePaint.setAlpha(100);
		linePaint.setColor(Color.WHITE);
		linePaint.setAlpha(40);
	}
	
	@Override
	public void subscribe(ConnectedNode node) {
//		subscriber = node.newSubscriber(topic, hanse_msgs.WallDetection._TYPE);
//		subscriber.addMessageListener(this, MainScreen.MESSAGE_QUEUE);
	}
	
	private void removeOldValues() {
		while (detections.size() > 200) {
			detections.remove(0);
		}
	}
	
	private void addWallDetection(hanse_msgs.WallDetection det) {
		RosRobot rob = RosRobot.getInstance();
		PointF pose = rob.getPosition();
		Double rot = rob.getRoll();
		if (pose == null || rot == null || det.getDistances().length == 0) {
			return;
		}
		double distance = det.getDistances()[0];
		rot += det.getHeadPosition() + Math.PI / 2;
		
		double dx = Math.sin(rot) * distance;
		double dy = -Math.cos(rot) * distance;
		
		PointF obstaclePos = new PointF((float)(dx + pose.x), (float)(dy + pose.y));
		
		synchronized (detections) {			
			detections.add(new WallDetection(pose, obstaclePos, det.getHeadPosition()));
			removeOldValues();
		}
	}
	
	private void drawOverlay(Canvas canvas) {
		if (getMapSurface() != null && isVisible()) {
			circlePaint.setAlpha(30);
			synchronized (detections) {
				for (WallDetection d : detections) {
					PointF a = getMapSurface().getViewportPosFromPose(d.getPose().x, d.getPose().y);
					PointF b = getMapSurface().getViewportPosFromPose(d.getObstaclePos().x, d.getObstaclePos().y);
					
					canvas.drawLine(a.x, a.y, b.x, b.y, linePaint);
					canvas.drawCircle(b.x, b.y, 3, circlePaint);
					
					circlePaint.setAlpha(Math.min(circlePaint.getAlpha() + 5, 255));
				}
			}
			
			
			
//			synchronized (lastDets) {
//				for (WallDetection lastDet : lastDets) {
//					PointF robPos = lastRobPos.get(lastDets.indexOf(lastDet));
//					Log.d("walldetectionoverlay", String.format("Drawing... Distance: %f Angle: %f WallDet: %s", 
//							lastDet.getRange(), lastDet.getHeadPosition(), lastDet.getWallDetected() + ""));
//					for (double dist : lastDet.getDistances()) {
//						double dx = Math.sin(lastDet.getHeadPosition() - Math.PI / 2) * dist;
//						double dy = Math.cos(lastDet.getHeadPosition() - Math.PI / 2) * dist;
//						PointF vP = getMapSurface().getViewportPosFromPose(robPos.x - (float)dx, robPos.y + (float)dy);
//						PointF vRob = getMapSurface().getViewportPosFromPose(robPos.x, robPos.y);
//						canvas.drawCircle(vP.x, vP.y, 3, circlePaint);
//						canvas.drawLine(vP.x, vP.y, vRob.x, vRob.y, linePaint);
//					}
//				}
//			}
			
			
		}
	}
	
	@Override
	public void unsubscribe(ConnectedNode node) {
		if (subscriber != null) {
			subscriber.shutdown();
		}
	}

	@Override
	public OverlayType getOverlayType() {
		return OverlayType.WALL_DETECTION_OVERLAY;
	}

	@Override
	public String getRosTopic() {
		return topic;
	}

	@Override
	public void onNewMessage(hanse_msgs.WallDetection det) {
		
		addWallDetection(det);
		redraw();
		
		
		// TODO Auto-generated method stub
//		
//		
//		Log.d("poseoverlay", "Pose reveiced: x:" + wall.getPose().getPosition().getX() + " y:" + wall.getPose().getPosition().getY());
//		lastPos = wall.getPose().getPosition();
//		lastDet.getHeadPosition();
//		lastDet.getRange();
//		lastDet.getWallDetected();
		
		
		
		
//		if (det.getDistances().length > 0 && RosRobot.getInstance().getPosition() != null) {
//			synchronized (lastDets) {
//				while (lastDets.size() > 200) {
//					lastDets.remove(0);
//				}
//				lastDets.add(det);
//				lastRobPos.add(RosRobot.getInstance().getPosition());
//			}
//			redraw();	
//		}
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
	
	class WallDetection {
		PointF pose;
		PointF obstaclePos;
		double headPos;
		
		public WallDetection(PointF pose, PointF obstaclePos, double headPos) {
			super();
			this.pose = pose;
			this.obstaclePos = obstaclePos;
			this.headPos = headPos;
		}
		
		public PointF getPose() {
			return pose;
		}
		
		public PointF getObstaclePos() {
			return obstaclePos;
		}
		
		public double getHeadPos() {
			return headPos;
		}
	}
}
