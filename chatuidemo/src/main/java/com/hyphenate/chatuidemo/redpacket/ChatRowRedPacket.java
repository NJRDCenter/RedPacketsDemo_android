package com.hyphenate.chatuidemo.redpacket;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.redpacket.http.RequestUtils;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.jd.redpackets.manager.RedPacketManager;
import com.jd.redpackets.manager.RedPacketType;
import com.jd.redpackets.manager.callback.RPResultHandler;
import com.jd.redpackets.manager.params.RPGrabParams;
import com.jd.redpackets.manager.result.RPGrabResult;

public class ChatRowRedPacket extends EaseChatRow {

	private SendCallback mSendCallback;

	private TextView mTvGreeting;
	private TextView mTvSponsorName;
	private TextView mTvPacketType;

	public ChatRowRedPacket(Context context, EMMessage message, int position,
			BaseAdapter adapter, SendCallback sendCallback) {
		super(context, message, position, adapter);
		mSendCallback = sendCallback;
	}

	@Override
	protected void onInflateView() {
		if (message.getBooleanAttribute(
				RPConstants.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE, false)) {
			inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE
					? R.layout.em_row_received_red_packet
					: R.layout.em_row_sent_red_packet, this);
		}
	}

	@Override
	protected void onFindViewById() {
		mTvGreeting = (TextView) findViewById(R.id.tv_money_greeting);
		mTvSponsorName = (TextView) findViewById(R.id.tv_sponsor_name);
		mTvPacketType = (TextView) findViewById(R.id.tv_packet_type);
	}

	@Override
	protected void onSetUpView() {
		String greetings = message
				.getStringAttribute(RPConstants.MESSAGE_ATTR_REDPKG_DESC, "");
		mTvGreeting.setText(greetings);

		String packetType = message
				.getStringAttribute(RPConstants.MESSAGE_ATTR_REDPKG_TYPE, "");

		if (!TextUtils.isEmpty(packetType)
				&& RedPacketType.TYPE_EXCLUSIVE.equals(packetType)) {
			mTvPacketType.setVisibility(VISIBLE);
			mTvPacketType.setText("专属红包");
		} else {
			mTvPacketType.setVisibility(GONE);
		}
		handleTextMessage();
	}

	protected void handleTextMessage() {
		if (message.direct() == EMMessage.Direct.SEND) {
			setMessageSendCallback();
			switch (message.status()) {
			case CREATE:
				progressBar.setVisibility(View.GONE);
				statusView.setVisibility(View.VISIBLE);
				// 发送消息
				break;
			case SUCCESS: // 发送成功
				progressBar.setVisibility(View.GONE);
				statusView.setVisibility(View.GONE);
				break;
			case FAIL: // 发送失败
				progressBar.setVisibility(View.GONE);
				statusView.setVisibility(View.VISIBLE);
				break;
			case INPROGRESS: // 发送中
				progressBar.setVisibility(View.VISIBLE);
				statusView.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void onUpdateView() {
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onBubbleClick() {
		grabRedPacket();
	}

	/**
	 * 抢红包
	 */
	private void grabRedPacket() {
		RPGrabParams params = new RPGrabParams();
		// 获取当前用户
		String currentUserName = RPHelper.getCurrentUser();
		EaseUser currentUser = null;
		if (!TextUtils.isEmpty(currentUserName)) {
			currentUser = RPHelper.getEaseUser(currentUserName);
		}
		if (currentUser != null) {
			// 设置头像和昵称
			params.platformHeadImg = currentUser.getAvatar();
			params.platformUserName = currentUser.getUsername();
		}
		// 设置红包id
		params.redpkgId = message
				.getLongAttribute(RPConstants.MESSAGE_ATTR_REDPKG_ID, -1);
		// 设置发送者id
		params.senderUserId = message
				.getStringAttribute(RPConstants.MESSAGE_ATTR_SENDER_USERID, "");
		// 风控参数加入
		params.riskInfo = RequestUtils.genTestRiskInfo();
		String type = message
				.getStringAttribute(RPConstants.MESSAGE_ATTR_REDPKG_TYPE, "");
		if (RedPacketType.TYPE_PERSONAL.equals(type)) {// 个人红包
			params.grabType = RPGrabParams.GrabType.PERSONAL;
			RedPacketManager.grabRedPacket((Activity) context, params,
					new RPResultHandler<RPGrabResult>() {
						@Override
						public void onSuccess(RPGrabResult result) {
							sendReceiveMsg();
						}
					});
		} else if (RedPacketType.TYPE_GROUP.equals(type)) {// 群红包
			params.grabType = RPGrabParams.GrabType.GROUP;
			RedPacketManager.grabRedPacket((Activity) context, params,
					new RPResultHandler<RPGrabResult>() {
						@Override
						public void onSuccess(RPGrabResult result) {
							sendReceiveMsg();
						}
					});
		} else if (RedPacketType.TYPE_EXCLUSIVE.equals(type)) {// 专属红包
			params.grabType = RPGrabParams.GrabType.EXCLUSIVE;
			RedPacketManager.grabRedPacket((Activity) context, params,
					new RPResultHandler<RPGrabResult>() {
						@Override
						public void onSuccess(RPGrabResult result) {
							sendReceiveMsg();
						}
					});
		}
	}

	/**
	 * 发送接收的消息
	 */
	public void sendReceiveMsg() {
		String senderUserId = message
				.getStringAttribute(RPConstants.MESSAGE_ATTR_SENDER_USERID, "");
		String currentUserId = RPHelper.getCurrentUser();
		if (mSendCallback != null) {
			mSendCallback.sendReceiveMsg(senderUserId, currentUserId);
		}
	}

	/**
	 * 发送回调，接收消息后发送信息
	 */
	public interface SendCallback {
		void sendReceiveMsg(String senderUserId, String currentUserId);
	}

}
