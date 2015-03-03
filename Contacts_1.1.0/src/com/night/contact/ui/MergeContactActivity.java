package com.night.contact.ui;

import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.bean.SortEntry;
import com.night.contact.util.Tools;

/**
 * 显示重复联系人界面
 * 
 * @author NightHary
 * 
 */
@SuppressLint("InflateParams")
public class MergeContactActivity extends Activity {
	Bundle bundle;
	private List<SortEntry> nameEntrys;
	private List<SortEntry> numEntrys;
	private ListView merge_contact_lv;
	private TextView no_merge_contact_tv;

	private ContactDAO contactDAO;
	ProgressDialog dialogLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_merge);

		merge_contact_lv = (ListView) this.findViewById(R.id.merge_contact_lv);
		no_merge_contact_tv = (TextView) this
				.findViewById(R.id.no_merge_contact_tv);

		bundle = getIntent().getExtras();
		nameEntrys = bundle.getParcelableArrayList("nameList");
		numEntrys = bundle.getParcelableArrayList("numList");
		List<List<SortEntry>> numLists = null;
		List<List<SortEntry>> nameLists = null;
		if(!nameEntrys.isEmpty()){
			nameLists = Tools.getMergeListName(nameEntrys);
		}
		if(!numEntrys.isEmpty()){
			numLists = Tools.getMergeListNum(numEntrys);
		}

		contactDAO = new ContactDAO(this);
		// List<SortEntry> entryList = numLists.get(0);
		// for (SortEntry entry : entryList) {
		// System.out.println(entry.mName + "  " + entry.mNum);
		// }
//		if(!numLists.isEmpty()){
//			numLists.addAll(nameLists);
//			merge_contact_lv.setAdapter(new MergeContactAdapter(numLists, this));
//		}else if(!nameLists.isEmpty()){
//			merge_contact_lv.setAdapter(new MergeContactAdapter(numLists, this));
//		}

		if (numLists == null && nameLists == null) {
			no_merge_contact_tv.setVisibility(View.VISIBLE);
		}else if(numLists == null && nameLists != null){
//			nameLists.addAll(numLists);
			merge_contact_lv.setAdapter(new MergeContactAdapter(nameLists, this));
		}else if(nameLists == null && numLists != null){
//			numLists.addAll(nameLists);
			merge_contact_lv.setAdapter(new MergeContactAdapter(numLists, this));
		}else{
			numLists.addAll(nameLists);
			merge_contact_lv.setAdapter(new MergeContactAdapter(numLists, this));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@SuppressLint("ViewHolder")
	private class MergeContactAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<List<SortEntry>> mergeLists;

		public MergeContactAdapter(List<List<SortEntry>> mergeLists,
				Context context) {
			this.mergeLists = mergeLists;
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		private Context context;

		@Override
		public int getCount() {
			return mergeLists.size();
		}

		@Override
		public Object getItem(int position) {
			return mergeLists.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final List<SortEntry> entryList = mergeLists.get(position);
			convertView = inflater.inflate(R.layout.list_item_merge_contact,
					null);

			MyListView merge_contact_lv_list = (MyListView) convertView
					.findViewById(R.id.merge_contact_lv_list);
			Button merge_btn_self = (Button) convertView
					.findViewById(R.id.merge_btn_self);

			merge_contact_lv_list.setAdapter(new MergeListAdapter(entryList,
					context));

			setListViewHeight(merge_contact_lv_list);

			merge_btn_self.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (entryList.get(0).mName.equals(entryList.get(1).mName)) {// 姓名重复
						final SortEntry contact = entryList.get(0);
						String number = "";
						for (SortEntry entry : entryList) {
							number += entry.mNum + "#";
						}
						contact.mNum = number;
						new AlertDialog.Builder(MergeContactActivity.this)
								.setTitle("合并联系人")
								.setMessage("合并联系人" + contact.mName + "?")
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												new DeleteContactTask(entryList).execute();
												contactDAO.addContact1(contact,0);
											}
										})
								.setNegativeButton("取消",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										}).show();
					} else {// 电话重复
						
						String strName = "";
						for (int i = 0; i < entryList.size(); i++) {
							strName += entryList.get(i).mName+"#";
						}
						final String[] names = strName.split("#");
						new AlertDialog.Builder(MergeContactActivity.this)
								.setTitle("请选择合并联系人保存的姓名")
								.setItems(names,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												entryList.remove(which);
												new DeleteContactTask(entryList).execute();
											}
										}).show();
					}
				}
			});
			return convertView;
		}
	}

	@SuppressLint("InflateParams")
	private class MergeListAdapter extends BaseAdapter {

		private List<SortEntry> entryList;
		private Context context;
		private LayoutInflater inflater;

		public MergeListAdapter(List<SortEntry> entryList, Context context) {
			this.entryList = entryList;
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return entryList.size();
		}

		@Override
		public Object getItem(int position) {
			return entryList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.list_item_merge_contact_list, null);
				holder = new ViewHolder();
				holder.photo = (ImageView) convertView
						.findViewById(R.id.contact_photo);
				holder.contactName = (TextView) convertView
						.findViewById(R.id.merge_contacts_name);
				holder.contatcNum = (TextView) convertView
						.findViewById(R.id.merge_contacts_num);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			System.out.println(entryList.size());
			SortEntry entry = new SortEntry();
			entry = entryList.get(position);
			holder.contactName.setText(entry.mName);
			holder.contatcNum.setText(entry.mNum);
			if (0 == entry.photoId) {
				holder.photo.setImageResource(R.drawable.default_contact_photo);
			} else {
				Uri uri = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI,
						Integer.parseInt(entry.mID));
				InputStream input = ContactsContract.Contacts
						.openContactPhotoInputStream(
								context.getContentResolver(), uri);
				Bitmap contactPhoto = BitmapFactory.decodeStream(input);
				holder.photo.setImageBitmap(contactPhoto);
			}
			return convertView;
		}
	}

	private class ViewHolder {
		ImageView photo;
		TextView contactName;
		TextView contatcNum;
	}

	public void setListViewHeight(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();

		if (listAdapter == null) {

			return;

		}

		int totalHeight = 0;

		for (int i = 0; i < listAdapter.getCount(); i++) {

			View listItem = listAdapter.getView(i, null, listView);

			listItem.measure(0, 0);

			totalHeight += listItem.getMeasuredHeight();

		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();

		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));

		listView.setLayoutParams(params);

	}

	// 删除联系人
	private class DeleteContactTask extends AsyncTask<Void, Integer, Void> {
		private List<SortEntry> entryList;
		
		public DeleteContactTask(List<SortEntry> entryList) {
			this.entryList = entryList;
		}

		@Override
		protected Void doInBackground(Void... params) {
			// Utils.deleteContact(chooseContactID);
			for (SortEntry entry : entryList) {
				contactDAO.deleteContact(Integer.parseInt(entry.mID));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dialogLoading != null) {
				dialogLoading.dismiss();
				Tools.Toast(MergeContactActivity.this, "合并成功");
				finish();
			}
		}

		@Override
		protected void onPreExecute() {
			dialogLoading = new ProgressDialog(MergeContactActivity.this);
			dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			dialogLoading.setMessage("正在删除");
			dialogLoading.setCancelable(false);
			dialogLoading.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.i("huahua", "onProgressUpdate");
		}

	}
}
