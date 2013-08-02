package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import kobuki_msgs.SensorState;

import geometry_msgs.PoseStamped;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import sensor_msgs.Imu;

import android.graphics.PointF;
import android.util.Log;


/**
 * This class provides the global position and 
 * target paths for the controlled robot.
 * The App will be subscribed to the topics used here at all times.
 * 
 * @author Stefan Hueske
 */
public class RosRobot {
	
	private ConnectedNode node;
	
	Subscriber<geometry_msgs.PoseStamped> positionSubscriber;
	Subscriber<sensor_msgs.Imu> imuSubscriber;
	
	//TODO implement GUI component to change topics used by this class
	
	MessageListener<geometry_msgs.PoseStamped> positionListener;
	MessageListener<sensor_msgs.Imu> imuListener;
	
	String positionTopic = "/hanse/position/estimate"; //TODO change static topic
	PointF lastPosition;
	
	String imuTopic = "/hanse/imu";
	
	private static RosRobot instance = new RosRobot();
	
	List<RobotPositionListener> robotPositionListeners = new LinkedList<RobotPositionListener>();
	List<RobotOrientationListener> robotOrientationListeners = new LinkedList<RobotOrientationListener>();
	
	
	//TODO remove this
//	double min = Double.MAX_VALUE;
//	double max = Double.MIN_VALUE;
	
	Double roll;
	Double pitch;
	Double yaw;
	
	UpdateOrientationRunnable updateOrientationRunnable;
	private int orientationUpdateInterval = 250;
	private long lastOrientationUpdate;
	
	public RosRobot() {
		positionListener = new MessageListener<PoseStamped>() {
			
			@Override
			public void onNewMessage(final PoseStamped pose) {
				
				geometry_msgs.Point p = pose.getPose().getPosition();
				lastPosition = new PointF((float)p.getX(), (float)p.getY());
				
				synchronized (RosRobot.this) {
					if (updateOrientationRunnable == null) {
						updateOrientationRunnable = new UpdateOrientationRunnable(pose);
						MainScreen.getExecutorService().schedule(updateOrientationRunnable, 
								orientationUpdateInterval, TimeUnit.MILLISECONDS);
					} else {
						updateOrientationRunnable.setLastPose(pose);
					}					
				}
				
				notifyPositionListeners();
				
//				
//				notifyUpdateListeners();
//				
//				double q0 = pose.getPose().getOrientation().getX();
//				double q1 = pose.getPose().getOrientation().getY();
//				double q2 = pose.getPose().getOrientation().getZ();
//				double q3 = pose.getPose().getOrientation().getW();
////				
////				float[] angles = toAngles(new float[3], (float)q0, (float)q1, (float)q2, (float)q3);
////				Log.d("imu", String.format("POSE: Angles: 1:%.5f 2:%.5f 3:%.5f", angles[0], angles[1], angles[2]));
//				
//				
//				double roll = Math.atan2(2*(q0*q1+q2*q3), 1 - 2*(q1*q1 + q2*q2));
//				double pitch = Math.asin(2*(q0*q2 - q3*q1));
//				double yaw = Math.atan2(2*(q0*q3+q1*q2), 1 - 2*(q2*q2 + q3*q3));
//				
//
////				Log.d("imu", "POSE: roll: " + String.format("%.5f", roll) + " pitch: " 
////						+ String.format("%.5f", pitch) + " yaw: " + String.format("%.5f", yaw));
////				Log.d("imu", "IMU : roll: " + String.format("%.5f", imuRoll) + " pitch: " 
////						+ String.format("%.5f", imuPitch) + " yaw: " + String.format("%.5f", imuYaw));
			}
		};
	}
	
	public Double getRoll() {
		return roll;
	}

	public Double getPitch() {
		return pitch;
	}

	public Double getYaw() {
		return yaw;
	}
	
	private void updateOrientation(PoseStamped pose) {
		double q0 = pose.getPose().getOrientation().getX();
		double q1 = pose.getPose().getOrientation().getY();
		double q2 = pose.getPose().getOrientation().getZ();
		double q3 = pose.getPose().getOrientation().getW();
		
		roll = Math.atan2(2*(q0*q1+q2*q3), 1 - 2*(q1*q1 + q2*q2));
		pitch = Math.asin(2*(q0*q2 - q3*q1));
		yaw = Math.atan2(2*(q0*q3+q1*q2), 1 - 2*(q2*q2 + q3*q3));
		
		notifyOrientationListeners();
		
		Log.d("imu", "POSE: roll: " + String.format("%.5f", roll) + " pitch: " 
		+ String.format("%.5f", pitch) + " yaw: " + String.format("%.5f", yaw));
	}
	
	private class UpdateOrientationRunnable implements Runnable {

		PoseStamped lastPose;
		boolean running = false;
		
		public UpdateOrientationRunnable(PoseStamped lastPose) {
			this.lastPose = lastPose;
		}

		public synchronized void setLastPose(PoseStamped lastPose) {
			if (running) {
				return;
			}
			this.lastPose = lastPose;
		}

		@Override
		public synchronized void run() {
			running = true;
			updateOrientation(lastPose);
			updateOrientationRunnable = null;
		}
		
	}
	
	//TODO remove toAngles
//	public float[] toAngles(float[] angles, float x, float y, float z, float w) {
//        if (angles == null) {
//            angles = new float[3];
//        } else if (angles.length != 3) {
//            throw new IllegalArgumentException("Angles array must have three elements");
//        }
//
//        float sqw = w * w;
//        float sqx = x * x;
//        float sqy = y * y;
//        float sqz = z * z;
//        float unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
//        // is correction factor
//        float test = x * y + z * w;
//        if (test > 0.499 * unit) { // singularity at north pole
//            angles[1] = 2 * (float)Math.atan2(x, w);
//            angles[2] = (float) Math.PI / 2;
//            angles[0] = 0;
//        } else if (test < -0.499 * unit) { // singularity at south pole
//            angles[1] = -2 * (float)Math.atan2(x, w);
//            angles[2] = -(float) Math.PI / 2;
//            angles[0] = 0;
//        } else {
//            angles[1] = (float)Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading 
//            angles[2] = (float)Math.asin(2 * test / unit); // pitch or attitude
//            angles[0] = (float)Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
//        }
//        return angles;
//    }
	
	public static RosRobot getInstance() {
		return instance;
	}
	
	public void setNode(ConnectedNode connectedNode) {
		this.node = connectedNode;
		positionSubscriber = node.newSubscriber(positionTopic, geometry_msgs.PoseStamped._TYPE);
		positionSubscriber.addMessageListener(positionListener);
//		imuSubscriber = node.newSubscriber(imuTopic, sensor_msgs.Imu._TYPE);
//		imuSubscriber.addMessageListener(imuListener);
	}

	public PointF getPosition() {
		return lastPosition;
	}

	private void notifyPositionListeners() {
		synchronized (robotPositionListeners) {
			for (final RobotPositionListener listener : robotPositionListeners) {
				MainScreen.getExecutorService().execute(new Runnable() {
					@Override
					public void run() {
						listener.positionUpdate();
					}
				});
			}
		}
	}
	
	private void notifyOrientationListeners() {
		synchronized (robotOrientationListeners) {
			for (final RobotOrientationListener listener : robotOrientationListeners) {
				MainScreen.getExecutorService().execute(new Runnable() {
					@Override
					public void run() {
						listener.orientationUpdate();
					}
				});
			}
		}
	}
	
	public interface RobotPositionListener {
		public void positionUpdate();
	}
	
	public interface RobotOrientationListener {
		public void orientationUpdate();
	}
	
	public void addRobotPositionListener(RobotPositionListener listener) {
		synchronized (robotPositionListeners) {			
			robotPositionListeners.add(listener);
		}
	}
	
	public void addRobotOrientationListener(RobotOrientationListener listener) {
		synchronized (robotOrientationListeners) {			
			robotOrientationListeners.add(listener);
		}
	}
}
