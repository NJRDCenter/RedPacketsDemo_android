package com.hyphenate.chatuidemo.redpacket;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.redpacket.adapter.MembersChooseAdapter;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.ChatFragment;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseSidebar;
import com.jd.redpackets.entity.send.ExclusiveUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RPMembersChooseActivity extends BaseActivity {
	public static final String INTENT_EXTRA_GROUP_ID = "groupId";

	ListView listView;

	private MembersChooseAdapter mAdapter;

	private TextView mConformTv;

	private String groupId;

	private EMGroup group;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_pick_at_user);

		groupId = getIntent().getStringExtra(INTENT_EXTRA_GROUP_ID);
		group = EMClient.getInstance().groupManager().getGroup(groupId);

		EaseSidebar sidebar = (EaseSidebar) findViewById(
				com.hyphenate.easeui.R.id.sidebar);
		mConformTv = (TextView) findViewById(R.id.conform_choose);
		mConformTv.setOnClickListener(mConformClickListener);
		listView = (ListView) findViewById(R.id.list);
		sidebar.setListView(listView);

		refreshUI();
		updateMembers();
	}

	/**
	 * 刷新ui
	 */
	private void refreshUI() {
		List<String> members = group.getMembers();
		List<EaseUser> userList = new ArrayList<EaseUser>();
		for (String username : members) {
			EaseUser user = EaseUserUtils.getUserInfo(username);
			userList.add(user);
		}

		Collections.sort(userList, new Comparator<EaseUser>() {

			@Override
			public int compare(EaseUser lhs, EaseUser rhs) {
				if (lhs.getInitialLetter().equals(rhs.getInitialLetter())) {
					return lhs.getNick().compareTo(rhs.getNick());
				} else {
					if ("#".equals(lhs.getInitialLetter())) {
						return 1;
					} else if ("#".equals(rhs.getInitialLetter())) {
						return -1;
					}
					return lhs.getInitialLetter()
							.compareTo(rhs.getInitialLetter());
				}

			}
		});

		mAdapter = new MembersChooseAdapter(this, 0, userList);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mAdapter.clickItem(position);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// private void addHeadView() {
	// View view = LayoutInflater.from(this).inflate(
	// R.layout.ease_row_contact, listView, false);
	// ImageView avatarView = (ImageView) view.findViewById(R.id.avatar);
	// TextView textView = (TextView) view.findViewById(R.id.name);
	// textView.setText(getString(R.string.all_members));
	// avatarView.setImageResource(R.drawable.ease_groups_icon);
	// listView.addHeaderView(view);
	// }

	private View.OnClickListener mConformClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			conformChooseMembers();
		}
	};

	private void conformChooseMembers() {
		List<EaseUser> easeUserList = mAdapter.getCheckedEaseUsers();
		ArrayList<ExclusiveUser> mExclusiveUsers = new ArrayList<>();
		for (EaseUser easeUser : easeUserList) {
			ExclusiveUser user = new ExclusiveUser();
			user.avatar = easeUser.getAvatar();
			user.userId = easeUser.getUsername();
			user.userName = easeUser.getNickname();
			mExclusiveUsers.add(user);
		}
		ChatFragment.mGroupIdCallback.chooseGroupMembers(mExclusiveUsers);
		finish();
	}

	public void back(View view) {
		finish();
	}

	/**
	 * 更新群成员
	 */
	public void updateMembers() {
		new Thread(new Runnable() {
			public void run() {
				try {
					EMClient.getInstance().groupManager()
							.getGroupFromServer(groupId);
					group = EMClient.getInstance().groupManager().getGroup(groupId);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							refreshUI();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
