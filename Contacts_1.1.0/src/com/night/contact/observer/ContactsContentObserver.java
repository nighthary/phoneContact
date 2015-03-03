package com.night.contact.observer;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

/**
 * 联系热数据库观察者
 * 
 * @author NightHary
 *
 */
public class ContactsContentObserver extends ContentObserver{

	public ContactsContentObserver(Handler handler) {
		super(handler);
	}
	@Override
	public void onChange(boolean selfChange) {
		
		Log.i("datachanged", "联系人数据库发生了变化");
	}

}
