package com.hyphenate.chatuidemo.redpacket.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;

import java.util.List;

/**
 * 群选择列表
 *
 * @author czhang
 */
public class GroupsChooseAdapter extends ArrayAdapter<EMGroup> {

	private LayoutInflater inflater;

	public GroupsChooseAdapter(Context context, int res, List<EMGroup> groups) {
		super(context, res, groups);
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == 0) {
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.em_search_bar_with_padding, parent, false);
			}
			final EditText query = (EditText) convertView
					.findViewById(R.id.query);
			final ImageButton clearSearch = (ImageButton) convertView
					.findViewById(R.id.search_clear);
			query.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					getFilter().filter(s);
					if (s.length() > 0) {
						clearSearch.setVisibility(View.VISIBLE);
					} else {
						clearSearch.setVisibility(View.INVISIBLE);
					}
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void afterTextChanged(Editable s) {
				}
			});
			clearSearch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					query.getText().clear();
				}
			});
		} else {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.em_row_group, parent,
						false);
			}
			((TextView) convertView.findViewById(R.id.name)).setText(getItem(
					position - 1).getGroupName());

		}

		return convertView;
	}

	@Override
	public int getCount() {
		return super.getCount() + 1;
	}

}
