package com.night.contact.contact;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

/**
 * cursor¼ÓÔØÆ÷
 * 
 * @author NightHary
 *
 */
public class SortCursorLoader extends CursorLoader{

	public SortCursorLoader(Context context, Uri uri, String[] projection,
			String selection, String[] selectionArgs, String sortOrder) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public Cursor loadInBackground() {
		Cursor cursor = super.loadInBackground();
		return new SortCursor(cursor);
	}

}
