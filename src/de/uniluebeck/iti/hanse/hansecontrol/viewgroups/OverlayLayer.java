package de.uniluebeck.iti.hanse.hansecontrol.viewgroups;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import de.uniluebeck.iti.hanse.hansecontrol.OverlayRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.views.AbstractOverlay;


public class OverlayLayer extends RelativeLayout {
	
	OverlayRegistry overlayRegistry;
	
	public OverlayLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public OverlayLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
    public OverlayLayer(Context context) {
		super(context);
		init(context);
	}
    
    private void init(Context context) {
    	overlayRegistry = new OverlayRegistry(context);
    	//TODO currently setting all overlays to visible , later the user should choose the visibility in the options menu!
    	for (AbstractOverlay o : overlayRegistry.getAllOverlays()) {
    		addOverlay(o);
    		o.setMode(AbstractOverlay.VISIBLE);
    	}
    }

	public void addOverlay(AbstractOverlay overlay) {
        addView(overlay);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) overlay.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		overlay.setLayoutParams(params);
    }  

//    public void removeOverlay(AbstractOverlay overlay) {
//        removeView(overlay);
//    }
    
    public OverlayRegistry getOverlayRegistry() {
		return overlayRegistry;
	}

}
