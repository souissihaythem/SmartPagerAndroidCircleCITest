package net.smartpager.android.web;

import net.smartpager.android.consts.Constants;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import biz.mobidev.framework.utils.Log;

public class SmartPagerHTTPClient {

	private static HttpClient mHttpClient;

	public static synchronized HttpClient getHttpClient() {
		if (mHttpClient == null) {
			Log.POINT();
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_PROTOCOL_CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);

			ConnManagerParams.setTimeout(params, Constants.HTTP_CLIENT_TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(params, Constants.HTTP_CLIENT_CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, Constants.HTTP_CLIENT_CONNECTION_TIMEOUT);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
			mHttpClient = new DefaultHttpClient(connectionManager, params);
		}
		return mHttpClient;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}