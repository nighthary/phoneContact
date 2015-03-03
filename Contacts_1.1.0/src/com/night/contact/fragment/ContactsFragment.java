package com.night.contact.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.DAO.GroupDAO;
import com.night.contact.adapter.FilterAdapter;
import com.night.contact.adapter.GroupAdapter;
import com.night.contact.bean.GroupBean;
import com.night.contact.bean.SortEntry;
import com.night.contact.contact.ClearEditText;
import com.night.contact.contact.ContactsCursorAdapter;
import com.night.contact.contact.SortCursor;
import com.night.contact.contact.SortCursorLoader;
import com.night.contact.observer.ContactsContentObserver;
import com.night.contact.ui.AddContactActivity;
import com.night.contact.ui.AddRemoveContactGroup;
import com.night.contact.ui.AlphabetScrollBar;
import com.night.contact.ui.ContactDetialActivity;
import com.night.contact.ui.GroupManageActivity;
import com.night.contact.ui.R;
import com.night.contact.util.Parameter;

/**
 * 联系人主界面--Fragment
 * 
 * @author NightHary
 * 
 */
@SuppressLint("InflateParams")
public class ContactsFragment extends Fragment {

	private View contactsView;

	// 最上面的layout
	private RelativeLayout acbuwa_topbar;

	// 联系人内容观察者
	private ContactsContentObserver contactsObserver;

	// 字母滑动栏
	private AlphabetScrollBar alphaBar;
	// 显示选中的字母
	private TextView letterNotice;
	// 联系人的列表
	private ListView contactslist;
	// 联系人列表的适配器
	private ContactsCursorAdapter contactsAdapter;
	// 加载器监听器
	private ContactsLoaderListener m_ContactsCallback = new ContactsLoaderListener();

	// 筛选查询后的数据list
	private ArrayList<SortEntry> filterList = new ArrayList<SortEntry>();
	// 搜索过滤联系人EditText
	private EditText filterEditText;
	// 筛选后的适配器
	private FilterAdapter fAdapter;
	// 新增联系人
	private Button btn_addContact;
	// 选中联系人ID
	private String chooseContactID;
	// 选中的联系人名字
	private String chooseContactName;
	// 选中的联系人号码
	private String chooseContactNumber;
	// 选中联系人lookupkey
	private String chooseContactLoopUpKey;
	// 选中联系人头像ID
	private int chooseContactPhotoId;
	// 进度对话框
	ProgressDialog dialogLoading;
	ProgressDialog dialog;

	// 群组
	private TextView topbar_title_tv;
	// 是否展开图标
	private ImageView topbar_group;
	// 是否展开
	private boolean isOpen = false;
	// 群组弹出框
	private PopupWindow popupwindow;
	private GroupDAO groupDAO;
	// 群组
	private ArrayList<GroupBean> groupList;
	// 弹出框view所在视图
	private ListView groupView;
	// 选择群组时显示的list
	// 管理群组按钮
	private Button group_manage;
	private boolean loadFlag = false;
	private ProgressBar process_init_data;
	private TextView tv_loading;
	private SortCursor contactsCursor;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		contactsView = inflater.inflate(R.layout.home_contact_page, null);

		contactsObserver = new ContactsContentObserver(new Handler());
		getActivity().getContentResolver().registerContentObserver(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, false,
				contactsObserver);

		acbuwa_topbar = (RelativeLayout) contactsView
				.findViewById(R.id.acbuwa_topbar);
		process_init_data = (ProgressBar) contactsView
				.findViewById(R.id.process_init_data);
		process_init_data.setVisibility(View.VISIBLE);
		tv_loading = (TextView) contactsView.findViewById(R.id.tv_loading);
		tv_loading.setVisibility(View.VISIBLE);
		// 右侧字母滑动栏
		alphaBar = (AlphabetScrollBar) contactsView
				.findViewById(R.id.fast_scroller);
		alphaBar.setOnTouchBarListener(new ScrollBarListener());
		letterNotice = (TextView) contactsView.findViewById(R.id.fast_position);
		alphaBar.setTextView(letterNotice);

		// 得到联系人列表,并设置适配器
		getActivity().getLoaderManager()
				.initLoader(0, null, m_ContactsCallback);

		contactslist = (ListView) contactsView.findViewById(R.id.pb_listvew);
		contactsAdapter = new ContactsCursorAdapter(getActivity(), null);
		contactslist.setAdapter(contactsAdapter);

		// 新增联系人
		btn_addContact = (Button) contactsView
				.findViewById(R.id.btn_add_contact);

		// 群组菜单
		topbar_title_tv = (TextView) contactsView
				.findViewById(R.id.topbar_title);
		topbar_group = (ImageView) this.contactsView
				.findViewById(R.id.topbar_group);

		filterEditText = (ClearEditText) contactsView
				.findViewById(R.id.pb_search_edit);
	}

	// 加载群组信息
	private Runnable groupRunnable = new Runnable() {
		@Override
		public void run() {
			groupDAO = new GroupDAO(getActivity());
			groupList = groupDAO.getGroups();
			groupList.add(0, new GroupBean(0, "全部"));
			groupList.add(groupList.size(), new GroupBean(0, "未分组"));
		}
	};

	@Override
	public void onResume() {
		super.onResume();

		new Thread(groupRunnable).start();

		while (loadFlag) {
			process_init_data.setVisibility(View.GONE);
			tv_loading.setVisibility(View.GONE);
		}
		// 群组菜单栏点击
		topbar_title_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!isOpen) {
					topbar_group.setImageResource(R.drawable.btn_group_click);
					isOpen = true;
				} else {
					topbar_group.setImageResource(R.drawable.btn_group_normal);
					isOpen = false;
				}
				if (popupwindow != null && popupwindow.isShowing()) {
					popupwindow.dismiss();
					return;
				} else {
					if (groupList != null) {
						initmPopupWindowView();
						popupwindow.showAsDropDown(v, 0, 5);
					}
				}
			}
		});

		// 新增联系人
		btn_addContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity().getApplicationContext(),
						AddContactActivity.class);
				startActivity(intent);
			}
		});

		// 查看联系人
		contactslist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 进行数据判断，如果搜索框内的值为空，则显示全部联系人列表
				// 如果不为空，则显示搜索list中的数据
				SortCursor contactsCursor = (SortCursor) contactsAdapter
						.getCursor();
				if (TextUtils.isEmpty(filterEditText.getText().toString()
						.trim())
						&& (topbar_title_tv.getText().toString().trim()
								.equals("全部") || topbar_title_tv.getText()
								.toString().trim().equals("联系人"))) {
					chooseContactName = contactsCursor.getName(position);
					chooseContactNumber = contactsCursor.getNumber(position);
					chooseContactID = contactsCursor.getID(position);
					chooseContactLoopUpKey = contactsCursor
							.getLookUpKey(position);
					chooseContactPhotoId = contactsCursor.getPhotoId(position);

				} else {
					chooseContactName = filterList.get(position).mName;
					chooseContactNumber = filterList.get(position).mNum;
					chooseContactID = filterList.get(position).mID;
					chooseContactLoopUpKey = filterList.get(position).lookUpKey;
					chooseContactPhotoId = filterList.get(position).photoId;
				}
				Toast.makeText(getActivity(),
						chooseContactName + chooseContactID, Toast.LENGTH_SHORT)
						.show();
				SortEntry entry = new SortEntry();
				entry.mID = chooseContactID;
				entry.mName = chooseContactName;
				entry.mNum = chooseContactNumber;
				entry.lookUpKey = chooseContactLoopUpKey;
				entry.photoId = chooseContactPhotoId;
				Intent intent = new Intent();
				// 传递点击的联系人信息到联系人详情界面
				Bundle bundle = new Bundle();
				// 设置参数类型
				bundle.putString("type", Parameter.CONTACT_DETIAL_TYPE);
				// 设置具体参数
				bundle.putParcelable(Parameter.CONTACT_DETIAL_KEY, entry);
				intent.putExtras(bundle);
				intent.setClass(getActivity().getApplicationContext(),
						ContactDetialActivity.class);
				startActivity(intent);
			}
		});

		// 搜索联系人
		filterEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (!"".equals(s.toString().trim())) {
					// 根据编辑框值过滤联系人并更新联系列表
					SortCursor contactsCursor = (SortCursor) contactsAdapter
							.getCursor();
					filterList = contactsCursor.filterSearch(s.toString()
							.trim());

					fAdapter = new FilterAdapter(getActivity(), filterList);
					contactslist.setAdapter(fAdapter);
				} else {
					contactsAdapter.notifyDataSetChanged();
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

		// 联系人列表长按监听
		contactslist.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				// 震动
				Vibrator vib = (Vibrator) getActivity().getSystemService(
						Service.VIBRATOR_SERVICE);
				vib.vibrate(50);

				// 进行数据判断，如果搜索框内的值为空，则显示全部联系人列表
				// 如果不为空，则显示搜索list中的数据
				if (TextUtils.isEmpty(filterEditText.getText().toString()
						.trim())
						&& (topbar_title_tv.getText().toString().trim()
								.equals("全部") || topbar_title_tv.getText()
								.toString().trim().equals("联系人"))) {
					SortCursor contactsCursor = (SortCursor) contactsAdapter
							.getCursor();
					chooseContactName = contactsCursor.getName(arg2);
					chooseContactNumber = contactsCursor.getNumber(arg2);
					chooseContactID = contactsCursor.getID(arg2);
				} else {
					chooseContactName = filterList.get(arg2).mName;
					chooseContactNumber = filterList.get(arg2).mNum;
					chooseContactID = filterList.get(arg2).mID;
				}
				showLongClickDialog();
				return false;
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	// 初始化群组菜单
	@SuppressWarnings("deprecation")
	public void initmPopupWindowView() {
		// // 获取自定义布局文件pop.xml的视图
		View customView = getActivity().getLayoutInflater().inflate(
				R.layout.popview_page, null, false);
		groupView = (ListView) customView.findViewById(R.id.lv_group);
		// 创建PopupWindow实例,200,150分别是宽度和高度
		popupwindow = new PopupWindow(customView, 150, 270);
		// 设置动画效果 [R.style.AnimationFade 是自己事先定义好的]
		popupwindow.setAnimationStyle(R.style.AnimationFade);
		// 附着在哪个view（应该是根据这个parent来确定弹出位置），相对于parent的x轴偏移 ，相对于parent的y轴偏移
		popupwindow.showAsDropDown(customView, 150, 100);

		// 使其聚集
		popupwindow.setFocusable(true);
		// 设置允许在外点击消失
		popupwindow.setOutsideTouchable(true);
		// 刷新状态（必须刷新否则无效）
		popupwindow.update();

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupwindow.setBackgroundDrawable(new BitmapDrawable());

		// 自定义view添加触摸事件
		customView.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupwindow != null && popupwindow.isShowing()) {
					popupwindow.dismiss();
					popupwindow = null;
				}
				return false;
			}
		});
		groupView.setDividerHeight(1);
		// 群组管理按钮
		group_manage = (Button) customView.findViewById(R.id.group_manage);
		group_manage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popupwindow.dismiss();
				Intent intent = new Intent();
				intent.setClass(getActivity(), GroupManageActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList(Parameter.GROUP_LIST,
						(ArrayList<? extends Parcelable>) groupList);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		groupView.setAdapter(new GroupAdapter(getActivity(), groupList));
		groupView.setOnItemClickListener(new GroupListener());
	}

	// 群组菜单点击
	private class GroupListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			int groupID = groupList.get(position).getId();
			String groupName = groupList.get(position).getName();
			SortCursor data = (SortCursor) contactsAdapter.getCursor();
			topbar_title_tv.setText(groupName);
			filterList.clear();
			if (groupName == "全部") {
				contactsAdapter.notifyDataSetChanged();
				contactslist.setAdapter(contactsAdapter);
			} else if (groupName == "未分组") {
				filterList = data.filterGroup(0);
				fAdapter.notifyDataSetChanged();
				
				fAdapter = new FilterAdapter(getActivity(), filterList);
				contactslist.setAdapter(fAdapter);
			} else {
				filterList = data.filterGroup(groupID);
				fAdapter = new FilterAdapter(getActivity(), filterList);
				fAdapter.notifyDataSetChanged();
				contactslist.setAdapter(fAdapter);
			}
			// 隐藏菜单栏
			if (popupwindow != null && popupwindow.isShowing()) {
				popupwindow.dismiss();
				popupwindow = null;
			}
		}
	}

	String[] items = { "拨号", "删除联系人" };

	// 长按显示的对话框
	private void showLongClickDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle(chooseContactName)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent intent = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel://" + chooseContactNumber));
							startActivity(intent);
							break;
						case 1:
							new AlertDialog.Builder(getActivity())
									.setTitle("")
									.setIcon(R.drawable.top_bar_bg)
									.setMessage(
											"删除联系人" + chooseContactName + "?")
									.setPositiveButton(
											"确定",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													// 删除联系人操作,放在线程中处理
													new DeleteContactTask()
															.execute();
												}
											})
									.setNegativeButton(
											"取消",
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
												}
											}).show();
							break;
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
		// Window window = longDialog.getWindow();
		// // 自定义alertdialog的样式
		// LayoutInflater inflater = getActivity().getLayoutInflater();
		// View view = inflater.inflate(R.layout.show_longclick_contact_dialog,
		// null);
		// window.setContentView(view);
		// LayoutParams lp = window.getAttributes();
		// lp.y = -50;
		// lp.width = 300;
		// lp.height = 230;
		// window.setAttributes(lp);
		// longDialog.setCanceledOnTouchOutside(true);
		//
		// // 获取弹出对话框的组件id
		// TextView dialog_title_tv = (TextView) view
		// .findViewById(R.id.dialog_title_tv);
		// dialog_title_tv.setText(chooseContactName);
		//
		// TextView dial_tv = (TextView) view.findViewById(R.id.dial_tv);
		// TextView delete_contact_tv = (TextView) view
		// .findViewById(R.id.delete_contact_tv);
		//
		// btn_dial = (Button) view.findViewById(R.id.imagebtn_dial);
		// btn_delete_contact = (Button) view
		// .findViewById(R.id.imagebtn_delete_contact);
		// // 设置点击监听器
		// dial_tv.setOnClickListener(new LongClickListener());
		// delete_contact_tv.setOnClickListener(new LongClickListener());
		// btn_dial.setOnClickListener(new LongClickListener());
		// btn_delete_contact.setOnClickListener(new LongClickListener());
	}

	// // 长按联系人的事件监听
	// private class LongClickListener implements OnClickListener {
	//
	// @Override
	// public void onClick(View v) {
	// switch (v.getId()) {
	// case R.id.dial_tv:
	// case R.id.imagebtn_dial:
	// longDialog.dismiss();
	// Intent intent = new Intent(Intent.ACTION_CALL,
	// Uri.parse("tel://" + chooseContactNumber));
	// startActivity(intent);
	// break;
	// case R.id.delete_contact_tv:
	// case R.id.imagebtn_delete_contact:
	// longDialog.dismiss();
	// new AlertDialog.Builder(getActivity())
	// .setTitle("")
	// .setIcon(R.drawable.top_bar_bg)
	// .setMessage("删除联系人" + chooseContactName + "?")
	// .setPositiveButton("确定",
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// // 删除联系人操作,放在线程中处理
	// new DeleteContactTask().execute();
	// }
	// })
	// .setNegativeButton("取消",
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// }
	// }).show();
	// break;
	// }
	// }
	//
	// }

	// 删除联系人
	private class DeleteContactTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			new ContactDAO(getActivity()).deleteContact(Integer
					.parseInt(chooseContactID));
			// Utils.deleteContact(chooseContactID);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dialogLoading != null) {
				contactsAdapter.notifyDataSetChanged();
				dialogLoading.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			dialogLoading = new ProgressDialog(getActivity());
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		ViewGroup p = (ViewGroup) contactsView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return contactsView;
	}

	// 加载器的监听器
	private class ContactsLoaderListener implements
			LoaderManager.LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			System.out.println("onCreateLoader...");
			return new SortCursorLoader(getActivity(),
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			contactsAdapter.swapCursor(arg1);
			System.out.println("onLoadFinished...");
			process_init_data.setVisibility(View.GONE);
			tv_loading.setVisibility(View.GONE);
			contactsCursor = (SortCursor) contactsAdapter.getCursor();
			if (acbuwa_topbar.getVisibility() == View.VISIBLE) {
				// Utils.mPersons = data.GetContactsArray();
			} else {
				filterList = contactsCursor.filterSearch(filterEditText
						.getText().toString().trim());
				// fAdapter.notifyDataSetChanged();
				fAdapter = new FilterAdapter(getActivity(), filterList);
				fAdapter.notifyDataSetChanged();
				contactslist.setAdapter(fAdapter);
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			contactsAdapter.swapCursor(null);

		}

	}

	// 字母列触摸的监听器
	private class ScrollBarListener implements
			AlphabetScrollBar.OnTouchBarListener {

		@Override
		public void onTouch(String letter) {

			// 触摸字母列时,将联系人列表更新到首字母出现的位置
			SortCursor contactsCursor = (SortCursor) contactsAdapter
					.getCursor();
			if (contactsCursor != null) {
				int idx = contactsCursor.binarySearch(letter);
				if (idx != -1) {
					contactslist.setSelection(idx);
				}
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.add(0, 1, 1, "批量删除");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			Intent intent = new Intent(getActivity(),
					AddRemoveContactGroup.class);
			intent.putExtra("type", "bathdelete");
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
}
