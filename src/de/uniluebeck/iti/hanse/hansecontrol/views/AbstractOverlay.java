package de.uniluebeck.iti.hanse.hansecontrol.views;

import org.ros.node.ConnectedNode;

import de.uniluebeck.iti.hanse.hansecontrol.MapSurface;
import de.uniluebeck.iti.hanse.hansecontrol.OverlayRegistry.OverlayType;

import android.content.Context;
import android.widget.RelativeLayout;


public abstract class AbstractOverlay extends RelativeLayout {
	
	private ConnectedNode node;
	private MapSurface mapSurface;
	
	private int mode = INVISIBLE;
	public static final int VISIBLE = 0;
	public static final int INVISIBLE = 1;
	
	public AbstractOverlay(Context context) {
		super(context);
		init();
	}

	private void init() {
		
	}

    public void setNode(ConnectedNode node) {
        this.node = node;
        if (mode == VISIBLE) {
        	subscribe(node);
        }
    }
    	
    public void setMapSurface(MapSurface mapSurface) {
        this.mapSurface = mapSurface;
        mapSurface.addListener(new MapSurface.MapSurfaceListener() {
			
			@Override
			public void mapSurfaceRedraw() {
				post(new Runnable() {
					
					@Override
					public void run() {
						redraw();
					}
				});
			}
		});
    }

    public MapSurface getMapSurface() {
		return mapSurface;
	}
    
	public abstract void subscribe(ConnectedNode node);
	
	public abstract void unsubscribe(ConnectedNode node);
	
	public abstract OverlayType getOverlayType();
	
	public abstract String getRosTopic();

	public abstract void redraw();
	
    public void setMode(int mode) {
        this.mode = mode;
        if (node != null) {
	        if (mode == INVISIBLE) {
				unsubscribe(node);
			} else if (mode == VISIBLE) {
				subscribe(node);
			}
        }
        redraw();
    }

	public boolean isVisible() {
		return mode == VISIBLE;
	}
}
