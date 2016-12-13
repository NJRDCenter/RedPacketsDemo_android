package com.hyphenate.chatuidemo.redpacket;

import android.text.TextUtils;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.util.List;

/**
 * 工具类，帮助获取一些用户信息
 * 
 * Created by czhang on 16/11/22.
 */
public class RPHelper {

	/**
	 * 获取当前用户名
	 * 
	 * @return
	 */
	public static final String getCurrentUser() {
		return EMClient.getInstance().getCurrentUser();
	}

	/**
	 * 获取用户，包含头像和昵称
	 * 
	 * @param username
	 *            用户id
	 * @return
	 */
	public static final EaseUser getEaseUser(String username) {
		if (TextUtils.isEmpty(username)) {
			return null;
		}
		return EaseUserUtils.getUserInfo(username);
	}

	/**
	 * 获取群信息<br/>
	 * 群信息中可以得到所有的成员信息
	 * 
	 * @param groupId
	 *            群id
	 * @return
	 */
	public static final EMGroup getGroup(String groupId) {
		return EMClient.getInstance().groupManager().getGroup(groupId);
	}

	/**
	 * 从服务端异步获取加入的群
	 * 
	 * @param callback
	 */
	public void asyncGetJoinedGroupsFromServer(
			final EMValueCallBack<List<EMGroup>> callback) {
		EMClient.getInstance().groupManager()
				.asyncGetJoinedGroupsFromServer(callback);
	}

	/**
	 * 获取所有的群
	 * 
	 * @return 群列表
	 */
	public List<EMGroup> getAllGroups() {
		return EMClient.getInstance().groupManager().getAllGroups();
	}

}
