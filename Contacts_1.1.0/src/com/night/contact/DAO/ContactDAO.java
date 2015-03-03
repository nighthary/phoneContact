package com.night.contact.DAO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.ContactStruct.PhoneData;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.widget.Toast;

import com.night.contact.bean.SortEntry;
import com.night.contact.util.ImageConvert;
import com.night.contact.util.PinyinUtils;
import com.night.contact.util.Tools;

/**
 * 联系人操作持久层
 * 
 * @author NightHary
 * 
 */
public class ContactDAO {

	private Context context;

	public ContactDAO() {
	}

	public ContactDAO(Context context) {
		this.context = context;
	}

	/**
	 * 添加联系人
	 * 
	 * @param contact
	 *            联系人实体对象
	 * @param groupId
	 *            组号
	 */
	public boolean addContact1(SortEntry contact, int groupId) {
		if (TextUtils.isEmpty(contact.mName)) {
			Toast.makeText(context, "姓名不能为空", Toast.LENGTH_LONG).show();
			return false;
		}
		ContentValues values = new ContentValues();
		Uri rawContactUri = context.getContentResolver().insert(
				RawContacts.CONTENT_URI, values);
		int rawContactId = (int) ContentUris.parseId(rawContactUri);
		// 向data表插入姓名
		if (contact.mName != "") {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
			values.put(StructuredName.GIVEN_NAME, contact.mName);
			context.getContentResolver().insert(
					ContactsContract.Data.CONTENT_URI,values);
		}

		// 向data中插入电话号码
		if (contact.mNum != "") {
			values.clear();
			String[] numbers = Tools.getPhoneNumber(contact.mNum);
			for (int i = 0; i < numbers.length; i++) {
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, numbers[i]);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
				context.getContentResolver().insert(
						ContactsContract.Data.CONTENT_URI, values);
			}
		}

		// 添加头像
		if (contact.contactPhoto != null) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
			values.put(Photo.PHOTO,
					ImageConvert.bitmapToByte(contact.contactPhoto));
			context.getContentResolver().insert(
					ContactsContract.Data.CONTENT_URI, values);
		}
		// // 添加地址
		// if (contact.getAddress() != "") {
		// values.clear();
		// values.put(Data.RAW_CONTACT_ID, rawContactId);
		// values.put(Data.MIMETYPE, SipAddress.CONTENT_ITEM_TYPE);
		// values.put(SipAddress.CONTENT_ITEM_TYPE, contact.getAddress());
		// context.getContentResolver().insert(
		// ContactsContract.Data.CONTENT_URI, values);
		// }
		// // 添加邮箱
		// if (contact.getEmail() != "") {
		// values.clear();
		// values.put(Data.RAW_CONTACT_ID, rawContactId);
		// values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
		// values.put(Email.CONTENT_ITEM_TYPE, contact.getEmail());
		// context.getContentResolver().insert(
		// ContactsContract.Data.CONTENT_URI, values);
		// }
		if (groupId != 0) {
			new GroupDAO(context).addMemberToGroup(rawContactId, groupId);
		}
		return true;
	}

	/**
	 * 删除联系人
	 * 
	 * @param rawContactId
	 *            联系人id
	 */
	public void deleteContact(int rawContactId) {
		context.getContentResolver().delete(
				ContentUris.withAppendedId(RawContacts.CONTENT_URI,
						rawContactId), null, null);
	}

	/**
	 * 更新联系人
	 * 
	 * @param rawContactId
	 *            联系人id
	 */
	public void updataCotact(long rawContactId, SortEntry contact,
			int old_groupID) {
		ContentValues values = new ContentValues();
		// 更新姓名
		values.put(StructuredName.GIVEN_NAME, contact.mName);
		context.getContentResolver().update(
				ContactsContract.Data.CONTENT_URI,
				values,
				Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
				new String[] { String.valueOf(rawContactId),
						StructuredName.CONTENT_ITEM_TYPE });

		// 更新电话
		if (contact.mNum != "") {
			values.clear();
			String[] numbers = Tools.getPhoneNumber(contact.mNum);
			for (int i = 0; i < numbers.length; i++) {
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, numbers[i]);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
//				context.getContentResolver().insert(
//						ContactsContract.Data.CONTENT_URI, values);
				context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
						values,
						Data.RAW_CONTACT_ID+"=? and "+Data.MIMETYPE+"=?",
						new String[]{String.valueOf(rawContactId),Phone.CONTENT_ITEM_TYPE});
			}
		}
		// 更新头像
		values.clear();
		if (contact.contactPhoto != null) {
			values.put(ContactsContract.CommonDataKinds.Photo.PHOTO,
					ImageConvert.bitmapToByte(contact.contactPhoto));
			context.getContentResolver().update(
					ContactsContract.Data.CONTENT_URI,
					values,
					Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + " =?",
					new String[] { String.valueOf(rawContactId),
							Photo.CONTENT_ITEM_TYPE });
		}
		// 更新群组
		if (contact.groupId != 0) {
			values.clear();
			values.put(
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
					contact.groupId);
			context.getContentResolver().update(
					ContactsContract.Data.CONTENT_URI,
					values,
					Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + " =?",
					new String[] { String.valueOf(rawContactId),
							GroupMembership.CONTENT_ITEM_TYPE });
		} else {
			new GroupDAO(context).deleteMemberFromGroup(
					Integer.parseInt(contact.mID), old_groupID);
		}
	}

	/**
	 * 更新联系人 开始没有头像的联系人
	 * 
	 * @param rawContactId
	 *            联系人id
	 */
	public void updataCotactNoPhoto(long rawContactId, SortEntry contact,
			int old_groupID) {
		ContentValues values = new ContentValues();
		// 更新姓名
		values.put(StructuredName.GIVEN_NAME, contact.mName);
		context.getContentResolver().update(
				ContactsContract.Data.CONTENT_URI,
				values,
				Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
				new String[] { String.valueOf(rawContactId),
						StructuredName.CONTENT_ITEM_TYPE });

		// 更新电话
		if (contact.mNum != "") {
			values.clear();
			String[] numbers = Tools.getPhoneNumber(contact.mNum);
			for (int i = 0; i < numbers.length; i++) {
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, numbers[i]);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
				context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
						values,
						Data.RAW_CONTACT_ID+"=? and "+Data.MIMETYPE+"=?",
						new String[]{String.valueOf(rawContactId),Phone.CONTENT_ITEM_TYPE});
			}
		}
		// 添加头像
		if (contact.contactPhoto != null) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
			values.put(Photo.PHOTO,
					ImageConvert.bitmapToByte(contact.contactPhoto));
			context.getContentResolver().insert(
					ContactsContract.Data.CONTENT_URI, values);
		}
		// 更新群组
		if (contact.groupId != 0) {
			values.clear();
			values.put(
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
					contact.groupId);
			context.getContentResolver().update(
					ContactsContract.Data.CONTENT_URI,
					values,
					Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + " =?",
					new String[] { String.valueOf(rawContactId),
							GroupMembership.CONTENT_ITEM_TYPE });
		} else {
			new GroupDAO(context).deleteMemberFromGroup(
					Integer.parseInt(contact.mID), old_groupID);
		}
	}

	/**
	 * 更新联系人 更新前没有群组的联系人
	 * 
	 * @param rawContactId
	 *            联系人id
	 */
	public void updataCotactNoGroup(long rawContactId, SortEntry contact) {
		ContentValues values = new ContentValues();
		// 更新姓名
		values.put(StructuredName.GIVEN_NAME, contact.mName);
		context.getContentResolver().update(
				ContactsContract.Data.CONTENT_URI,
				values,
				Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
				new String[] { String.valueOf(rawContactId),
						StructuredName.CONTENT_ITEM_TYPE });

		// 更新电话
		if (contact.mNum != "") {
			values.clear();
			String[] numbers = Tools.getPhoneNumber(contact.mNum);
			for (int i = 0; i < numbers.length; i++) {
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, numbers[i]);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
				context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
						values,
						Data.RAW_CONTACT_ID+"=? and "+Data.MIMETYPE+"=?",
						new String[]{String.valueOf(rawContactId),Phone.CONTENT_ITEM_TYPE});
			}
		}
		// 更新头像
		if (contact.contactPhoto != null) {
			values.clear();
			values.put(ContactsContract.CommonDataKinds.Photo.PHOTO,
					ImageConvert.bitmapToByte(contact.contactPhoto));
			context.getContentResolver().update(
					ContactsContract.Data.CONTENT_URI,
					values,
					Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + " =?",
					new String[] { String.valueOf(rawContactId),
							Photo.CONTENT_ITEM_TYPE });
		}
		// 添加到群组
		if (contact.groupId != 0) {
			new GroupDAO(context).addMemberToGroup(
					Integer.parseInt(contact.mID), contact.groupId);
		}
	}

	/**
	 * 更新联系人 更新前没有头像、群组的联系人
	 * 
	 * @param rawContactId
	 *            联系人id
	 */
	public void updataCotactNoG_Photo(long rawContactId, SortEntry contact) {
		ContentValues values = new ContentValues();
		// 更新姓名
		values.put(StructuredName.GIVEN_NAME, contact.mName);
		context.getContentResolver().update(
				ContactsContract.Data.CONTENT_URI,
				values,
				Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
				new String[] { String.valueOf(rawContactId),
						StructuredName.CONTENT_ITEM_TYPE });

		// 更新电话
		if (contact.mNum != "") {
			values.clear();
			String[] numbers = Tools.getPhoneNumber(contact.mNum);
			for (int i = 0; i < numbers.length; i++) {
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, numbers[i]);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
				context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
						values,
						Data.RAW_CONTACT_ID+"=? and "+Data.MIMETYPE+"=?",
						new String[]{String.valueOf(rawContactId),Phone.CONTENT_ITEM_TYPE});
			}
		}
		// 添加头像
		if (contact.contactPhoto != null) {
			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
			values.put(Photo.PHOTO,
					ImageConvert.bitmapToByte(contact.contactPhoto));
			context.getContentResolver().insert(
					ContactsContract.Data.CONTENT_URI, values);
		}
		// 添加到群组
		if (contact.groupId != 0) {
			new GroupDAO(context).addMemberToGroup(
					Integer.parseInt(contact.mID), contact.groupId);
		}
	}

	/**
	 * 更新联系人所在分组
	 * 
	 * @param groupID
	 *            当前联系人所在群组
	 * @param contactID
	 *            联系人ID
	 */
	public void updateContactFromGroup(int groupID, String contactID) {
		ContentValues values = new ContentValues();
		// 更新群组
		values.clear();
		values.put(
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
				groupID);
		context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
				values,
				Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + " =?",
				new String[] { contactID, GroupMembership.CONTENT_ITEM_TYPE });
	}

	Map<Integer, SortEntry> contactIdMap;
	List<SortEntry> contacts;

	/**
	 * 返回所有联系人 联系人id、联系人姓名、联系人号码、联系人头像 去除了重复的联系人
	 */
	@SuppressLint("UseSparseArrays")
	public List<SortEntry> getContacts() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY, }; // 查询的列
		Cursor cursor = context.getContentResolver().query(uri, projection,
				null, null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询

		if (cursor != null && cursor.getCount() > 0) {
			contactIdMap = new HashMap<Integer, SortEntry>();

			contacts = new ArrayList<SortEntry>();
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String name = cursor.getString(1);
				String number = cursor.getString(2);
				int contactId = cursor.getInt(3);
				int photoId = cursor.getInt(4);
				String lookUpKey = cursor.getString(5);
				if (contactIdMap.containsKey(contactId)) {

				} else {
					SortEntry entry = null;
					if (!contactIdMap.containsValue(name)) {
						entry = new SortEntry();
						entry.mName = name;
						entry.mNum = number;
						entry.mID = String.valueOf(contactId);
						entry.photoId = photoId;
						entry.lookUpKey = lookUpKey;

						int groupId = new ContactDAO(context)
								.getGroupIdByContactId(contactId);
						entry.groupId = groupId;
						contacts.add(entry);
					}
					contactIdMap.put(contactId, entry);
				}
			}
			cursor.close();
		}
		return contacts;
	}

	/**
	 * 获取某个分组下联系人信息
	 * 
	 * @param personId
	 * @param context
	 * @return 该分组下的所有联系人
	 */
	public ArrayList<SortEntry> getContactsByGroupId(int groupId) {
		Uri uri = ContactsContract.Data.CONTENT_URI;

		String[] RAW_PROJECTION = new String[] { ContactsContract.Data.RAW_CONTACT_ID, };
		String RAW_CONTACTS_WHERE = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
				+ " =? and "
				+ ContactsContract.Data.MIMETYPE
				+ " = '"
				+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
				+ "'";
		Cursor cursor = context.getContentResolver().query(uri, RAW_PROJECTION,
				RAW_CONTACTS_WHERE, new String[] { groupId + "" }, "data1 asc");

		ArrayList<SortEntry> list = new ArrayList<SortEntry>();
		while (cursor.moveToNext()) {
			// RAW_CONTACTS_ID
			int raw_contact_id = cursor.getInt(cursor
					.getColumnIndex("raw_contact_id"));

			SortEntry entry = new SortEntry();
			entry.mID = String.valueOf(raw_contact_id);
			entry = getContactByID(raw_contact_id, entry);
			list.add(entry);
			entry = null;
		}
		cursor.close();
		return list;
	}

	/**
	 * 根据联系人ID查找具体信息
	 * 
	 * @param contactID
	 *            联系人ID
	 */
	public SortEntry getContactByID(int contactID, SortEntry entry) {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri

		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY, }; // 查询的列

		Cursor cursor = context.getContentResolver().query(uri, projection,
				Phone.RAW_CONTACT_ID + "=?", new String[] { contactID + "" },
				"data1 asc");
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			String name = cursor.getString(1);
			String number = cursor.getString(2);
			int photoId = cursor.getInt(3);
			String lookUpKey = cursor.getString(4);

			entry.mName = name;
			entry.mNum = number;
			entry.photoId = (int) photoId;
			entry.lookUpKey = lookUpKey;

		}
		cursor.close();
		return entry;
	}

	/**
	 * 根据联系人ID获取联系人所在群组
	 * 
	 * @param contactId
	 *            联系人ID
	 * @return
	 */
	public int getGroupIdByContactId(int contactId) {
		Uri uri = ContactsContract.Data.CONTENT_URI;

		String[] RAW_PROJECTION = new String[] { Data.MIMETYPE, Data.DATA1 };
		Cursor cursor = context.getContentResolver().query(uri, RAW_PROJECTION,
				Data.RAW_CONTACT_ID + "=?", new String[] { contactId + "" },
				null);
		int groupId = 0;
		while (cursor.moveToNext()) {
			String mime = cursor.getString(cursor.getColumnIndex("mimetype"));

			if ("vnd.android.cursor.item/group_membership".equals(mime)) {
				groupId = cursor.getInt(cursor.getColumnIndex("data1"));
			}
		}
		cursor.close();
		return groupId;
	}

	/**
	 * 获取vCard文件中的联系人信息
	 * 
	 * @return
	 */
	public List<SortEntry> restoreContacts() throws Exception {
		List<SortEntry> contactInfoList = new ArrayList<SortEntry>();

		VCardParser parse = new VCardParser();
		VDataBuilder builder = new VDataBuilder();
		String file = Environment.getExternalStorageDirectory()
				+ "/example.vcf";

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));

		String vcardString = "";
		String line;
		while ((line = reader.readLine()) != null) {
			vcardString += line + "\n";
		}
		reader.close();

		boolean parsed = parse.parse(vcardString, "UTF-8", builder);

		if (!parsed) {
			throw new VCardException("Could not parse vCard file: " + file);
		}

		List<VNode> pimContacts = builder.vNodeList;
		for (VNode contact : pimContacts) {
			SortEntry entry = new SortEntry();
			ContactStruct contactStruct = ContactStruct
					.constructContactFromVNode(contact, 1);
			// 获取备份文件中的联系人电话信息
			List<PhoneData> phoneDataList = contactStruct.phoneList;
			// String[] numbers = new String[phoneDataList.size()];
			String numbers = "";
			for (int i = 0; i < phoneDataList.size(); i++) {
				// numbers[i] = phoneDataList.get(i).data;
				numbers += phoneDataList.get(i).data + "#";
			}
			entry.mNum = numbers;
			entry.mName = contactStruct.name;
			contactInfoList.add(entry);
		}

		return contactInfoList;
	}

	/**
	 * 返回所有联系人 联系人id、联系人姓名、联系人号码、联系人头像 去除了重复的联系人
	 */
	@SuppressLint("UseSparseArrays")
	public List<SortEntry> getContacts1() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY, }; // 查询的列
		Cursor cursor = context.getContentResolver().query(uri, projection,
				null, null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	
		if (cursor != null && cursor.getCount() > 0) {
			contactIdMap = new HashMap<Integer, SortEntry>();
	
			contacts = new ArrayList<SortEntry>();
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String name = cursor.getString(1);
				String number = cursor.getString(2);
				int contactId = cursor.getInt(3);
				int photoId = cursor.getInt(4);
				String lookUpKey = cursor.getString(5);
				if (contactIdMap.containsKey(contactId)) {
	
				} else {
					SortEntry entry = null;
					if (!contactIdMap.containsValue(name)) {
						entry = new SortEntry();
						entry.mName = name;
						entry.mNum = number;
						entry.mPY = PinyinUtils.getPingYin1(entry.mName);
						entry.mID = String.valueOf(contactId);
						entry.photoId = photoId;
						entry.lookUpKey = lookUpKey;
	
						contacts.add(entry);
					}
					contactIdMap.put(contactId, entry);
				}
			}
			cursor.close();
		}
		return contacts;
	}
}
