package com.hyphenate.chatuidemo.redpacket;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.hyphenate.chatuidemo.R;

/**
 * 联系人列表
 * 
 * Created by czhang on 16/11/25.
 */

public class RPContactListActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		// 添加会话fragment
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fl_contact_list_container, new RPContactListFragment());
		ft.commit();
	}

}
