package net.smartpager.android.activity;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.utils.Log;
import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MuteNotificationsActivity extends Activity implements OnItemClickListener {
	
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private Bundle extras;
	
	List<String> items = new ArrayList<String>();
	List<Float> hourValues = new ArrayList<Float>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mute_notifications);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		extras = getIntent().getExtras();
		
		fillList();
		
		listView = (ListView) findViewById(R.id.mute_notifications_list_view);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
				
	}
	
	private void fillList() {
		items.add("For 30 minutes");
		items.add("For 1 hour");
		items.add("For 8 hours");
		items.add("For 24 hours");
		
		hourValues.add((float) 0.5);
		hourValues.add((float) 1.0);
		hourValues.add((float) 8.0);
		hourValues.add((float) 24.0);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			default:
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		
		final int threadId = extras.getInt("mThreadID");
		
		JSONObject jsonParams = new JSONObject();
		try {
			jsonParams.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
			jsonParams.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
			jsonParams.put("threadId", threadId);
			jsonParams.put("expiry", calculateExpiryDate(hourValues.get(position)));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringEntity entity = null;
		try {
			entity = new StringEntity(jsonParams.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		RequestParams params = new RequestParams();
//		params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
//		params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
//		params.put("threadId", threadId);
//		params.put("expiry", calculateExpiryDate(hourValues.get(position)));
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(getApplicationContext(), Constants.BASE_REST_URL + "/suppressThread", entity, "application/json", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.e("ChatActivityxx", "response: " + response);
				SmartPagerApplication.getInstance().muteThread(getApplicationContext(), threadId, calculateExpiryDate(hourValues.get(position)));
				endActivity();
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("Failure: " + responseBody);
			}
		});
	}
	
	public String calculateExpiryDate(Float hourPortion) {
		final long ONE_HOUR_IN_MILLISECONDS = 3600000;
		long minutesUntilExpiry = (long) (hourPortion * ONE_HOUR_IN_MILLISECONDS);
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		
		Date now = new Date();
		long curTimeInMilliseconds = now.getTime();
		Date expiryDate = new Date(curTimeInMilliseconds + minutesUntilExpiry);
		String expiryISO = df.format(expiryDate);
		
		return expiryISO;
	}
	
	public void endActivity() {
		Intent mIntent = new Intent();
		setResult(RESULT_OK, mIntent);
		finish();
	}
}
