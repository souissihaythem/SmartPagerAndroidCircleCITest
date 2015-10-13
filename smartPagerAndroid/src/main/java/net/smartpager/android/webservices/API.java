package net.smartpager.android.webservices;


import net.smartpager.android.model2.BaseMessaging;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;


public interface API {

	@Multipart
	@POST("/rest/messageTypes")
	void getMessageTypes(@Part("smartPagerID") String smartPagerID,
			   @Part("uberPassword") String uberPassword,
			   Callback<BaseMessaging> callback);
}

