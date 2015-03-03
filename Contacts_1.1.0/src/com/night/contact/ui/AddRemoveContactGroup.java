package com.night.contact.ui;


import java.io.InputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.night.contact.DAO.GroupDAO;
import com.night.contact.adapter.FilterAdapter;
import com.night.contact.bean.SortEntry;
import com.night.contact.contact.SortCursor;
import com.night.contact.contact.SortCursorLoader;
import com.night.contact.util.Tools;

@SuppressLint("HandlerLeak")
public class AddRemoveContactGroup extends Activity{
	
	// 最上面的layout
	private RelativeLayout acbuwa_topbar;
	//字母列视图View
	private AlphabetScrollBar asb;
	//显示选中的字母
	private TextView letterNotice;
	//联系人的列表
	private ListView contactslist;
	//联系人列表的适配器
	private ContactsCursorAdapter contactsAdapter;
	//加载器监听器
	private ContactsLoaderListener m_loaderCallback = new ContactsLoaderListener();
	private String type;//操作类型
	private int groupID;
	//所有联系人的数据list
	private ArrayList<SortEntry> sortList = new ArrayList<SortEntry>();
	//筛选查询后的数据list
	private ArrayList<SortEntry> filterList = new ArrayList<SortEntry>();
	// 搜索过滤联系人EditText
	private EditText filterEditText;
	// 筛选后的适配器
	private FilterAdapter fAdapter;
	//选中多少个需要删除的联系人
	private int choosenum=0;
	//批量删除按钮
	private Button sureNumBtn;
	private TextView txtAddContactToGroup_Title;
	//没有匹配联系人时显示的TextView
	private TextView listEmptyText;
	//选中全部按钮
	private Button selectAllBtn;
	//选择所有联系人的标志
	private boolean selectAll = false;
	//id的数组
	private ArrayList<String> chooseContactsID = new ArrayList<String>();
	private ProgressDialog m_dialogLoading;
	private SortCursor contactsCursor;
	
	private ImageButton imbAddContactToGroup_Back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_contact_to_group);
		
		txtAddContactToGroup_Title = (TextView) this.findViewById(R.id.txtAddContactToGroup_Title);
		type = getIntent().getExtras().getString("type");
		groupID = getIntent().getExtras().getInt("groupID");
		
		//得到联系人列表,并设置适配器
		getLoaderManager().initLoader(0,null,m_loaderCallback);
		contactslist = (ListView)findViewById(R.id.pb_listvew);
		contactsAdapter = new ContactsCursorAdapter(this, null);
		contactsAdapter.setData(sortList);
		contactslist.setAdapter(contactsAdapter);
		
		//得到字母列的对象,并设置触摸响应监听器
		asb = (AlphabetScrollBar)findViewById(R.id.fast_scroller);
		asb.setOnTouchBarListener(new ScrollBarListener());
		letterNotice = (TextView)findViewById(R.id.fast_position);
		asb.setTextView(letterNotice);
				
		listEmptyText = (TextView)findViewById(R.id.nocontacts_notice);
		acbuwa_topbar = (RelativeLayout)findViewById(R.id.acbuwa_topbar);
		sureNumBtn = (Button)findViewById(R.id.sure_num);
		selectAllBtn = (Button)findViewById(R.id.select_all);
		
		//初始化搜索编辑框,设置文本改变时的监听器
		filterEditText = (EditText)findViewById(R.id.pb_search_edit);
		
		imbAddContactToGroup_Back = (ImageButton) this.findViewById(R.id.imbAddContactToGroup_Back);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(type.equals("add")){
			Tools.Toast(AddRemoveContactGroup.this,type);
			txtAddContactToGroup_Title.setText("向群组添加成员");
		}else{
			Tools.Toast(AddRemoveContactGroup.this,type);
			txtAddContactToGroup_Title.setText("从群组移除成员");
		}
		//返回
		imbAddContactToGroup_Back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// 搜索联系人
		filterEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (!"".equals(s.toString().trim())) {
					// 根据编辑框值过滤联系人并更新联系列表
//					SortCursor contactsCursor = (SortCursor) contactsAdapter.getCursor();
//					getContactCursor();
					filterList = contactsCursor.filterSearch1(s.toString().trim(),sortList);

					fAdapter = new FilterAdapter(AddRemoveContactGroup.this, filterList);
					contactslist.setAdapter(fAdapter);
				} else {
					contactslist.setAdapter(contactsAdapter);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
				
		//联系人点击事件监听，点击就选中或者反选
		contactslist.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(sortList.get(arg2).mchoose == 0)
				{
					sortList.get(arg2).mchoose = 1;
					choosenum++;
					chooseContactsID.add(sortList.get(arg2).mID);
				}
				else
				{
					sortList.get(arg2).mchoose = 0;
					for(int i=0;i<choosenum;i++)
					{
						if(chooseContactsID.get(i).equals(sortList.get(arg2).mID))
						{
							chooseContactsID.remove(i);
							break;
						}
					}
					choosenum--;
				}
				
				contactsAdapter.notifyDataSetChanged();
				sureNumBtn.setText("确定("+ choosenum +")");
			}
		});
		
		//确定按钮和选择全部按钮
		sureNumBtn.setOnClickListener(new BtnClick());
		selectAllBtn.setOnClickListener(new BtnClick());
	}
	
	private class BtnClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.sure_num)
			{
				if(choosenum>0)
				{
					if(type.equals("add")){
						new AddContactToGTask(groupID).execute();
					}else if(type.equals("remove")){
						new RemoveContactFromGTask(groupID).execute();
					}
				}
				else
				{
					Tools.Toast(AddRemoveContactGroup.this,"请选择要"+Tools.pinYinToHanZi(type)+"的联系人");
				}
			}
			else if(v.getId() == R.id.select_all)
			{
				chooseContactsID.clear();
				if(!selectAll)
				{
					for(int i=0;i<sortList.size();i++)
					{
						sortList.get(i).mchoose = 1;
						chooseContactsID.add(sortList.get(i).mID);
					}
					choosenum = sortList.size();
					sureNumBtn.setText("确定("+ sortList.size() +")");
					selectAllBtn.setText("取消全部");
					contactsAdapter.notifyDataSetChanged();
					selectAll = !selectAll;
				}
				else
				{
					for(int i=0;i<sortList.size();i++)
					{
						sortList.get(i).mchoose = 0;
					}
					choosenum = 0;
					sureNumBtn.setText("确定(0)");
					selectAllBtn.setText("选择全部");
					contactsAdapter.notifyDataSetChanged();
					selectAll = !selectAll;
				}
			}
		}
	}
	
	// 加载器的监听器
	private class ContactsLoaderListener implements
			LoaderManager.LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new SortCursorLoader(AddRemoveContactGroup.this,
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			contactsAdapter.swapCursor(arg1);
//			new LoadAnysTack().execute();
			contactsCursor = (SortCursor) contactsAdapter.getCursor();
			if (acbuwa_topbar.getVisibility() == View.VISIBLE) {
				if(type.equals("add")){
					sortList  = contactsCursor.getAddContactsArray(groupID);
				}else{
					sortList  = contactsCursor.getRemoveContactsArray(groupID);
				}
				contactsAdapter.setData(sortList);
				contactslist.setAdapter(contactsAdapter);
			}else {
				filterList = contactsCursor.filterSearch(filterEditText.getText()
						.toString().trim());
				fAdapter = new FilterAdapter(AddRemoveContactGroup.this, filterList);
				contactslist.setAdapter(fAdapter);
			}
		}
		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			contactsAdapter.swapCursor(null);
		}
	}
	
	//字母列触摸的监听器
	private class ScrollBarListener implements AlphabetScrollBar.OnTouchBarListener {

		@Override
		public void onTouch(String letter) {
			
			//触摸字母列时,将联系人列表更新到首字母出现的位置
//			contactsCursor = (SortCursor)contactsAdapter.getCursor();
//			getContactCursor();
			if(contactsCursor != null) 
			{
				int idx = contactsCursor.binarySearch(letter);
				if(idx != -1)
				{
					contactslist.setSelection(idx);
				}
			}
		}
	}
	
	private class ContactsCursorAdapter extends CursorAdapter{
		private Context context;
		private ArrayList<SortEntry> lists;
		@SuppressWarnings("deprecation")
		public ContactsCursorAdapter(Context context, Cursor c) {
			super(context, c);
			this.context = context;
		}
		public void setData(ArrayList<SortEntry> entryLists){
			this.lists = entryLists;
		}
		
		@Override
		public SortEntry getItem(int position) {
			return sortList.get(position);
		}

		@Override
		public int getCount() {
			return sortList.size();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			{
				convertView = LayoutInflater.from(context).inflate(R.layout.list_item_add_remove_contact, parent, false);
			}
			
			ImageView photo = (ImageView) convertView.findViewById(R.id.contact_photo);
	        
	        int photoId = lists.get(position).photoId;
	        String contactID = lists.get(position).mID;
	        if(0 == photoId){
	        	photo.setImageResource(R.drawable.default_contact_photo);
	        }else{
	        	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,Integer.parseInt(contactID));
				InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri); 
				Bitmap contactPhoto = BitmapFactory.decodeStream(input);
				photo.setImageBitmap(contactPhoto);
	        }
	        
            TextView name = (TextView) convertView.findViewById(R.id.contacts_name);
            name.setText(lists.get(position).mName);
			if(lists.get(position).mchoose == 1)
			{
				ImageView choosecontact = (ImageView)convertView.findViewById(R.id.choose_contact);
				choosecontact.setImageResource(R.drawable.cb_checked);
			}
			else
			{
				ImageView choosecontact = (ImageView)convertView.findViewById(R.id.choose_contact);
				choosecontact.setImageResource(R.drawable.cb_unchecked);
			}

			return convertView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if(cursor == null)
			{
				return;
			}
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return LayoutInflater.from(context).inflate(R.layout.list_item_home_contacts, parent, false);
		}
		
	}
	
	
	/**
	 * 添加成员到群组
	 * 
	 * @author NightHary
	 * 
	 */
	private class AddContactToGTask extends AsyncTask<Void, Void, ArrayList<SortEntry>> {
		int groupID;

		public AddContactToGTask(int groupID) {
			this.groupID = groupID;
		}

		@Override
		protected ArrayList<SortEntry> doInBackground(Void... params) {
			ArrayList<SortEntry> entrys;
			for(String contactId : chooseContactsID){
				new GroupDAO(AddRemoveContactGroup.this).addMemberToGroup(Integer.parseInt(contactId),groupID);
			}
		//	contactsCursor = (SortCursor) contactsAdapter.getCursor();
			if(type.equals("add")){
				entrys  = contactsCursor.getAddContactsArray(groupID);
			}else{
				entrys  = contactsCursor.getRemoveContactsArray(groupID);
			}
			return entrys;
		}

		@Override
		protected void onPostExecute(ArrayList<SortEntry> result) {
			super.onPostExecute(result);
			if (m_dialogLoading != null) {
				if(result.isEmpty()){
					listEmptyText.setVisibility(View.VISIBLE);
				}
				contactsAdapter.setData(result);
				contactslist.setAdapter(contactsAdapter);
				contactsAdapter.notifyDataSetChanged();
				sureNumBtn.setText("确定(0)");
				choosenum = 0;
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				m_dialogLoading.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			m_dialogLoading = new ProgressDialog(AddRemoveContactGroup.this);
			m_dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			m_dialogLoading.setMessage("正在添加...");
			m_dialogLoading.setCancelable(false);
			m_dialogLoading.show();
		}
	}

	/**
	 * 从群组移除成员
	 * 
	 * @author NightHary
	 * 
	 */
	private class RemoveContactFromGTask extends AsyncTask<Void, Void, ArrayList<SortEntry>> {
		int groupID;
		
		public RemoveContactFromGTask(int groupID) {
			this.groupID = groupID;
		}

		@Override
		protected ArrayList<SortEntry> doInBackground(Void... params) {
			ArrayList<SortEntry> entrys;
			for(String contactId : chooseContactsID){
				new GroupDAO(AddRemoveContactGroup.this).deleteMemberFromGroup(Integer.parseInt(contactId),groupID);
			}
			contactsCursor = (SortCursor) contactsAdapter.getCursor();
			if(type.equals("add")){
				entrys  = contactsCursor.getAddContactsArray(groupID);
			}else{
				entrys  = contactsCursor.getRemoveContactsArray(groupID);
			}
			return entrys;
		}

		@Override
		protected void onPostExecute(ArrayList<SortEntry> result) {
			super.onPostExecute(result);
			if (m_dialogLoading != null) {
				if(result.isEmpty()){
					listEmptyText.setVisibility(View.VISIBLE);
				}
				contactsAdapter.setData(result);
				contactslist.setAdapter(contactsAdapter);
				contactsAdapter.notifyDataSetChanged();
				sureNumBtn.setText("确定(0)");
				choosenum = 0;
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				m_dialogLoading.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			m_dialogLoading = new ProgressDialog(AddRemoveContactGroup.this);
			m_dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			m_dialogLoading.setMessage("正在移除...");
			m_dialogLoading.setCancelable(false);
			m_dialogLoading.show();
		}
	}
}
