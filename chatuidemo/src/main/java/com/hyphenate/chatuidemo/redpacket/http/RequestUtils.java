package com.hyphenate.chatuidemo.redpacket.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 随意写的
 * 
 * Created by czhang on 16/11/30.
 */

public class RequestUtils {
	// public static final String SIGN_BASE_URL =
	// "http://172.24.9.200:8091/redpkgop/im/sign/";
	public static final String SIGN_BASE_URL = "https://mt.jdpay.com/redpkgop/im/sign/";

	/**
	 * 同步获取get请求结果
	 * 
	 * @param url
	 *            请求地址
	 * @return 请求结果
	 */
	public static String requestByHttpGetSync(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			// 获取HttpClient对象
			HttpClient httpClient = new DefaultHttpClient();
			// 获取HttpResponse实例
			HttpResponse httpResp = httpClient.execute(httpGet);
			// 判断是够请求成功
			if (httpResp.getStatusLine().getStatusCode() == 200) {
				// 获取返回的数据
				String result = EntityUtils.toString(httpResp.getEntity(),
						"UTF-8");
				return result;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * http get请求
	 * 
	 * @param url
	 * @param callback
	 */
	public static void requestByHttpGet(final String url,
			final HttpResultCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 新建HttpGet对象
				try {
					HttpGet httpGet = new HttpGet(url);
					// 获取HttpClient对象
					HttpClient httpClient = new DefaultHttpClient();
					// 获取HttpResponse实例
					HttpResponse httpResp = httpClient.execute(httpGet);
					// 判断是够请求成功
					if (httpResp.getStatusLine().getStatusCode() == 200) {
						// 获取返回的数据
						final String result = EntityUtils
								.toString(httpResp.getEntity(), "UTF-8");
						Log.i("CPProtocolGroup", result + "");
						new Handler(Looper.getMainLooper())
								.post(new Runnable() {
							@Override
							public void run() {
								if (callback == null) {
									return;
								}
								callback.onSuccess(result);
							}
						});
					} else {
						new Handler(Looper.getMainLooper())
								.post(new Runnable() {
							@Override
							public void run() {
								if (callback != null) {
									callback.onFailure();
								}
							}
						});
					}
				} catch (Exception e) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onFailure();
							}
						}
					});
				}

			}
		}).start();

	}

	/**
	 * 产生风控信息
	 *
	 * @return
	 */
	public static String genTestRiskInfo(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("eid", "redpkgtest");
			jsonObject.put("isCompany", "1");
			jsonObject.put("isErp", "1");
			jsonObject.put("userLevel", "G");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

}
