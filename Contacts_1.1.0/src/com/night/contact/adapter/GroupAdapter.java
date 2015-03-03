package com.night.contact.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.night.contact.bean.GroupBean;
import com.night.contact.ui.R;

/**
 * 标题栏下拉菜单选项适配器
 * @author NightHary
 *
 */
public class GroupAdapter extends BaseAdapter {

	private Context context ;
	private List<GroupBean> groupList;
	public GroupAdapter(Context context,List<GroupBean> groupList){
		this.context = context;
		this.groupList = groupList;
	}
	public int getCount() {
		return groupList.size();
	}

	public Object getItem(int position) {
		return groupList.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}
	@SuppressLint("ViewHolder") public View getView(int position, View convertView, ViewGroup parent) {
		GroupBean gb = groupList.get(position);
		View view = View.inflate(context,R.layout.list_item_popview,null);
		TextView button = (TextView) view.findViewById(R.id.btn_group_list);
		button.setText(gb.getName());
		return view;
	}

}
