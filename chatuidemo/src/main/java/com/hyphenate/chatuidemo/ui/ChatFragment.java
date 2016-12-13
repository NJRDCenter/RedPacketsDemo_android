package com.hyphenate.chatuidemo.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.domain.EmojiconExampleGroupData;
import com.hyphenate.chatuidemo.domain.RobotUser;
import com.hyphenate.chatuidemo.redpacket.ChatRowRedPacket;
import com.hyphenate.chatuidemo.redpacket.ChatRowRedPacketAck;
import com.hyphenate.chatuidemo.redpacket.RPConstants;
import com.hyphenate.chatuidemo.redpacket.RPHelper;
import com.hyphenate.chatuidemo.redpacket.RPMembersChooseActivity;
import com.hyphenate.chatuidemo.redpacket.RedPacketsCreator;
import com.hyphenate.chatuidemo.redpacket.RedpkgParams;
import com.hyphenate.chatuidemo.redpacket.http.RequestUtils;
import com.hyphenate.chatuidemo.widget.ChatRowVoiceCall;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.ui.EaseChatFragment.EaseChatFragmentHelper;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconMenu;
import com.hyphenate.util.EasyUtils;
import com.hyphenate.util.PathUtil;
import com.jd.redpackets.manager.RPSendGroupInfo;
import com.jd.redpackets.manager.RPSendInfo;
import com.jd.redpackets.manager.RedPacketManager;
import com.jd.redpackets.manager.RedPacketType;
import com.jd.redpackets.manager.callback.MembersCallback;
import com.jd.redpackets.manager.callback.MembersChooseCallback;
import com.jd.redpackets.manager.callback.RPResultHandler;
import com.jd.redpackets.manager.params.RPSendGroupParams;
import com.jd.redpackets.manager.params.RPSendPersonalParams;
import com.jd.redpackets.manager.result.RPSendResult;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ChatFragment extends EaseChatFragment implements
		EaseChatFragmentHelper {

	public static MembersCallback mGroupIdCallback;

	// constant start from 11 to avoid conflict with constant in base class
	private static final int ITEM_VIDEO = 11;
	private static final int ITEM_FILE = 12;
	private static final int ITEM_VOICE_CALL = 13;
	private static final int ITEM_VIDEO_CALL = 14;

	private static final int REQUEST_CODE_SELECT_VIDEO = 11;
	private static final int REQUEST_CODE_SELECT_FILE = 12;
	private static final int REQUEST_CODE_GROUP_DETAIL = 13;
	private static final int REQUEST_CODE_CONTEXT_MENU = 14;
	private static final int REQUEST_CODE_SELECT_AT_USER = 15;

	private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 1;
	private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 2;
	private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 3;
	private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 4;

	// red packet code : 红包功能使用的常量
	private static final int MESSAGE_TYPE_RECV_RED_PACKET = 5;
	private static final int MESSAGE_TYPE_SEND_RED_PACKET = 6;
	private static final int MESSAGE_TYPE_SEND_RED_PACKET_ACK = 7;
	private static final int MESSAGE_TYPE_RECV_RED_PACKET_ACK = 8;
	private static final int MESSAGE_TYPE_RECV_TRANSFER_PACKET = 9;
	private static final int MESSAGE_TYPE_SEND_TRANSFER_PACKET = 10;
	private static final int REQUEST_CODE_SEND_RED_PACKET = 16;
	private static final int ITEM_RED_PACKET = 16;
	private static final int REQUEST_CODE_SEND_TRANSFER_PACKET = 17;
	private static final int ITEM_TRANSFER_PACKET = 17;
	// end of red packet code

	/**
	 * if it is chatBot
	 */
	private boolean isRobot;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void setUpView() {
		setChatFragmentListener(this);
		if (chatType == Constant.CHATTYPE_SINGLE) {
			Map<String, RobotUser> robotMap = DemoHelper.getInstance()
					.getRobotList();
			if (robotMap != null && robotMap.containsKey(toChatUsername)) {
				isRobot = true;
			}
		} else {
			try {
				EMClient.getInstance().groupManager()
						.getGroupFromServer(toChatUsername);
			} catch (Exception e) {
			}
		}
		super.setUpView();
		// set click listener
		titleBar.setLeftLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (EasyUtils.isSingleActivity(getActivity())) {
					Intent intent = new Intent(getActivity(),
							MainActivity.class);
					startActivity(intent);
				}
				onBackPressed();
			}
		});
		((EaseEmojiconMenu) inputMenu.getEmojiconMenu())
				.addEmojiconGroup(EmojiconExampleGroupData.getData());
		if (chatType == EaseConstant.CHATTYPE_GROUP) {
			inputMenu.getPrimaryMenu().getEditText()
					.addTextChangedListener(new TextWatcher() {

						@Override
						public void onTextChanged(CharSequence s, int start,
								int before, int count) {
							if (count == 1
									&& "@".equals(String.valueOf(s
											.charAt(start)))) {
								startActivityForResult(
										new Intent(getActivity(),
												PickAtUserActivity.class)
												.putExtra("groupId",
														toChatUsername),
										REQUEST_CODE_SELECT_AT_USER);
							}
						}

						@Override
						public void beforeTextChanged(CharSequence s,
								int start, int count, int after) {

						}

						@Override
						public void afterTextChanged(Editable s) {

						}
					});
		}
		final RPSendGroupInfo info = (RPSendGroupInfo) getArguments()
				.getSerializable("rp_group");
		if (null != info) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (chatType == EaseConstant.CHATTYPE_GROUP) {
						sendRP(info, RedPacketType.TYPE_GROUP);
					} else if (chatType == EaseConstant.CHATTYPE_SINGLE) {
						sendRP(info, RedPacketType.TYPE_PERSONAL);
					}
					// RedpkgParams params = new RedpkgParams();
					// params.desc = info.content;
					// params.redpkgId = info.redpkgId;
					// EaseUser easeUser = RPHelper.getEaseUser(RPHelper
					// .getCurrentUser());
					// params.platformUserName = info.senderUserId;
					// params.platformHeadImg =
					// "http://h.hiphotos.baidu.com/image/h%3D200/sign=cd65e7fa13d5ad6eb5f963eab1cb39a3/377adab44aed2e7394aa5a128f01a18b87d6fa49.jpg";
					// params.senderUserId = info.senderUserId;
					//
					// EMMessage message = RedPacketsCreator.createRPMessage(
					// params, toChatUsername);
					// sendMessage(message);
				}
			}, 500);

		}
	}

	@Override
	protected void registerExtendMenuItem() {
		// use the menu in base class
		super.registerExtendMenuItem();
		// extend menu items
		inputMenu.registerExtendMenuItem(R.string.attach_video,
				R.drawable.em_chat_video_selector, ITEM_VIDEO,
				extendMenuItemClickListener);
		inputMenu.registerExtendMenuItem(R.string.attach_file,
				R.drawable.em_chat_file_selector, ITEM_FILE,
				extendMenuItemClickListener);
		if (chatType == Constant.CHATTYPE_SINGLE) {
			inputMenu.registerExtendMenuItem(R.string.attach_voice_call,
					R.drawable.em_chat_voice_call_selector, ITEM_VOICE_CALL,
					extendMenuItemClickListener);
			inputMenu.registerExtendMenuItem(R.string.attach_video_call,
					R.drawable.em_chat_video_call_selector, ITEM_VIDEO_CALL,
					extendMenuItemClickListener);
		}
		// 聊天室暂时不支持红包功能
		// red packet code : 注册红包菜单选项
		if (chatType != Constant.CHATTYPE_CHATROOM) {
			inputMenu.registerExtendMenuItem(R.string.attach_red_packet,
					R.drawable.em_chat_red_packet_selector, ITEM_RED_PACKET,
					extendMenuItemClickListener);
		}
		// red packet code : 注册转账菜单选项
		if (chatType == Constant.CHATTYPE_SINGLE) {
			inputMenu.registerExtendMenuItem(R.string.attach_transfer_money,
					R.drawable.em_chat_transfer_selector, ITEM_TRANSFER_PACKET,
					extendMenuItemClickListener);
		}
		// end of red packet code
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			switch (resultCode) {
			case ContextMenuActivity.RESULT_CODE_COPY: // copy
				clipboard.setPrimaryClip(ClipData.newPlainText(null,
						((EMTextMessageBody) contextMenuMessage.getBody())
								.getMessage()));
				break;
			case ContextMenuActivity.RESULT_CODE_DELETE: // delete
				conversation.removeMessage(contextMenuMessage.getMsgId());
				messageList.refresh();
				break;

			case ContextMenuActivity.RESULT_CODE_FORWARD: // forward
				Intent intent = new Intent(getActivity(),
						ForwardMessageActivity.class);
				intent.putExtra("forward_msg_id", contextMenuMessage.getMsgId());
				startActivity(intent);

				break;

			default:
				break;
			}
		}
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_SELECT_VIDEO: // send the video
				if (data != null) {
					int duration = data.getIntExtra("dur", 0);
					String videoPath = data.getStringExtra("path");
					File file = new File(PathUtil.getInstance().getImagePath(),
							"thvideo" + System.currentTimeMillis());
					try {
						FileOutputStream fos = new FileOutputStream(file);
						Bitmap ThumbBitmap = ThumbnailUtils
								.createVideoThumbnail(videoPath, 3);
						ThumbBitmap.compress(CompressFormat.JPEG, 100, fos);
						fos.close();
						sendVideoMessage(videoPath, file.getAbsolutePath(),
								duration);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case REQUEST_CODE_SELECT_FILE: // send the file
				if (data != null) {
					Uri uri = data.getData();
					if (uri != null) {
						sendFileByUri(uri);
					}
				}
				break;
			case REQUEST_CODE_SELECT_AT_USER:
				if (data != null) {
					String username = data.getStringExtra("username");
					inputAtUsername(username, false);
				}
				break;
			// red packet code : 发送红包消息到聊天界面
			case REQUEST_CODE_SEND_RED_PACKET:
				// TODO czhang 发送红包回调
				Log.i("czhang", "send redpacket");
				if (data != null) {
					// sendMessage(RedPacketUtil.createRPMessage(getActivity(),
					// data, toChatUsername));
				}
				break;
			case REQUEST_CODE_SEND_TRANSFER_PACKET:// 发送转账消息
				if (data != null) {
					// sendMessage(RedPacketUtil.createTRMessage(getActivity(),
					// data, toChatUsername));
				}
				break;
			// end of red packet code
			default:
				break;
			}
		}

	}

	@Override
	public void onSetMessageAttributes(EMMessage message) {
		if (isRobot) {
			// set message extension
			message.setAttribute("em_robot_message", isRobot);
		}
	}

	@Override
	public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
		return new CustomChatRowProvider();
	}

	@Override
	public void onEnterToChatDetails() {
		if (chatType == Constant.CHATTYPE_GROUP) {
			EMGroup group = EMClient.getInstance().groupManager()
					.getGroup(toChatUsername);
			if (group == null) {
				Toast.makeText(getActivity(), R.string.gorup_not_found,
						Toast.LENGTH_SHORT).show();
				return;
			}
			startActivityForResult((new Intent(getActivity(),
					GroupDetailsActivity.class).putExtra("groupId",
					toChatUsername)), REQUEST_CODE_GROUP_DETAIL);
		} else if (chatType == Constant.CHATTYPE_CHATROOM) {
			startActivityForResult(new Intent(getActivity(),
					ChatRoomDetailsActivity.class).putExtra("roomId",
					toChatUsername), REQUEST_CODE_GROUP_DETAIL);
		}
	}

	@Override
	public void onAvatarClick(String username) {
		// handling when user click avatar
		Intent intent = new Intent(getActivity(), UserProfileActivity.class);
		intent.putExtra("username", username);
		startActivity(intent);

		// TODO czhang 群成员入口
		// Intent intent = new Intent(getActivity(),
		// RPMembersChooseActivity.class);
		// intent.putExtra(RPMembersChooseActivity.INTENT_EXTRA_GROUP_ID,
		// toChatUsername);
		// startActivity(intent);

		// TODO czhang 群选择入口
		// Intent intent = new Intent(getActivity(),
		// RPGroupsChooseActivity.class);
		// startActivity(intent);

	}

	@Override
	public void onAvatarLongClick(String username) {
		inputAtUsername(username);
	}

	@Override
	public boolean onMessageBubbleClick(EMMessage message) {
		// 消息框点击事件，demo这里不做覆盖，如需覆盖，return true
		// red packet code : 拆红包页面
		return false;
	}

	@Override
	public void onCmdMessageReceived(List<EMMessage> messages) {
		// red packet code : 处理红包回执透传消息
		for (EMMessage message : messages) {
			EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
			String action = cmdMsgBody.action();// 获取自定义action
			// if (action.equals(RPConstant.REFRESH_GROUP_RED_PACKET_ACTION)) {
			// RedPacketUtil.receiveRedPacketAckMessage(message);
			messageList.refresh();
			// }
		}
		// end of red packet code
		super.onCmdMessageReceived(messages);
	}

	@Override
	public void onMessageBubbleLongClick(EMMessage message) {
		// no message forward when in chat room
		startActivityForResult(
				(new Intent(getActivity(), ContextMenuActivity.class))
						.putExtra("message", message).putExtra("ischatroom",
								chatType == EaseConstant.CHATTYPE_CHATROOM),
				REQUEST_CODE_CONTEXT_MENU);
	}

	@Override
	public boolean onExtendMenuItemClick(int itemId, View view) {
		switch (itemId) {
		case ITEM_VIDEO:
			Intent intent = new Intent(getActivity(), ImageGridActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
			break;
		case ITEM_FILE: // file
			selectFileFromLocal();
			break;
		case ITEM_VOICE_CALL:
			startVoiceCall();
			break;
		case ITEM_VIDEO_CALL:
			startVideoCall();
			break;
		// red packet code : 进入发红包页面
		case ITEM_RED_PACKET:
			// TODO czhang 发红包入口
			Log.i("czhang", "send redPackets");
			if (chatType == Constant.CHATTYPE_SINGLE) {
				RPSendPersonalParams personalParams = new RPSendPersonalParams();
				personalParams.targetUserId = toChatUsername;
				personalParams.platformUserName = RPHelper.getCurrentUser();
				EaseUser easeUser = RPHelper.getEaseUser(RPHelper
						.getCurrentUser());
				personalParams.platformHeadImg = easeUser.getAvatar();
				// 测试加入风控信息
				personalParams.riskInfo = RequestUtils.genTestRiskInfo();
				RedPacketManager.sendRedPacket(getActivity(),
						personalParams, new RPResultHandler<RPSendResult>() {
							@Override
							public void onSuccess(RPSendResult result) {
								sendRP(result);
							}
						},null);
			} else if (chatType == Constant.CHATTYPE_GROUP) {
				RPSendGroupParams groupParams = new RPSendGroupParams();
				groupParams.groupId = toChatUsername;
				EMGroup group = RPHelper.getGroup(toChatUsername);
				groupParams.groupNum = String.valueOf(group.getMemberCount());
				groupParams.platformUserName = RPHelper.getCurrentUser();
				EaseUser easeUser = RPHelper.getEaseUser(RPHelper
						.getCurrentUser());
				groupParams.platformHeadImg = easeUser.getAvatar();
				// 测试加入风控信息
				groupParams.riskInfo = RequestUtils.genTestRiskInfo();
				RedPacketManager.sendRedPacket(getActivity(),
						groupParams, new RPResultHandler<RPSendResult>() {
							@Override
							public void onSuccess(RPSendResult result) {
								sendRP(result);
							}
						}, new MembersChooseCallback() {

							@Override
							public void startChooseMembers(Activity activity,
									MembersCallback callback) {
								mGroupIdCallback = callback;
								Intent intent = new Intent(getActivity(),
										RPMembersChooseActivity.class);
								intent.putExtra(
										RPMembersChooseActivity.INTENT_EXTRA_GROUP_ID,
										toChatUsername);
								startActivity(intent);
							}
						});
			}
			// RedPacketUtil.startRedPacketActivityForResult(this, chatType,
			// toChatUsername, REQUEST_CODE_SEND_RED_PACKET);
			break;
		case ITEM_TRANSFER_PACKET:// 进入转账页面
			break;
		// end of red packet code
		default:
			break;
		}
		// keep exist extend menu
		return false;
	}

	private void sendRP(RPSendResult result) {
		RedpkgParams params = new RedpkgParams();
		if (!TextUtils.isEmpty(result.redpkgId)) {
			params.redpkgId = Long.valueOf(result.redpkgId);
		}
		EaseUser currentUser = RPHelper.getEaseUser(RPHelper.getCurrentUser());
		if (currentUser != null){
			params.senderUserId = currentUser.getUsername();
		}
//		params.senderUserId = result.senderUserId;
		EaseUser easeUser = RPHelper.getEaseUser(RPHelper.getCurrentUser());
		params.platformUserName = null != easeUser ? easeUser.getNickname()
				: RPHelper.getCurrentUser();
		params.platformHeadImg = null != easeUser ? easeUser.getAvatar() : "";
		params.type = result.redpkgExtType;
		params.desc = result.content;
		EMMessage emMessage = RedPacketsCreator.createRPMessage(params,
				toChatUsername);
		sendMessage(emMessage);
	}

	private void sendRP(RPSendInfo result, String type) {
		RedpkgParams params = new RedpkgParams();
		params.redpkgId = result.redpkgId;
		params.senderUserId = result.senderUserId;
		EaseUser easeUser = RPHelper.getEaseUser(RPHelper.getCurrentUser());
		params.platformUserName = null != easeUser ? easeUser.getNickname()
				: RPHelper.getCurrentUser();
		params.platformHeadImg = null != easeUser ? easeUser.getAvatar() : "";
		params.type = type;
		params.desc = result.content;
		EMMessage emMessage = RedPacketsCreator.createRPMessage(params,
				toChatUsername);
		sendMessage(emMessage);
	}

	private void sendRP(RPSendGroupInfo info, String type) {
		RedpkgParams params = new RedpkgParams();
		params.redpkgId = info.redpkgId;
		params.senderUserId = info.senderUserId;
		EaseUser easeUser = RPHelper.getEaseUser(RPHelper.getCurrentUser());
		params.platformUserName = null != easeUser ? easeUser.getNickname()
				: RPHelper.getCurrentUser();
		params.platformHeadImg = null != easeUser ? easeUser.getAvatar() : "";
		params.type = type;
		params.desc = info.content;
		EMMessage emMessage = RedPacketsCreator.createRPMessage(params,
				toChatUsername);
		sendMessage(emMessage);
	}

	/**
	 * select file
	 */
	protected void selectFileFromLocal() {
		Intent intent = null;
		if (Build.VERSION.SDK_INT < 19) { // api 19 and later, we can't use this
											// way, demo just select from images
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);

		} else {
			intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
	}

	/**
	 * make a voice call
	 */
	protected void startVoiceCall() {
		if (!EMClient.getInstance().isConnected()) {
			Toast.makeText(getActivity(), R.string.not_connect_to_server,
					Toast.LENGTH_SHORT).show();
		} else {
			startActivity(new Intent(getActivity(), VoiceCallActivity.class)
					.putExtra("username", toChatUsername).putExtra(
							"isComingCall", false));
			// voiceCallBtn.setEnabled(false);
			inputMenu.hideExtendMenuContainer();
		}
	}

	/**
	 * make a video call
	 */
	protected void startVideoCall() {
		if (!EMClient.getInstance().isConnected())
			Toast.makeText(getActivity(), R.string.not_connect_to_server,
					Toast.LENGTH_SHORT).show();
		else {
			startActivity(new Intent(getActivity(), VideoCallActivity.class)
					.putExtra("username", toChatUsername).putExtra(
							"isComingCall", false));
			// videoCallBtn.setEnabled(false);
			inputMenu.hideExtendMenuContainer();
		}
	}

	/**
	 * chat row provider
	 *
	 */
	private final class CustomChatRowProvider implements
			EaseCustomChatRowProvider {
		@Override
		public int getCustomChatRowTypeCount() {
			// here the number is the message type in EMMessage::Type
			// which is used to count the number of different chat row
			return 10;
		}

		@Override
		public int getCustomChatRowType(EMMessage message) {
			if (message.getType() == EMMessage.Type.TXT) {
				// voice call
				if (message.getBooleanAttribute(
						Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
					return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL
							: MESSAGE_TYPE_SENT_VOICE_CALL;
				} else if (message.getBooleanAttribute(
						Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
					// video call
					return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO_CALL
							: MESSAGE_TYPE_SENT_VIDEO_CALL;
				}
				// red packet code : 红包消息、红包回执消息以及转账消息的chatrow type
				else if (message.getBooleanAttribute(
						RPConstants.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE, false)) {
					// 发送红包消息
					return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_RED_PACKET
							: MESSAGE_TYPE_SEND_RED_PACKET;
				} else if (message.getBooleanAttribute(
						RPConstants.MESSAGE_ATTR_REV_IS_RED_PACKET_MESSAGE,
						false)) {
					// 收红包消息
					return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_SEND_RED_PACKET_ACK
							: MESSAGE_TYPE_RECV_RED_PACKET_ACK;
				}
				// end of red packet code
			}
			return 0;
		}

		@Override
		public EaseChatRow getCustomChatRow(EMMessage message, int position,
				BaseAdapter adapter) {
			if (message.getType() == EMMessage.Type.TXT) {
				// voice call or video call
				if (message.getBooleanAttribute(
						Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)
						|| message.getBooleanAttribute(
								Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
					return new ChatRowVoiceCall(getActivity(), message,
							position, adapter);
				}
				// red packet code : 红包消息、红包回执消息以及转账消息的chat row
				else if (message.getBooleanAttribute(
						RPConstants.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE, false)) {// 红包消息
					return new com.hyphenate.chatuidemo.redpacket.ChatRowRedPacket(
							getActivity(), message, position, adapter,
							new ChatRowRedPacket.SendCallback() {
								@Override
								public void sendReceiveMsg(String senderUserId,
										String currentUserId) {
									// 发送接收红包的消息
									EMMessage emMessage = RedPacketsCreator
											.createRevRPMessage(senderUserId,
													currentUserId,
													toChatUsername);
									sendMessage(emMessage);
								}

							});
					// return new ChatRowRedPacket(getActivity(), message,
					// position, adapter);
				} else if (message.getBooleanAttribute(
						RPConstants.MESSAGE_ATTR_REV_IS_RED_PACKET_MESSAGE,
						false)) {// 收红包消息
					return new ChatRowRedPacketAck(getActivity(), message,
							position, adapter);
				}
				// end of red packet code
			}
			return null;
		}

	}

}
