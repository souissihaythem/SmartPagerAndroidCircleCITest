package net.smartpager.android.webservices;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import net.smartpager.android.consts.Constants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RestClient {
	private static API apiService;

	public static void setupRestClient() 
	{
		Gson gson = new GsonBuilder()
		.setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
		.create();
			
		RestAdapter restAdapter = new RestAdapter.Builder()
		.setLogLevel(RestAdapter.LogLevel.FULL)
		.setEndpoint(Constants.BASE_URL)
		.setConverter(new GsonConverter(gson))
		.build();

		apiService = restAdapter.create(API.class);
	}

	public static API getApiService()
	{	
		if (apiService == null) setupRestClient();
		return apiService;
	}

}
