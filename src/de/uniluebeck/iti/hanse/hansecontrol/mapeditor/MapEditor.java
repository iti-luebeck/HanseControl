package de.uniluebeck.iti.hanse.hansecontrol.mapeditor;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;

import java.io.File;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MapSurface;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.MapLayer;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MapEditor extends Activity {

	Map map;
	MapLayer mapLayer;
	MapEditorMarkerLayer markerLayer;
	
	String imagePath;
	
	EditText mapName;
	Button chooseMap;
	TextView imagePathTextView;
	EditText rosP1_x;
	EditText rosP1_y;
	EditText rosP2_x;
	EditText rosP2_y;
	EditText errorText;
	Button saveMap;
	
	//workaround for startup
	boolean initialLayout = false;
	
	//intent request codes
	private static final int _ReqChooseFile = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_editor);
        map = MapManager.getInstance().getMapFromConfigPath(
        		getIntent().getStringExtra(MainScreenFragment.MAP_TO_EDIT_MESSAGE));
        if (map == null) {
        	map = new Map();
        }
        mapLayer = (MapLayer) findViewById(R.id.mapLayer);
        markerLayer = (MapEditorMarkerLayer) findViewById(R.id.markerLayer);
        mapName = (EditText) findViewById(R.id.mapName);
        chooseMap = (Button) findViewById(R.id.chooseImageButton);
        imagePathTextView = (TextView) findViewById(R.id.imagePathTextView);
        rosP1_x = (EditText) findViewById(R.id.point1_x_editText);
        rosP1_y = (EditText) findViewById(R.id.point1_y_editText);
        rosP2_x = (EditText) findViewById(R.id.point2_x_editText);
        rosP2_y = (EditText) findViewById(R.id.point2_y_editText);
        errorText = (EditText) findViewById(R.id.errorText);
        errorText.setVisibility(View.INVISIBLE);
        saveMap = (Button) findViewById(R.id.saveButton);
//     	loadMapData(map);
        
//        markerLayer.setVisibility(View.INVISIBLE);
        
        mapLayer.getMapSurface().setNoMapHintText("Please choose a map! (Button on the panel to the right)");
        
//        
        chooseMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MapEditor.this, FileChooserActivity.class);
				startActivityForResult(intent, _ReqChooseFile); //see flags in android-filechooser
			}
		});
        
        //workaround for startup
        View view = getWindow().getDecorView().findViewById(android.R.id.content);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            	if (!initialLayout) {
            		loadMapData(map);
            		initialLayout = true;
            	}
            }
        });
        
        mapLayer.getMapSurface().addListener(new MapSurface.MapSurfaceListener() {
			
			@Override
			public void mapSurfaceRedraw() {
				PointF m1 = mapLayer.getMapSurface().getViewportPosFromImagePos(
		    			map.getX1_OnImage(), map.getY1_OnImage());
		    	PointF m2 = mapLayer.getMapSurface().getViewportPosFromImagePos(
		    			map.getX2_OnImage(), map.getY2_OnImage());
				markerLayer.setMarker1Pos(m1.x, m1.y);
				markerLayer.setMarker2Pos(m2.x, m2.y);
			}
		});
        
        markerLayer.setPositionListener(new MapEditorMarkerLayer.MarkerPositionListener() {
			
			@Override
			public void positionUpdate() {
				PointF m1 = mapLayer.getMapSurface().getImagePosFromViewportPos(
						markerLayer.getMarker1Pos().x, markerLayer.getMarker1Pos().y);
				PointF m2 = mapLayer.getMapSurface().getImagePosFromViewportPos(
						markerLayer.getMarker2Pos().x, markerLayer.getMarker2Pos().y);
				map.setX1_OnImage((int)m1.x);
				map.setY1_OnImage((int)m1.y);
				map.setX2_OnImage((int)m2.x);
				map.setY2_OnImage((int)m2.y);
			}
		});
        
        saveMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (saveMapData()) {
					finish();
				}
			}
		});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == _ReqChooseFile) {
    		if (resultCode == RESULT_OK) {
    			List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
	            for (LocalFile f : files) {
	            	imagePath = f.getAbsolutePath();
	            	
	            	imagePathTextView.setText(imagePath);
	            	try{
	            		imagePathTextView.setText(new File(imagePath).getName());
	            	} catch (Exception e) {}
	            	
	            	map.setImagePath(imagePath);
	            	mapLayer.setMap(map);
	            	
	            	break;
	            }
    		}
    	}
    }
    
    private boolean saveMapData() {
    	try {
    		//perfoms checks
    		if (mapName.getText().toString().trim().isEmpty()) {
    			throw new Exception("The map name must not be empty.");
    		}
    		
    		try {
    			Bitmap img = BitmapManager.getInstance().getBitmap(imagePath);
    			if (img == null) { throw new Exception(); }
    		} catch (Exception e) {
				throw new Exception(String.format("The image at '%s' is invalid or too big, " +
						"max.: 2048x2048 pixels.", imagePath));
			}
    		
	    	//setup map instance
	    	map.setName(mapName.getText().toString());
	    	map.setImagePath(imagePath);
	    	//(points on image are already set)
	    	map.setX1_OnPose(Float.parseFloat(rosP1_x.getText().toString()));
	    	map.setY1_OnPose(Float.parseFloat(rosP1_y.getText().toString()));
	    	map.setX2_OnPose(Float.parseFloat(rosP2_x.getText().toString()));
	    	map.setY2_OnPose(Float.parseFloat(rosP2_y.getText().toString()));
	    	
//	    	throw new Exception("testerror!");
	    	
	    	if (map.getConfigPath().isEmpty()) {
	    		String dataDir = Environment.getExternalStorageDirectory()
	    				.getAbsolutePath() + File.separator + MapManager.MAPS_DIR + File.separator;
	    		String filePath =  dataDir + new File(imagePath).getName() + MapManager.MAPCONFIG_EXTENSION;
	    		if (new File(filePath).exists()) {
	    			int count = 2;
	    			String filePathPrefix = dataDir + new File(imagePath).getName();
	    			do {
	    				filePath = filePathPrefix + count++ + MapManager.MAPCONFIG_EXTENSION;
	    			} while (new File(filePath).exists());
	    		}
	    		map.setConfigPath(filePath);
	    	}
	    	map.writeConfigFile();	    	
	    	
	    	return true;
    	} catch (final Exception e) {
    		errorText.post(new Runnable() {
				@Override
				public void run() {
					errorText.setText(e.getMessage());
					errorText.setVisibility(View.VISIBLE);
				}
			});
			return false;
		}
    }
    
    private void loadMapData(final Map map) {
    	mapName.setText(map.getName());
    	
    	imagePath = map.getImagePath();
    	imagePathTextView.setText(imagePath);
    	try{
    		imagePathTextView.setText(new File(imagePath).getName());
    	} catch (Exception e) {}
    	
    	loadRosCoordinate(rosP1_x, map.getX1_OnPose());
    	loadRosCoordinate(rosP1_y, map.getY1_OnPose());
    	loadRosCoordinate(rosP2_x, map.getX2_OnPose());
    	loadRosCoordinate(rosP2_y, map.getY2_OnPose());
    	
    	
    	mapLayer.setMapLayerListener(new MapLayer.MapLayerListener() {
			
			@Override
			public void onMapSurfaceCreated(MapSurface mapSurface) {
				mapLayer.setMap(map);	
				
				PointF m1 = mapLayer.getMapSurface().getViewportPosFromImagePos(
		    			map.getX1_OnImage(), map.getY1_OnImage());
		    	PointF m2 = mapLayer.getMapSurface().getViewportPosFromImagePos(
		    			map.getX2_OnImage(), map.getY2_OnImage());
		    	if(map.getConfigPath().isEmpty()) {
		    		PointF mm1 = mapLayer.getMapSurface().getImagePosFromViewportPos(
		    				markerLayer.getWidth() / 2, rosP1_x.getY());
					PointF mm2 = mapLayer.getMapSurface().getImagePosFromViewportPos(
							markerLayer.getWidth() / 2, rosP2_x.getY());
					map.setX1_OnImage((int)mm1.x);
					map.setY1_OnImage((int)mm1.y);
					map.setX2_OnImage((int)mm2.x);
					map.setY2_OnImage((int)mm2.y);
		    		
		    		markerLayer.addMarker(markerLayer.getWidth() / 2, rosP1_x.getY());
		    		markerLayer.addMarker(markerLayer.getWidth() / 2, rosP2_x.getY());
		    	} else {
		    		markerLayer.addMarker(m1.x, m1.y);
		    		markerLayer.addMarker(m2.x, m2.y);		    		
		    	}
			}
		});
    }
    
    private void loadRosCoordinate(EditText editText, float x) {
    	editText.setText(Float.toString(x));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_editor, menu);
        return true;
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
//    	initialLayout = false;
    	MapManager.getInstance().reloadMapData();
    	MainScreen.getExecutorService().shutdownNow();
    }
}
