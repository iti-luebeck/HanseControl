package de.uniluebeck.iti.hanse.hansecontrol.views;

import org.ros.node.ConnectedNode;

import de.uniluebeck.iti.hanse.hansecontrol.HanseControlNode;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import android.content.Context;
import android.util.Log;

public abstract class RosMapWidget extends MapWidget {

	private ConnectedNode node;
	
	public RosMapWidget(int defaultWidth, int defaultHeight, int widgetID,
			Context context, DragLayer dragLayer) {
		super(defaultWidth, defaultHeight, widgetID, context, dragLayer);	
	}
	
	public void setNode(ConnectedNode node) {
		this.node = node;
		if (getMode() == FULLSIZE_MODE) {
			subscribe(node);
		}
	}
	
	@Override
	public void setMode(int mode) {
		super.setMode(mode);
		if (node == null) {
			Log.e("ros", "RosMapWidget: setMode() called, but node is null!");
			return;
		}
		if (mode == ICON_MODE) {
			unsubscribe(node);
		} else if (mode == FULLSIZE_MODE) {
			subscribe(node);
		}
	}
	
	public abstract void subscribe(ConnectedNode node);
	
	public abstract void unsubscribe(ConnectedNode node);
	
//	public abstract String getWidgetType();
}
