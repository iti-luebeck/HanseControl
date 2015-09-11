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

    public void deleteOverlay(AbstractOverlay overlay) {
    	overlay.setMode(AbstractOverlay.INVISIBLE);
        removeView(overlay);
        overlayRegistry.deleteOverlay(overlay);
    }
    
    public OverlayRegistry getOverlayRegistry() {
		return overlayRegistry;
	}

}
