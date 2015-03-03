package com.night.contact.contact;


import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts.Data;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.night.contact.ui.R;
import com.night.contact.util.PinyinUtils;

/**
 * 联系人列表适配器
 * 
 * @author NightHary
 *
 */
public class ContactsCursorAdapter extends CursorAdapter{
	int ItemPos = -1;
	
	@SuppressWarnings("deprecation")
	public ContactsCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemPos = position;

		return super.getView(position,convertView,parent);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if(cursor == null)
		{
			return;
		}
        TextView name = (TextView) view.findViewById(R.id.contacts_name);
        name.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
	    
        ImageView photo = (ImageView) view.findViewById(R.id.contact_photo);
        
        int photoId = cursor.getInt
        		(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
        String contactID = cursor.getString(cursor
				.getColumnIndex(Data.RAW_CONTACT_ID));
        if(0 == photoId){
        	photo.setImageResource(R.drawable.default_contact_photo);
        }else{
        	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,Integer.parseInt(contactID));
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri); 
			Bitmap contactPhoto = BitmapFactory.decodeStream(input);
			photo.setImageBitmap(contactPhoto);
        }
        
        ImageView chooseView = (ImageView)view.findViewById(R.id.choose_contact);
        chooseView.setVisibility(View.GONE);
        
		//字母提示textview的显示 
		TextView letterTag = (TextView)view.findViewById(R.id.pb_item_LetterTag);
		//获得当前姓名的拼音首字母
		String firstLetter = PinyinUtils.getPingYin(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).substring(0,1).toUpperCase();
		
		//如果是第1个联系人 那么letterTag始终要显示
		if(ItemPos == 0)
		{
			letterTag.setVisibility(View.VISIBLE);
			letterTag.setText(firstLetter);
		}			
		else
		{
			//获得上一个姓名的拼音首字母
			cursor.moveToPrevious();
			String firstLetterPre = PinyinUtils.getPingYin(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).substring(0,1).toUpperCase();
			//比较一下两者是否相同
			if(firstLetter.equals(firstLetterPre))
			{
				letterTag.setVisibility(View.GONE);
			}
			else 
			{
				letterTag.setVisibility(View.VISIBLE);
				letterTag.setText(firstLetter);
			}
		}
		
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.list_item_home_contacts, parent,false);
	}
}
