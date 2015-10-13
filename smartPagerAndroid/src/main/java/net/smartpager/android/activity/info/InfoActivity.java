package net.smartpager.android.activity.info;

import net.smartpager.android.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.TextView;

public class InfoActivity extends Activity {
	
	private TextView appVersion;
	private Button viewEULA;
	private Button emailSupport;
	private Button callSupport;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		viewEULA = (Button) findViewById(R.id.button_view_eula);
        viewEULA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(InfoActivity.this, EULAActivity.class);
                startActivity(i);
            }
        });

        emailSupport = (Button) findViewById(R.id.button_email_support);
        emailSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@smartpager.net"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Support Request from SmartPager Android");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    //Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        callSupport = (Button) findViewById(R.id.button_call_support);
        callSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:18883623948"));
                startActivity(callIntent);

            }
        });
		
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
