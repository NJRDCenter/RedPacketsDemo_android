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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.redpacket.adapter.GroupsChooseAdapter;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.exceptions.HyphenateException;
import com.jd.redpackets.manager.RedPacketManager;
import com.jd.redpackets.manager.result.RPSendResult;

import java.util.List;

public class RPGroupsChooseActivity extends BaseActivity {
	public static final String TAG = "GroupsActivity";
	private ListView groupListView;
	protected List<EMGroup> grouplist;
	private GroupsChooseAdapter groupAdapter;
	private InputMethodManager inputMethodManager;
	public static RPGroupsChooseActivity instance;
	private View progressBar;
	private SwipeRefreshLayout swipeRefreshLayout;
	private RPSendResult result;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			swipeRefreshLayout.setRefreshing(false);
			switch (msg.what) {
			case 0:
				refresh();
				break;
			case 1:
				Toast.makeText(RPGroupsChooseActivity.this,
						R.string.Failed_to_get_group_chat_information,
						Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_groups_choose);
		result = (RPSendResult) getIntent()
				.getSerializableExtra("group_choose");
		instance = this;
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		grouplist = EMClient.getInstance().groupManager().getAllGroups();
		groupListView = (ListView) findViewById(R.id.list);
		// show group list
		groupAdapter = new GroupsChooseAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
		swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright,
				R.color.holo_green_light, R.color.holo_orange_light,
				R.color.holo_red_light);
		// pull down to refresh
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Thread() {
					@Override
					public void run() {
						try {
							EMClient.getInstance().groupManager()
									.getJoinedGroupsFromServer();
							handler.sendEmptyMessage(0);
						} catch (HyphenateException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(1);
						}
					}
				}.start();
			}
		});

		groupListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object o = parent.getItemAtPosition(position - 1);
				if (o instanceof EMGroup) {
					EMGroup user = (EMGroup) o;
					String receiver = user.getGroupId();
					RedpkgParams params = new RedpkgParams();
					params.type = result.redpkgExtType;
					params.desc = result.content;
					if (!TextUtils.isEmpty(result.redpkgId)) {
						params.redpkgId = Long.valueOf(result.redpkgId);
					}
					EaseUser sendUser = RPHelper.getEaseUser(RPHelper
							.getCurrentUser());
					if (!TextUtils.isEmpty(RPHelper.getCurrentUser())
							&& null != sendUser) {
						params.platformHeadImg = sendUser.getAvatar();
						params.platformUserName = sendUser.getNickname();
						params.senderUserId = sendUser.getUsername();
					}
					EMMessage message = RedPacketsCreator.createRPMessage(
							params, receiver);
					RedPacketsCreator.sendMessage(message,
							EaseConstant.CHATTYPE_GROUP);
					if (!TextUtils.isEmpty(result.redpkgId)) {
						RedPacketManager.confirmSended(Long
								.valueOf(result.redpkgId));
					}
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					finish();
				}
			}

		});
		groupListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(
								getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		refresh();
		super.onResume();
	}

	private void refresh() {
		grouplist = EMClient.getInstance().groupManager().getAllGroups();
		groupAdapter = new GroupsChooseAdapter(this, 1, grouplist);
		groupListView.setAdapter(groupAdapter);
		groupAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	public void back(View view) {
		finish();
	}
}
