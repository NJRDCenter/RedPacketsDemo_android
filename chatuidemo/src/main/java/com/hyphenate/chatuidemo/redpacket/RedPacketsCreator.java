package com.hyphenate.chatuidemo.redpacket;

import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.jd.redpackets.manager.RedPacketType;

/**
 * 创建红包
 *
 * Created by czhang on 16/11/21.
 */
public class RedPacketsCreator {

	private static final String REDPKG_NORMAL = "普通红包";
	private static final String REDPKG_EXCLUSIVE = "专属红包";
	private static final String REDPKG_PERSONAL = "个人红包";
	private static final String REDPKG_REWARD = "打赏红包";

	/**
	 * 创建红包消息
	 * 
	 * @param params
	 *            红包参数
	 * @param toChatUsername
	 *            发送给的用户
	 * @return 红包消息
	 */
	public static EMMessage createRPMessage(RedpkgParams params,
			String toChatUsername) {
		if (params == null || TextUtils.isEmpty(params.type)) {
			return null;
		}
		String redpkgStr = " ";
		if (RedPacketType.TYPE_PERSONAL.equals(params.type)) {
			redpkgStr = REDPKG_PERSONAL;
		} else if (RedPacketType.TYPE_GROUP.equals(params.type)) {
			redpkgStr = REDPKG_NORMAL;
		} else if (RedPacketType.TYPE_EXCLUSIVE.equals(params.type)) {
			redpkgStr = REDPKG_EXCLUSIVE;
		} else if (RedPacketType.TYPE_REWARD.equals(params.type)) {
			redpkgStr = REDPKG_REWARD;
		}
		EMMessage message = EMMessage.createTxtSendMessage(redpkgStr,
				toChatUsername);
		message.setAttribute(RPConstants.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE,
				true);
		message.setAttribute(RPConstants.MESSAGE_ATTR_REDPKG_ID,
				params.redpkgId);
		if (!TextUtils.isEmpty(params.senderUserId)) {
			message.setAttribute(RPConstants.MESSAGE_ATTR_SENDER_USERID,
					params.senderUserId);
		}
		if (!TextUtils.isEmpty(params.desc)) {
			message.setAttribute(RPConstants.MESSAGE_ATTR_REDPKG_DESC,
					params.desc);
		}
		message.setAttribute(RPConstants.MESSAGE_ATTR_REDPKG_TYPE, params.type);
		return message;
	}

	/**
	 * 创建红包接收消息
	 *
	 * @param senderUserId
	 *            红包发送者id
	 * @param receiverUserId
	 *            红包接收者id
	 * @param toChatUsername
	 *            发送给的用户
	 * @return 红包消息
	 */
	public static EMMessage createRevRPMessage(String senderUserId,
			String receiverUserId, String toChatUsername) {
		String redpkgStr = "红包领取";
		EMMessage message = EMMessage.createTxtSendMessage(redpkgStr,
				toChatUsername);
		message.setAttribute(RPConstants.MESSAGE_ATTR_REV_IS_RED_PACKET_MESSAGE,
				true);
		if (!TextUtils.isEmpty(senderUserId)) {
			message.setAttribute(RPConstants.MESSAGE_ATTR_REV_SENDER_USERID,
					senderUserId);
		}
		if (!TextUtils.isEmpty(receiverUserId)) {
			message.setAttribute(RPConstants.MESSAGE_ATTR_REV_RECEIVER_USERID,
					receiverUserId);
		}
		return message;
	}

	public static void sendMessage(EMMessage message,int chatType){
		if (chatType == EaseConstant.CHATTYPE_GROUP){
			message.setChatType(EMMessage.ChatType.GroupChat);
		}else if(chatType == EaseConstant.CHATTYPE_CHATROOM){
			message.setChatType(EMMessage.ChatType.ChatRoom);
		}
		//send message
		EMClient.getInstance().chatManager().sendMessage(message);
	}
}
