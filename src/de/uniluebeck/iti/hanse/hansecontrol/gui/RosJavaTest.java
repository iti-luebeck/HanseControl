package de.uniluebeck.iti.hanse.hansecontrol.gui;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

public class RosJavaTest {
	
}



class Listener extends AbstractNodeMain {

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("HanseControl2");
	}


	
}