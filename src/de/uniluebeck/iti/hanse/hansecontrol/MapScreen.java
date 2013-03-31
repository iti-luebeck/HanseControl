package de.uniluebeck.iti.hanse.hansecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class MapScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_screen);
		final Button sendbutton = (Button) findViewById(R.id.sendButton);
		final TextView history = (TextView) findViewById(R.id.history);
		final TextView sendtext = (TextView) findViewById(R.id.messageText);
		//final ScrollView historyScroll = (ScrollView) findViewById(R.id.scrollView1);
		
		history.setText("s0\ns1\ns2\ns3\ns4\ns5\ns6\ns7\ns8\ns9\ns10\ns11\ns12\ns13\ns14\ns15\ns16\ns17\ns18\ns19\ns20\ns21\ns22\ns23\ns24\ns25\ns26\ns27\ns28\ns29\ns30\ns31\ns32\ns33\ns34\ns35\ns36\ns37\ns38\ns39\n");
		history.setKeyListener(null);
		sendtext.setText("gggg");
		sendbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CharSequence text = sendtext.getText();
				history.setText(text + "\n" + history.getText());
				sendtext.setText("");
				//history.scrollTo(0, history.getLayout().getLineBottom(40));
				//historyScroll.fullScroll(View.FOCUS_DOWN);
				//history.scrollBy(0, Integer.parseInt(sendtext.getText().toString()));
			}
		});
		
//		historyScroll.setOnFocusChangeListener(new OnFocusChangeListener() {
//
//	        @Override
//	        public void onFocusChange(View v, boolean hasFocus) {
//	            if (hasFocus) {
//	            	historyScroll.fullScroll(View.FOCUS_DOWN);
//	            }
//	        }
//	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_screen, menu);
		return true;
	}
}
