package com.night.contact.ui;

import java.io.InputStream;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.DAO.GroupDAO;
import com.night.contact.bean.SortEntry;
import com.night.contact.util.Parameter;
import com.night.contact.util.Tools;

/**
 * 联系人详情界面
 * @author NightHary
 *
 */
public class ContactDetialActivity extends Activity {
	
	private Button btn_contact_back;
	private Button btn_editContact;
	
	private TextView tv_contact_group;
	private TextView tv_contact_name;
	private ImageButton btn_contact_photo;
	
	private GroupDAO groupDAO;
	
	private static int contactFlag = 0;
	
	private SortEntry entry;
	//电话和短信按钮
	private Button contact_num_btn_1;
	private Button tv_conatct_sms_1;
	//电话和短信按钮
	private Button contact_num_btn_2;
	private Button tv_conatct_sms_2;
	//号码3
	private Button contact_num_btn_3;
	private Button tv_conatct_sms_3;
	
	private String[] numbers;//电话数组
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_detail);
		
		btn_contact_back = (Button) this.findViewById(R.id.btn_contact_back);
		btn_editContact = (Button) this.findViewById(R.id.btn_editContact);
		tv_contact_name = (TextView) this.findViewById(R.id.tv_contact_name);
		tv_contact_group = (TextView) this.findViewById(R.id.tv_contact_group);
		btn_contact_photo = (ImageButton) this.findViewById(R.id.btn_contact_photo);
		groupDAO = new GroupDAO(ContactDetialActivity.this);
		
		contact_num_btn_1  = (Button) this.findViewById(R.id.contact_num_btn_1);
		tv_conatct_sms_1 = (Button) this.findViewById(R.id.tv_conatct_sms_1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//获取联系人信息并设置到界面
		String type = getIntent().getStringExtra("type");
		if(type.equals(Parameter.CONTACT_DETIAL_TYPE)){
			entry = getIntent().getParcelableExtra(Parameter.CONTACT_DETIAL_KEY);
		}
		//显示姓名
		tv_contact_name.setText(entry.mName);
		//显示电话号码
		String phoneNumber = entry.mNum;
		System.out.println(phoneNumber);
		numbers = Tools.getPhoneNumber(phoneNumber);
		if(numbers.length == 1){//只有一个号码
			contact_num_btn_1.setText(numbers[0]);
		}else if(numbers.length == 2){//两个号码,获取组件，并设置号码
			contact_num_btn_2 = (Button) this.findViewById(R.id.contact_num_btn_2);
			tv_conatct_sms_2 = (Button) this.findViewById(R.id.tv_conatct_sms_2);
			View v = (View) contact_num_btn_2.getParent();
			v.setVisibility(View.VISIBLE);
			contact_num_btn_1.setText(numbers[0]);
			contact_num_btn_2.setText(numbers[1]);
		}else if(numbers.length == 3){//两个号码,获取组件，并设置号码
			contact_num_btn_2 = (Button) this.findViewById(R.id.contact_num_btn_2);
			tv_conatct_sms_2 = (Button) this.findViewById(R.id.tv_conatct_sms_2);
			contact_num_btn_3 = (Button) this.findViewById(R.id.contact_num_btn_3);
			tv_conatct_sms_3 = (Button) this.findViewById(R.id.tv_conatct_sms_3);
			View v = (View) contact_num_btn_2.getParent();
			v.setVisibility(View.VISIBLE);
			View v1 = (View) contact_num_btn_3.getParent();
			v1.setVisibility(View.VISIBLE);
			contact_num_btn_1.setText(numbers[0]);
			contact_num_btn_2.setText(numbers[1]);
			contact_num_btn_3.setText(numbers[2]);
		}
		
		//设置群组
		int groupId = new ContactDAO(this).getGroupIdByContactId(Integer.parseInt(entry.mID));
		entry.groupId = groupId;
		String groupName = groupDAO.getGroupNameByGroupId(groupId);
		entry.groupName = groupName;
		if(groupId == 0){
			tv_contact_group.setText("未分组");
		}else{
			tv_contact_group.setText(groupName);
		}
		
		//头像
		if(0 == entry.photoId){
			btn_contact_photo.setImageResource(R.drawable.default_contact_photo);
		}else{
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Integer.parseInt(entry.mID));
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(this.getContentResolver(), uri); 
			Bitmap contactPhoto = BitmapFactory.decodeStream(input);
			btn_contact_photo.setImageBitmap(contactPhoto);
		}
		
		
		//返回
		btn_contact_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ContactDetialActivity.this.finish();
			}
		});
		
		//编辑联系人
		btn_editContact.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				contactFlag = 1;
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("type",Parameter.CONTACT_EDIT_TYPE);
				bundle.putParcelable(Parameter.CONTACT_EDIT_KEY, entry);
				intent.putExtras(bundle);
				intent.setClass(ContactDetialActivity.this,AddContactActivity.class);
				startActivity(intent);
			}
		});
		
		addListener(contact_num_btn_1,tv_conatct_sms_1,numbers[0]);
		//如果两个button不为空即已经根据id获取了值，即该联系人存在两个号码
		if(contact_num_btn_2 != null && tv_conatct_sms_2 != null){
			addListener(contact_num_btn_2,tv_conatct_sms_2,numbers[1]);
		}
		if(contact_num_btn_3 != null && tv_conatct_sms_3 != null){
			addListener(contact_num_btn_3,tv_conatct_sms_3,numbers[2]);
		}
	}

	public static int getContactFlag() {
		return contactFlag;
	}

	public static void setContactFlag(int contactFlag) {
		ContactDetialActivity.contactFlag = contactFlag;
	}
	
	private void addListener(View viewId_phone,View viewId_sms,final String number){
		//拨打电话
		viewId_phone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						Intent.ACTION_CALL,
						Uri.parse("tel://"
								+ number));
				startActivity(intent);
			}
		});
		//发送短信
		viewId_sms.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri smsToUri = Uri.parse("smsto://134");
				Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri );
				startActivity(mIntent);
			}
		});
	}
}
