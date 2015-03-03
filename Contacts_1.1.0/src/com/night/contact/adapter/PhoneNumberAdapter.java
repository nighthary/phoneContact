package com.night.contact.adapter;

import com.night.contact.ui.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

@SuppressLint({ "InflateParams", "ViewHolder" })
public class PhoneNumberAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private String[] numbers;
	@SuppressWarnings("unused")
	private Context context;
	public PhoneNumberAdapter(String[] numbers,Context context){
		this.mInflater = LayoutInflater.from(context);
		this.numbers = numbers;
		this.context = context;
	}
	@Override
	public int getCount() {
		return numbers.length;
	}

	@Override
	public Object getItem(int position) {
		return numbers[position-1];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.list_item_home_contacts,null);
		
		Button btn_contact_num = (Button) convertView.findViewById(R.id.btn_contact_detial_num);
//		ImageButton imagebtn_contact_detial_sms  = (ImageButton) convertView.findViewById(R.id.imagebtn_contact_detial_sms);
		System.out.println(position);
		String num = numbers[position];
		btn_contact_num.setText(num);
		return convertView;
	}
}
