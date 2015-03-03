package com.night.contact.ui;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.DAO.GroupDAO;
import com.night.contact.bean.GroupBean;
import com.night.contact.bean.SortEntry;
import com.night.contact.util.ImageConvert;
import com.night.contact.util.Parameter;
import com.night.contact.util.Tools;

public class AddContactActivity extends Activity {

	private Button btn_contact_back;
	private TextView btn_contact_title;
	private Button btn_save_contact;

	public EditText et_name;
	private ImageButton btn_img;
	private Bitmap contactPhotos;
	public String photoUri;
	public EditText et_phone;
	public EditText et_email;
	public EditText et_address;
	private Spinner sp_group;

	private ProgressDialog m_dialogLoading;

	private List<GroupBean> groupList;
	private String groupName;
	private ContactDAO cat;

	// 添加按钮
	private ImageButton add_phone_row_btn;

	public EditText et_phone_2;
	private ImageButton add_phone_row_btn_2;

	public EditText et_phone_3;
	private ImageButton add_phone_row_btn_3;

	/* 头像名称 */
	private static final String IMAGE_FILE_NAME = "faceImage.jpg";

	/* 请求码 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;
	// 拍照按钮
//	private ImageButton imagebtn_camera;
	// 从图库选择照片按钮
//	private ImageButton imagebtn_glary;

	private SortEntry entry;
	private int flag = 0;
	private int loadlag = 0;
	private int groupFlag = 0;// 联系人是否已存在分组 0--none 1--have
	private int photoFlag = 0;// 联系人是否已存在头像

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.contact_addnew);
		findId();
		String type = getIntent().getStringExtra("type");
		if (type != null && type.equals(Parameter.CONTACT_EDIT_TYPE)) {
			entry = getIntent().getParcelableExtra(Parameter.CONTACT_EDIT_KEY);
			groupName = entry.groupName;
			flag = 1;
			if (entry.groupId != 0) {
				groupFlag = 1;
			}
			if (entry.photoId != 0) {
				photoFlag = 1;
			}
			btn_contact_title.setText(Parameter.EDIT_CONTACT_TITLE);

			et_name.setText(entry.mName);
			String[] numbers = Tools.getPhoneNumber(entry.mNum);
			if (numbers.length == 1) {
				et_phone.setText(numbers[0]);
			} else if (numbers.length == 2) {// 2个号码
				et_phone.setText(numbers[0]);
				et_phone_2 = (EditText) this.findViewById(R.id.et_phone_2);
				View v = (View) et_phone_2.getParent();
				v.setVisibility(View.VISIBLE);
				et_phone_2.setText(numbers[1]);
			} else if (numbers.length == 3) {// 3个号码
				et_phone.setText(numbers[0]);
				et_phone_2 = (EditText) this.findViewById(R.id.et_phone_2);
				View v = (View) et_phone_2.getParent();
				v.setVisibility(View.VISIBLE);
				et_phone_2.setText(numbers[1]);

				et_phone_3 = (EditText) this.findViewById(R.id.et_phone_3);
				View v1 = (View) et_phone_3.getParent();
				v1.setVisibility(View.VISIBLE);
				et_phone_3.setText(numbers[2]);
			}

			// et_address.setText(entry.m);
			// et_email.setText(contact.getEmail());

			if (0 == entry.photoId) {
				btn_img.setImageResource(R.drawable.default_contact_photo);
			} else {
				Uri uri = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI,
						Integer.parseInt(entry.mID));
				InputStream input = ContactsContract.Contacts
						.openContactPhotoInputStream(this.getContentResolver(),
								uri);
				Bitmap contactPhoto = BitmapFactory.decodeStream(input);
				contactPhotos = contactPhoto;
				btn_img.setImageBitmap(contactPhoto);
			}
		}
	}

	public void findId() {
		btn_contact_back = (Button) this.findViewById(R.id.btn_contact_back);
		btn_contact_title = (TextView) this
				.findViewById(R.id.btn_contact_title);
		btn_save_contact = (Button) this.findViewById(R.id.btn_save_contact);

		et_name = (EditText) this.findViewById(R.id.et_name);
		btn_img = (ImageButton) this.findViewById(R.id.btn_img);
		et_phone = (EditText) this.findViewById(R.id.et_phone);
		et_email = (EditText) this.findViewById(R.id.et_email);
		et_address = (EditText) this.findViewById(R.id.et_address);
		sp_group = (Spinner) this.findViewById(R.id.sp_group);

		add_phone_row_btn = (ImageButton) this.findViewById(R.id.add_phone_row);
	}

	int count = 0;

	@Override
	protected void onResume() {
		super.onResume();

		new Thread(runnable).start();

		// 添加电话行
		add_phone_row_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				count++;
				if (count == 1) {// 第一次点击时
					add_phone_row_btn_2 = (ImageButton) AddContactActivity.this
							.findViewById(R.id.add_phone_row_2);
					et_phone_2 = (EditText) AddContactActivity.this
							.findViewById(R.id.et_phone_2);
					View v1 = (View) et_phone_2.getParent();
					v1.setVisibility(View.VISIBLE);
					// 添加删除事件监听
					add_phone_row_btn_2
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									View v1 = (View) add_phone_row_btn_2
											.getParent();
									v1.setVisibility(View.GONE);
									count--;
								}
							});
				}
				if (count == 2) {// 当第二次点击添加行时
					add_phone_row_btn_3 = (ImageButton) AddContactActivity.this
							.findViewById(R.id.add_phone_row_3);
					et_phone_3 = (EditText) AddContactActivity.this
							.findViewById(R.id.et_phone_3);
					View v2 = (View) et_phone_3.getParent();
					v2.setVisibility(View.VISIBLE);
					// 添加删除事件监听
					add_phone_row_btn_3
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									View v1 = (View) add_phone_row_btn_3
											.getParent();
									v1.setVisibility(View.GONE);
									count--;
								}
							});
				}
			}
		});

		// 头像选择
		btn_img.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showChooseDialog();
			}
		});

		// 保存
		btn_save_contact.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (loadlag != 1) {
					Toast.makeText(getApplicationContext(), "群组不能为空",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String groupName = sp_group.getSelectedItem().toString();
				if (TextUtils.isEmpty(groupName)) {// 当群组没有加载出来时用户点击修改弹出的提示
					Toast.makeText(getApplicationContext(), "姓名不能为空",
							Toast.LENGTH_SHORT).show();
					return;
				}
				int groupId = new GroupDAO(AddContactActivity.this)
						.getIdByGroupName(groupName);
				SortEntry contact = new SortEntry();
				// 获取姓名
				String name = et_name.getText().toString();
				// String address = et_address.getText().toString();
				// String email = et_email.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Toast.makeText(AddContactActivity.this, "姓名不能为空",
							Toast.LENGTH_SHORT).show();
					return;
				}

				contact.mName = name;
				// 获取手机号码
				String mobilePhone = et_phone.getText().toString();
				// 对手机号码的个数进行判断，
				// 如果添加了一行，就获取一次
				// 如果添加了两行，就获取两次
				// 这里只能添加两行
				if (et_phone_2 != null) {
					String mobilePhone_2 = et_phone_2.getText().toString()
							.trim();
					mobilePhone += "#" + mobilePhone_2;
				} else if (et_phone_3 != null) {
					String mobilePhone_2 = et_phone_2.getText().toString()
							.trim();
					String mobilePhone_3 = et_phone_3.getText().toString()
							.trim();
					mobilePhone += "#" + mobilePhone_2 + "#" + mobilePhone_3;
				}
				contact.mNum = mobilePhone;
				if (contactPhotos != null) {
					contact.contactPhoto = contactPhotos;
				}

				if (groupName == "无") {
					groupId = 0;
				}
				contact.groupId = groupId;
				if (flag == 0) {
					new AddContactTask(contact, groupId).execute();
				} else {
					contact.mID = entry.mID;
					new UpdateContactTask(contact, entry.groupId).execute();
				}
			}
		});

		// 返回
		btn_contact_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	// 可以多次运行,设置群组信息
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			GroupDAO gp = new GroupDAO(AddContactActivity.this);
			groupList = gp.getGroups();
			handler.sendEmptyMessage(0);
		}
	};
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				ArrayAdapter<String> adapter3 = setAdapterGroup(groupList);// 发送消息通知ListView更新
				sp_group.setAdapter(adapter3); // 重新设置ListView的数据适配器
				if (flag == 1) {
					Tools.setSpinnerItemSelectedByValue(sp_group, groupName);
				}
				loadlag = 1;// 群组信息加载状态 1代表加载完成
				break;
			default:
				break;
			}
		}
	};

	public ArrayAdapter<String> setAdapterGroup(List<GroupBean> list) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add("无");
		for (GroupBean bean : list) {
			adapter.add(bean.getName());
		}
		return adapter;
	}

	private AlertDialog dialog;

	/**
	 * 显示选择对话框 自定义对话框风格
	 */
	String[] items = {"拍照","从图库选择"};
	@SuppressLint("InflateParams")
	private void showChooseDialog() {

		dialog = new AlertDialog.Builder(this)
				.setTitle("设置头像")
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 1:
							Intent intentFromGallery = new Intent();
							intentFromGallery.setType("image/*"); // 设置文件类型
							intentFromGallery
									.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(intentFromGallery,
									IMAGE_REQUEST_CODE);
							break;
						case 0:
							Intent intentFromCapture = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							// 判断存储卡是否可以用，可用进行存储
							if (Tools.hasSdcard()) {
								intentFromCapture.putExtra(
										MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(new File(Environment
												.getExternalStorageDirectory(),
												IMAGE_FILE_NAME)));
							}

							startActivityForResult(intentFromCapture,
									CAMERA_REQUEST_CODE);
							break;
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
		// Window window = dialog.getWindow();
		// // 自定义alertdialog的样式
		// LayoutInflater inflater = this.getLayoutInflater();
		// View view = inflater.inflate(R.layout.show_choose_photo_dialog,
		// null);
		// window.setContentView(view);
		// // 自定义alertdialog的位置
		// LayoutParams lp = window.getAttributes();
		// lp.y = -50;
		// lp.width = 300;
		// lp.height = 230;
		// window.setAttributes(lp);
		// dialog.setCanceledOnTouchOutside(true);
		// // 设置按钮点击事件
		// TextView take_photo_tv = (TextView)
		// view.findViewById(R.id.take_photo);
		// TextView choose_glary_tv = (TextView) view
		// .findViewById(R.id.choose_glary);
		//
		// imagebtn_camera = (ImageButton)
		// view.findViewById(R.id.imagebtn_camera);
		// imagebtn_glary = (ImageButton)
		// view.findViewById(R.id.imagebtn_glary);
		//
		// // 设置按钮以及文字的点击事件，判断是拍照选择头像还是从图库选择
		// imagebtn_camera.setOnClickListener(new PhotoListener());
		// take_photo_tv.setOnClickListener(new PhotoListener());
		// imagebtn_glary.setOnClickListener(new PhotoListener());
		// choose_glary_tv.setOnClickListener(new PhotoListener());
	}

	// 选择头像点击事件
//	private class PhotoListener implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			switch (v.getId()) {
//			case R.id.take_photo:
//			case R.id.imagebtn_camera:
//				dialog.dismiss();
//				Intent intentFromCapture = new Intent(
//						MediaStore.ACTION_IMAGE_CAPTURE);
//				// 判断存储卡是否可以用，可用进行存储
//				if (Tools.hasSdcard()) {
//					intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri
//							.fromFile(new File(Environment
//									.getExternalStorageDirectory(),
//									IMAGE_FILE_NAME)));
//				}
//				startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
//				break;
//			case R.id.choose_glary:
//			case R.id.imagebtn_glary:
//				dialog.dismiss();
//				Intent intentFromGallery = new Intent();
//				intentFromGallery.setType("image/*"); // 设置文件类型
//				intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
//				startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
//				break;
//			}
//		}
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 结果码不等于取消时候
		if (resultCode != RESULT_CANCELED) {

			switch (requestCode) {
			case IMAGE_REQUEST_CODE:// 图库
				startPhotoZoom(data.getData());
				break;
			case CAMERA_REQUEST_CODE:// 相机
				if (Tools.hasSdcard()) {
					File tempFile = new File(
							Environment.getExternalStorageDirectory(),
							IMAGE_FILE_NAME);
					startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(AddContactActivity.this, "未找到存储卡，无法存储照片！",
							Toast.LENGTH_LONG).show();
				}

				break;
			case RESULT_REQUEST_CODE:// 返回值
				if (data != null) {
					contactPhotos = new ImageConvert().getImageToView(
							AddContactActivity.this, data, btn_img);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 320);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 2);
	}

	// 保存联系人
	private class AddContactTask extends AsyncTask<Void, Integer, Void> {

		private SortEntry be;
		private int groupId;

		public AddContactTask(SortEntry be, int groupId) {
			this.be = be;
			this.groupId = groupId;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!TextUtils.isEmpty(be.mName)) {
				System.out.println(be.mNum);
				cat = new ContactDAO(getApplicationContext());
				cat.addContact1(be, groupId);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (m_dialogLoading != null) {
				finish();
				// contactFlag = 2;//联系人发生了变化
				m_dialogLoading.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			m_dialogLoading = new ProgressDialog(AddContactActivity.this);
			m_dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			m_dialogLoading.setMessage("正在保存...");
			m_dialogLoading.setCancelable(false);
			m_dialogLoading.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}

	private class UpdateContactTask extends AsyncTask<Void, Integer, Void> {

		private SortEntry be;
		private int old_groupID;

		public UpdateContactTask(SortEntry be, int old_groupID) {
			this.be = be;
			this.old_groupID = old_groupID;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ContactDAO contactDAO = new ContactDAO(AddContactActivity.this);
			int raw_contact_id = Integer.parseInt(be.mID);
			System.out.println(be.mID + "  " + be.mName + " " + be.mNum + " "
					+ be.contactPhoto);
			if (groupFlag == 1 && photoFlag == 1) {// 联系人已经存在头像已分组-
				System.out.println("1");
				contactDAO.updataCotact(raw_contact_id, be, old_groupID);
			} else if (groupFlag == 0 && photoFlag == 1) {// 联系人未分组、头像存在-
				System.out.println("2");
				contactDAO.updataCotactNoGroup(raw_contact_id, be);
			} else if (groupFlag == 1 && photoFlag == 0) {// 联系人已分组、头像不存在
				System.out.println("3");
				contactDAO.updataCotactNoPhoto(raw_contact_id, be, old_groupID);
			} else {// 联系人头像不存在、未分组-
				System.out.println("4");
				contactDAO.updataCotactNoG_Photo(raw_contact_id, be);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (m_dialogLoading != null) {
				finish();
				Intent intent = new Intent();
				intent.setClass(AddContactActivity.this,
						ContactDetialActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(Parameter.CONTACT_DETIAL_KEY, be);
				intent.putExtra("type", Parameter.CONTACT_DETIAL_TYPE);
				intent.putExtras(bundle);
				startActivity(intent);
				m_dialogLoading.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			m_dialogLoading = new ProgressDialog(AddContactActivity.this);
			m_dialogLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			m_dialogLoading.setMessage("正在保存...");
			m_dialogLoading.setCancelable(false);
			m_dialogLoading.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}

}
