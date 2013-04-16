package de.uniluebeck.iti.hanse.hansecontrol;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

public class HanseControlNode extends AbstractNodeMain {

//	ConnectedNode nod
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("hanse_control");
	}
	
	
}
