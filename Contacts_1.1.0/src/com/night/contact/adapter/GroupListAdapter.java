package com.night.contact.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.bean.GroupBean;
import com.night.contact.ui.R;
/**
 * 群组管理界面显示的适配器
 * @author NightHary
 *
 */
@SuppressLint("InflateParams") public class GroupListAdapter extends BaseAdapter{
	private LayoutInflater inflater;
	private List<GroupBean> groups;
	private Context context;
	public GroupListAdapter(List<GroupBean> groups,Context context){
		this.context = context;
		this.groups = groups;
		this.inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Object getItem(int position) {
		return groups.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	//通知数据更新
	public void refresh(List<GroupBean> groupsList){
		this.groups = groupsList;
		notifyDataSetChanged();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.list_item_group_manage,null);
			holder = new ViewHolder();
			holder.groupPhoto = (ImageView) convertView.findViewById(R.id.group_photo);
			holder.groupName = (TextView) convertView.findViewById(R.id.group_name);
			holder.groupMember = (TextView) convertView.findViewById(R.id.group_member);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		GroupBean group = groups.get(position);
		String groupName = group.getName();
		int number = new ContactDAO(context).getContactsByGroupId(group.getId()).size();
//		int count = group.getCount();
		holder.groupPhoto.setImageResource(R.drawable.group_photo);
		holder.groupName.setText(groupName);
		holder.groupMember.setText("共有"+number+" 位联系人");
		return convertView;
	}
	private static class ViewHolder{
		ImageView groupPhoto;
		TextView groupName;
		TextView groupMember;
	}
}

