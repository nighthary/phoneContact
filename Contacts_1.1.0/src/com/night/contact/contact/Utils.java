package com.night.contact.contact;

import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Data;
import android.util.Log;

import com.night.contact.bean.SortEntry;

/**
 * database操作类
 * @deprecated
 * @author NightHary
 *
 */
public class Utils {
	public static Context m_context;
	//所有联系人的数据list
	public static ArrayList<SortEntry> mPersons = new ArrayList<SortEntry>();
	
	//初始化传入主Activity的上下文
	public static void init(Context context)
	{
		m_context = context;
	}

	//往数据库中新增联系人
	public static void addContact(String name, String number)
	{
		ContentValues values = new ContentValues(); 
        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId  
        Uri rawContactUri = m_context.getContentResolver().insert(RawContacts.CONTENT_URI, values); 
        long rawContactId = ContentUris.parseId(rawContactUri); 
        //往data表插入姓名数据 
        values.clear(); 
        values.put(Data.RAW_CONTACT_ID, rawContactId);  
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);//内容类型 
        values.put(StructuredName.GIVEN_NAME, name); 
        m_context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
       
        //往data表插入电话数据 
        values.clear(); 
        values.put(Data.RAW_CONTACT_ID, rawContactId); 
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE); 
        values.put(Phone.NUMBER, number); 
        values.put(Phone.TYPE, Phone.TYPE_MOBILE); 
        m_context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values); 
        
	}
	
	//更改数据库中联系人
	public static void changeContact(String name, String number, String ContactId)
	{
		Log.i("huahua", name);
		ContentValues values = new ContentValues();
		// 更新姓名
        values.put(StructuredName.GIVEN_NAME, name);
        m_context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
                        values,
                        Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE  + "=?",
                        new String[] { ContactId,StructuredName.CONTENT_ITEM_TYPE });
		
		//更新电话
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
        m_context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
				values, 
				Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE  + "=?",
				new String[] { ContactId,Phone.CONTENT_ITEM_TYPE});
	}
	
	//删除联系人
	public static void deleteContact(String ContactId)
	{
		m_context.getContentResolver().delete(
				ContentUris.withAppendedId(RawContacts.CONTENT_URI,
						Integer.parseInt(ContactId)), null, null);
	}
}
