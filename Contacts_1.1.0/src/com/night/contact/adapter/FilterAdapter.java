package com.night.contact.adapter;

import java.io.InputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.night.contact.bean.SortEntry;
import com.night.contact.ui.R;

/**
 * 联系人过滤适配器
 * @author NightHary
 *
 */
@SuppressLint({ "ViewHolder", "InflateParams" })
public  class FilterAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private ArrayList<SortEntry> data;
	private Context context;
	
    public FilterAdapter(Context context,
    		ArrayList<SortEntry> data) {
	    this.mInflater = LayoutInflater.from(context);
	    this.data = data;
	    this.context = context;
    }

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		arg1 = mInflater.inflate(R.layout.list_item_home_contacts, null);
		
		//姓名显示
		TextView nameCtrl = (TextView)arg1.findViewById(R.id.contacts_name);			
		String strName = data.get(arg0).mName;
		nameCtrl.setText(strName);
		
		//头像
		ImageView photo = (ImageView) arg1.findViewById(R.id.contact_photo);
		int photoId = data.get(arg0).photoId;
		String contactID = data.get(arg0).mID;
		if(0 == photoId){
			photo.setImageResource(R.drawable.default_contact_photo);
		}else{
        	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,Integer.parseInt(contactID));
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri); 
			Bitmap contactPhoto = BitmapFactory.decodeStream(input);
			photo.setImageBitmap(contactPhoto);
        }
		//是否选择
        ImageView chooseView = (ImageView)arg1.findViewById(R.id.choose_contact);
        chooseView.setVisibility(View.GONE);
		
		return arg1;
	}
}
