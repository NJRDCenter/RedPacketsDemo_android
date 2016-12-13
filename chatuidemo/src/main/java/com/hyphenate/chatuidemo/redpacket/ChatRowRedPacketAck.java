package com.hyphenate.chatuidemo.redpacket;

import android.content.Context;
import android.text.TextUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

public class ChatRowRedPacketAck extends EaseChatRow {

	private TextView mTvMessage;

	public ChatRowRedPacketAck(Context context, EMMessage message, int position,
			BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

	@Override
	protected void onInflateView() {
		if (message.getBooleanAttribute(
				RPConstants.MESSAGE_ATTR_REV_IS_RED_PACKET_MESSAGE, false)) {
			inflater.inflate(message.direct() == EMMessage.Direct.RECEIVE
					? R.layout.em_row_red_packet_ack_message
					: R.layout.em_row_red_packet_ack_message, this);
		}
	}

	@Override
	protected void onFindViewById() {
		mTvMessage = (TextView) findViewById(R.id.ease_tv_money_msg);
	}

	@Override
	protected void onSetUpView() {
		String currentUser = EMClient.getInstance().getCurrentUser();
		String fromUser = message.getStringAttribute(
				RPConstants.MESSAGE_ATTR_REV_SENDER_USERID, "");// 红包发送者
		String toUser = message.getStringAttribute(
				RPConstants.MESSAGE_ATTR_REV_RECEIVER_USERID, "");// 红包接收者
		// 发送者用户昵称
		String fromUserNick = fromUser;
		// 接收者用户昵称
		String toUserNick = toUser;
		EaseUser fromEaseUser = RPHelper.getEaseUser(fromUser);
		EaseUser toEaseUser = RPHelper.getEaseUser(toUser);
		if (fromEaseUser != null
				&& !TextUtils.isEmpty(fromEaseUser.getNickname())) {
			fromUserNick = fromEaseUser.getNickname();
		}
		if (toEaseUser != null
				&& !TextUtils.isEmpty(toEaseUser.getNickname())) {
			toUserNick = toEaseUser.getNickname();
		}

		if (message.direct() == EMMessage.Direct.SEND) {
			if (message.getChatType().equals(EMMessage.ChatType.GroupChat)) {
				if (fromUser.equals(currentUser)) {
					mTvMessage.setText(R.string.msg_take_red_packet);
				} else {
					mTvMessage.setText(String.format(
							getResources().getString(
									R.string.msg_take_someone_red_packet),
							fromUserNick));
				}
			} else {
				mTvMessage.setText(String.format(
						getResources().getString(
								R.string.msg_take_someone_red_packet),
						fromUserNick));
			}
		} else {
			if (RPHelper.getCurrentUser().equals(fromUser)) {
				mTvMessage.setText(String.format(
						getResources().getString(
								R.string.msg_someone_take_your_red_packet),
						toUserNick));
			}
			mTvMessage
					.setText(String.format(
							getResources().getString(
									R.string.msg_someone_take_red_packet),
					toUserNick));

		}
	}

	@Override
	protected void onUpdateView() {

	}

	@Override
	protected void onBubbleClick() {
	}

}
