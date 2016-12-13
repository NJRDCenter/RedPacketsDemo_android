package com.hyphenate.chatuidemo.redpacket.http;

/**
 * http回调
 * 
 * Created by czhang on 16/11/30.
 */

public interface HttpResultCallback {
	void onSuccess(String result);

	void onFailure();

}
