/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatuidemo.redpacket;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.DemoHelper.DataSyncListener;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.db.InviteMessgeDao;
import com.hyphenate.chatuidemo.db.UserDao;
import com.hyphenate.chatuidemo.ui.AddContactActivity;
import com.hyphenate.chatuidemo.ui.ContactListFragment;
import com.hyphenate.chatuidemo.ui.NewFriendsMsgActivity;
import com.hyphenate.chatuidemo.ui.PublicChatRoomsActivity;
import com.hyphenate.chatuidemo.ui.RobotsActivity;
import com.hyphenate.chatuidemo.widget.ContactItemView;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.NetUtils;
import com.jd.redpackets.manager.RedPacketManager;
import com.jd.redpackets.manager.result.RPSendResult;

import java.util.Hashtable;
import java.util.Map;

/**
 * contact list
 *
 */
public class RPContactListFragment extends EaseContactListFragment {

	private static final int QUEST_CODE_GROUP_CHOOSE = 1001;
	private static final String TAG = ContactListFragment.class.getSimpleName();
	private ContactSyncListener contactSyncListener;
	private BlackListSyncListener blackListSyncListener;
	private ContactInfoSyncListener contactInfoSyncListener;
	private View loadingView;
	private ContactItemView applicationItem;
	private InviteMessgeDao inviteMessgeDao;
	private RPSendResult result;

	@SuppressLint("InflateParams")
	@Override
	protected void initView() {
		super.initView();
		@SuppressLint("InflateParams")
		View headerView = LayoutInflater.from(getActivity()).inflate(
				R.layout.em_contacts_header, null);
		HeaderItemClickListener clickListener = new HeaderItemClickListener();
		applicationItem = (ContactItemView) headerView
				.findViewById(R.id.application_item);
		applicationItem.setOnClickListener(clickListener);
		headerView.findViewById(R.id.group_item).setOnClickListener(
				clickListener);
		headerView.findViewById(R.id.chat_room_item).setOnClickListener(
				clickListener);
		headerView.findViewById(R.id.robot_item).setOnClickListener(
				clickListener);
		listView.addHeaderView(headerView);
		// add loading view
		loadingView = LayoutInflater.from(getActivity()).inflate(
				R.layout.em_layout_loading_data, null);
		contentContainer.addView(loadingView);

		registerForContextMenu(listView);

		// 隐藏部分页面
		applicationItem.setVisibility(View.GONE);
		headerView.findViewById(R.id.chat_room_item).setVisibility(View.GONE);
		headerView.findViewById(R.id.robot_item).setVisibility(View.GONE);
	}

	@Override
	public void refresh() {
		Map<String, EaseUser> m = DemoHelper.getInstance().getContactList();
		if (m instanceof Hashtable<?, ?>) {
			// noinspection unchecked
			m = (Map<String, EaseUser>) ((Hashtable<String, EaseUser>) m)
					.clone();
		}
		setContactsMap(m);
		super.refresh();
		if (inviteMessgeDao == null) {
			inviteMessgeDao = new InviteMessgeDao(getActivity());
		}
		if (inviteMessgeDao.getUnreadMessagesCount() > 0) {
			applicationItem.showUnreadMsgView();
		} else {
			applicationItem.hideUnreadMsgView();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setUpView() {
		result = (RPSendResult) getActivity().getIntent().getSerializableExtra(
				"rp_result");
		titleBar.setRightImageResource(R.drawable.em_add);
		titleBar.setRightLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// startActivity(new Intent(getActivity(),
				// AddContactActivity.class));
				NetUtils.hasDataConnection(getActivity());
			}
		});
		// 设置联系人数据
		Map<String, EaseUser> m = DemoHelper.getInstance().getContactList();
		if (m instanceof Hashtable<?, ?>) {
			m = (Map<String, EaseUser>) ((Hashtable<String, EaseUser>) m)
					.clone();
		}
		setContactsMap(m);
		super.setUpView();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EaseUser receiverUser = (EaseUser) listView
						.getItemAtPosition(position);
				if (receiverUser != null) {
					String receiverUserName = receiverUser.getUsername();
					// demo中直接进入聊天页面，实际一般是进入用户详情页
					// startActivity(new Intent(getActivity(),
					// ChatActivity.class)
					// .putExtra("userId", username));
					RedpkgParams params = new RedpkgParams();
					// params.senderUserId = result.senderUserId;
					params.type = result.redpkgExtType;
					params.desc = result.content;
					if (!TextUtils.isEmpty(result.redpkgId)) {
						params.redpkgId = Long.valueOf(result.redpkgId);
					}
					String sendUserId = RPHelper.getCurrentUser();
					if (!TextUtils.isEmpty(sendUserId)) {
						params.senderUserId = sendUserId;
						EaseUser sendUser = RPHelper.getEaseUser(sendUserId);
						if (null != sendUser) {
							params.platformUserName = sendUser.getNickname();
							params.platformHeadImg = sendUser.getAvatar();
						}
					}
					EMMessage message = RedPacketsCreator.createRPMessage(
							params, receiverUserName);
					RedPacketsCreator.sendMessage(message,
							EaseConstant.CHATTYPE_SINGLE);
					if (!TextUtils.isEmpty(result.redpkgId)) {
						RedPacketManager.confirmSended(Long
								.valueOf(result.redpkgId));
					}
					getActivity().finish();
				}
			}
		});

		// 进入添加好友页
		titleBar.getRightLayout().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(),
						AddContactActivity.class));
			}
		});

		contactSyncListener = new ContactSyncListener();
		DemoHelper.getInstance().addSyncContactListener(contactSyncListener);

		blackListSyncListener = new BlackListSyncListener();
		DemoHelper.getInstance()
				.addSyncBlackListListener(blackListSyncListener);

		contactInfoSyncListener = new ContactInfoSyncListener();
		DemoHelper.getInstance().getUserProfileManager()
				.addSyncContactInfoListener(contactInfoSyncListener);

		if (DemoHelper.getInstance().isContactsSyncedWithServer()) {
			loadingView.setVisibility(View.GONE);
		} else if (DemoHelper.getInstance().isSyncingContactsWithServer()) {
			loadingView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (contactSyncListener != null) {
			DemoHelper.getInstance().removeSyncContactListener(
					contactSyncListener);
			contactSyncListener = null;
		}

		if (blackListSyncListener != null) {
			DemoHelper.getInstance().removeSyncBlackListListener(
					blackListSyncListener);
		}

		if (contactInfoSyncListener != null) {
			DemoHelper.getInstance().getUserProfileManager()
					.removeSyncContactInfoListener(contactInfoSyncListener);
		}
	}

	protected class HeaderItemClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.application_item:
				// 进入申请与通知页面
				startActivity(new Intent(getActivity(),
						NewFriendsMsgActivity.class));
				break;
			case R.id.group_item:
				// 进入群聊列表页面
				Intent intent = new Intent(getActivity(),
						RPGroupsChooseActivity.class);
				intent.putExtra("group_choose", result);
				startActivityForResult(intent, QUEST_CODE_GROUP_CHOOSE);
				break;
			case R.id.chat_room_item:
				// 进入聊天室列表页面
				startActivity(new Intent(getActivity(),
						PublicChatRoomsActivity.class));
				break;
			case R.id.robot_item:
				// 进入Robot列表页面
				startActivity(new Intent(getActivity(), RobotsActivity.class));
				break;

			default:
				break;
			}
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		toBeProcessUser = (EaseUser) listView
				.getItemAtPosition(((AdapterContextMenuInfo) menuInfo).position);
		toBeProcessUsername = toBeProcessUser.getUsername();
		getActivity().getMenuInflater().inflate(R.menu.em_context_contact_list,
				menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			try {
				// delete contact
				deleteContact(toBeProcessUser);
				// remove invitation message
				InviteMessgeDao dao = new InviteMessgeDao(getActivity());
				dao.deleteMessage(toBeProcessUser.getUsername());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (item.getItemId() == R.id.add_to_blacklist) {
			moveToBlacklist(toBeProcessUsername);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * delete contact
	 *
	 */
	public void deleteContact(final EaseUser tobeDeleteUser) {
		String st1 = getResources().getString(R.string.deleting);
		final String st2 = getResources().getString(R.string.Delete_failed);
		final ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMClient.getInstance().contactManager()
							.deleteContact(tobeDeleteUser.getUsername());
					// remove user from memory and database
					UserDao dao = new UserDao(getActivity());
					dao.deleteContact(tobeDeleteUser.getUsername());
					DemoHelper.getInstance().getContactList()
							.remove(tobeDeleteUser.getUsername());
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							contactList.remove(tobeDeleteUser);
							contactListLayout.refresh();

						}
					});
				} catch (final Exception e) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2 + e.getMessage(),
									Toast.LENGTH_LONG).show();
						}
					});

				}

			}
		}).start();

	}

	class ContactSyncListener implements DataSyncListener {
		@Override
		public void onSyncComplete(final boolean success) {
			EMLog.d(TAG, "on contact list sync success:" + success);
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (success) {
								loadingView.setVisibility(View.GONE);
								refresh();
							} else {
								String s1 = getResources().getString(
										R.string.get_failed_please_check);
								Toast.makeText(getActivity(), s1,
										Toast.LENGTH_LONG).show();
								loadingView.setVisibility(View.GONE);
							}
						}

					});
				}
			});
		}
	}

	class BlackListSyncListener implements DataSyncListener {

		@Override
		public void onSyncComplete(boolean success) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					refresh();
				}
			});
		}

	}

	class ContactInfoSyncListener implements DataSyncListener {

		@Override
		public void onSyncComplete(final boolean success) {
			EMLog.d(TAG, "on contactinfo list sync success:" + success);
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					loadingView.setVisibility(View.GONE);
					if (success) {
						refresh();
					}
				}
			});
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != getActivity().RESULT_OK) {
			return;
		}
		if (requestCode == QUEST_CODE_GROUP_CHOOSE) {
			getActivity().finish();
		}
	}
}
