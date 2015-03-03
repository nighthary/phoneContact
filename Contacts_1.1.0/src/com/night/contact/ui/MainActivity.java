package com.night.contact.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.night.contact.adapter.FragmentAdapter;
import com.night.contact.contact.SortCursor;
import com.night.contact.fragment.ContactsFragment;
import com.night.contact.fragment.DialFragment;
import com.night.contact.fragment.SettingFragment;

public class MainActivity extends FragmentActivity{

	private ViewPager mPageVp;

	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	private FragmentAdapter mFragmentAdapter;
	
	/**
	 * Tab显示内容TextView
	 */
	private TextView mTabDialTv, mTabContactsTv,mTabSettingTv;
	/**
	 * Tab显示内容ImageButton
	 */
	private ImageButton mTabDialIb,mTabContactIb,mTabSettingIb;
	/**
	 * Fragment
	 */
	private DialFragment mDialFg;
	private SettingFragment mSettingFg;
	private ContactsFragment mContactsFg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.activity_main);
		findById();
		init();
		//初始化数据库操作类
		SortCursor.init(this);
		
		mTabDialTv.setOnClickListener(new MyListener());
		mTabContactsTv.setOnClickListener(new MyListener());
		mTabSettingTv.setOnClickListener(new MyListener());
		mTabDialIb.setOnClickListener(new MyListener());
		mTabContactIb.setOnClickListener(new MyListener());
		mTabSettingIb.setOnClickListener(new MyListener());
		
	}
	
	private class MyListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			resetTextView();
			resetButtonView();
			switch(v.getId()){
			case R.id.id_dial_tv:
			case R.id.id_imagebtn_dial:
				mTabDialIb.setImageResource(R.drawable.tab_dial_selected);
				mTabDialTv.setTextColor(Color.BLUE);
				mPageVp.setCurrentItem(0);
				break;
			case R.id.id_contacts_tv:
			case R.id.id_imagebtn_contacts:
				mTabContactIb.setImageResource(R.drawable.tab_contact_selected);
				mTabContactsTv.setTextColor(Color.BLUE);
				mPageVp.setCurrentItem(1);
				break;
			case R.id.id_settings_tv:
			case R.id.id_imagebtn_setting:
				mTabSettingIb.setImageResource(R.drawable.tab_setting_selected);
				mTabSettingTv.setTextColor(Color.BLUE);
				mPageVp.setCurrentItem(2);
				break;
			}
		}
	}
	private void findById() {
		mTabContactsTv = (TextView) this.findViewById(R.id.id_contacts_tv);
		mTabDialTv = (TextView) this.findViewById(R.id.id_dial_tv);
		mTabSettingTv = (TextView) this.findViewById(R.id.id_settings_tv);

		mTabDialIb = (ImageButton) this.findViewById(R.id.id_imagebtn_dial);
		mTabContactIb = (ImageButton) this.findViewById(R.id.id_imagebtn_contacts);
		mTabSettingIb = (ImageButton) this.findViewById(R.id.id_imagebtn_setting);

		mPageVp = (ViewPager) this.findViewById(R.id.id_page_vp);
	}

	private void init() {
		mSettingFg = new SettingFragment();
		mContactsFg = new ContactsFragment();
		mDialFg = new DialFragment();
		
		mFragmentList.add(mDialFg);
		mFragmentList.add(mContactsFg);
		mFragmentList.add(mSettingFg);

		mFragmentAdapter = new FragmentAdapter(
				this.getSupportFragmentManager(), mFragmentList);
		mPageVp.setAdapter(mFragmentAdapter);
		mPageVp.setCurrentItem(0);
		
		mPageVp.setOnPageChangeListener(new OnPageChangeListener() {

			/**
			 * state滑动中的状态 有三种状态（0，1，2） 1：正在滑动 2：滑动完毕 0：什么都没做。
			 */
			@Override
			public void onPageScrollStateChanged(int state) {

			}

			@Override
			public void onPageSelected(int position) {
				resetTextView();
				resetButtonView();
				switch (position) {
				case 0:
					mTabDialIb.setImageResource(R.drawable.tab_dial_selected);
					mTabDialTv.setTextColor(Color.BLUE);
					break;
				case 1:
					mTabContactIb.setImageResource(R.drawable.tab_contact_selected);
					mTabContactsTv.setTextColor(Color.BLUE);
					break;
				case 2:
					mTabSettingIb.setImageResource(R.drawable.tab_setting_selected);
					mTabSettingTv.setTextColor(Color.BLUE);
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
		});

	}

	/**
	 * 重置颜色
	 */
	private void resetTextView() {
		mTabDialTv.setTextColor(Color.WHITE);
		mTabSettingTv.setTextColor(Color.WHITE);
		mTabContactsTv.setTextColor(Color.WHITE);
	}
	
	/**
	 * 重置背景图片
	 */
	private void resetButtonView(){
		mTabDialIb.setImageResource(R.drawable.tab_dial_normal);
		mTabContactIb.setImageResource(R.drawable.tab_contact_normal);
		mTabSettingIb.setImageResource(R.drawable.tab_setting_normal);
	}

}
