package com.night.contact.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.bean.SortEntry;
import com.night.contact.contact.SortCursor;
import com.night.contact.ui.MergeContactActivity;
import com.night.contact.ui.R;
import com.night.contact.util.NameCompartor;
import com.night.contact.util.NumCompartor;
import com.night.contact.util.Tools;

@SuppressLint({ "InflateParams", "HandlerLeak" })
public class SettingFragment extends Fragment implements
		OnFocusChangeListener {
	private View concertView;

	private LinearLayout ll_backup_contact;
	private LinearLayout ll_restore_contact;
	private LinearLayout ll_merge_contact;
	private ContactDAO contactDAO;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		concertView = inflater.inflate(R.layout.activity_tab_setting, null);

		ll_backup_contact = (LinearLayout) concertView
				.findViewById(R.id.ll_backup_contact);

		ll_restore_contact = (LinearLayout) concertView
				.findViewById(R.id.ll_restore_contact);
		
		ll_merge_contact = (LinearLayout) concertView
				.findViewById(R.id.ll_merge_contact);

		contactDAO = new ContactDAO(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		ViewGroup p = (ViewGroup) concertView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return concertView;
	}

	public void onResume() {
		super.onResume();
		//备份
		ll_backup_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(getActivity())
						.setTitle("备份联系人")
						.setMessage(
								"联系人将备份到"
										+ Environment
												.getExternalStorageDirectory()
										+ "/example.vcf")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										backup_dialog = ProgressDialog.show(
												getActivity(), "备份联系人",
												"正在备份...", true);
										backupThread();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();

			}
		});
		ll_backup_contact.setOnFocusChangeListener(this);
		
		//还原
		ll_restore_contact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				restore_dialog = ProgressDialog.show(getActivity(), "还原联系人",
						"正在还原...", true);
				restoreThread();
			}
		});
		ll_restore_contact.setOnFocusChangeListener(this);
		
		//去重
		ll_merge_contact.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(),MergeContactActivity.class);
				entrys = contactDAO.getContacts1();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("entrys",(ArrayList<? extends Parcelable>) entrys);
				
				//按电话号码排序，然后进行查找相同号码
				Collections.sort(entrys,new NumCompartor());
				List<SortEntry> numList= Tools.duplicateNum(entrys);
				
//				for(SortEntry entry : numList){
//					System.out.println(entry.mName+"  "+entry.mNum);
//				}
				//按姓名排序，然后进行查找相同姓名
				Collections.sort(entrys,new NameCompartor());
				List<SortEntry> nameList = Tools.duplicateName(entrys);
				
//				for(SortEntry entry : nameList){
//					System.out.println(entry.mName+"  "+entry.mNum);
//				}
//				numList.addAll(nameList);
				
			//	Collections.sort(numList,new NumCompartor());
				
				if(numList.isEmpty() && nameList.isEmpty()){
					Tools.Toast(getActivity(),"暂无重复联系人");
				}else{
					bundle.putParcelableArrayList("numList",(ArrayList<? extends Parcelable>) numList);
					bundle.putParcelableArrayList("nameList",(ArrayList<? extends Parcelable>) nameList);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
		ll_merge_contact.setOnFocusChangeListener(this);
		
	}

	ProgressDialog backup_dialog;
	ProgressDialog restore_dialog;
	SortCursor contactCursor;
	List<SortEntry> entrys;

	/**
	 * 备份联系人
	 * 
	 * @param entrys
	 *            需要备份的联系人数据
	 */
	public void backupContact(List<SortEntry> entrys) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) // 判断存储卡是否存在
		{
			OutputStreamWriter writer;
			File file = new File(Environment.getExternalStorageDirectory(),
					"example.vcf");
			// 得到存储卡的根路径，将example.vcf写入到根目录下
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file),
						"UTF-8");

				VCardComposer composer = new VCardComposer();
				if(entrys != null){
					for (SortEntry entry : entrys) {
						ContactStruct contact1 = new ContactStruct();
						contact1.name = entry.mName;
						contact1.addPhone(Contacts.Phones.TYPE_MOBILE, entry.mNum,
								null, true);
						String vcardString;
						vcardString = composer.createVCard(contact1,
								VCardComposer.VERSION_VCARD30_INT);
						writer.write(vcardString);
						writer.write("\n");

						writer.flush();
					}
				}
				writer.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (VCardException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				throw new Exception("写入失败，SD卡不存在");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 备份联系人子线程处理
	 */
	private void backupThread() {
		final Handler handler = new Handler() {

			public void handleMessage(Message msg) {
				backup_dialog.dismiss();
				Toast.makeText(getActivity().getApplicationContext(),
						"已成功导入SD卡中！", Toast.LENGTH_SHORT).show();
			}
		};

		Thread myThread = new Thread() {

			@Override
			public void run() {
				super.run();
				entrys = contactDAO.getContacts();
				backupContact(entrys);
				handler.sendEmptyMessage(0);
			}
		};
		myThread.start();
	}

	/**
	 * 还原联系人子线程
	 */
	private void restoreThread() {
		final Handler handler = new Handler() {

			public void handleMessage(Message msg) {
				restore_dialog.dismiss();
				Toast.makeText(getActivity(), "导入联系人信息成功!", Toast.LENGTH_LONG)
						.show();
			}
		};

		Thread restoreThread = new Thread() {
			@Override
			public void run() {
				super.run();
				restoreContact();
				handler.sendEmptyMessage(0);
			}
		};
		restoreThread.start();
	}

	/**
	 * 还原联系人
	 */
	private void restoreContact() {
		try {
			// 获取要恢复的联系人信息
			List<SortEntry> infoList = contactDAO.restoreContacts();
			System.out.println(infoList.size());
			if(infoList != null){
				for (SortEntry entry : infoList) {
					// 恢复联系人
					contactDAO.addContact1(entry, 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.ll_backup_contact:
			ll_backup_contact
					.setBackgroundResource(R.drawable.btn_save_pressed);
			break;
		case R.id.ll_restore_contact:
			ll_restore_contact
					.setBackgroundResource(R.drawable.btn_save_pressed);
			break;
		}
	}

}
