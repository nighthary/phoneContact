package com.night.contact.DAO;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;

import com.night.contact.bean.GroupBean;

/**
 * 群组操作持久层
 * 
 * @author NightHary
 *
 */
public class GroupDAO {

	private Context context;

	public GroupDAO(Context context) {
		this.context = context;
	}

	/**
	 * 获取所有联系人分组
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	public ArrayList<GroupBean> getGroups() {
		Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI,
				null, null, null, null);
		ArrayList<GroupBean> list = new ArrayList<GroupBean>();

		while (cursor.moveToNext()) {
			// 得到状态--是否被删除
			int isDeleted = cursor
					.getInt(cursor.getColumnIndex(Groups.DELETED));
			if (isDeleted == 0) {
				GroupBean gb = new GroupBean();
				// 添加组名
				String name = cursor.getString(cursor
						.getColumnIndex(Groups.TITLE));
				gb.setName(name);
				// 添加组id
				int groupId = cursor.getInt(cursor.getColumnIndex(Groups._ID));
				gb.setId(groupId);
				int count = new ContactDAO(context).getContactsByGroupId(
						groupId).size();
				gb.setCount(count);
				list.add(gb);
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * 新建组
	 * 
	 * @param groupName
	 *            组名
	 * @param context
	 *            上下文
	 */
	public void addGroup(String groupName) {
		ContentValues values = new ContentValues();
		values.put(Groups.TITLE, groupName);
		context.getContentResolver().insert(Groups.CONTENT_URI, values);
	}

	/**
	 * 删除组
	 * 
	 * @param groupId
	 *            组id
	 * @param context
	 *            上下文
	 */
	public void deleteGroup(int groupId) {
		context.getContentResolver().delete(
				Uri.parse(Groups.CONTENT_URI + "?"
						+ ContactsContract.CALLER_IS_SYNCADAPTER + "=true"),
				Groups._ID + "=" + groupId, null);
	}

	/**
	 * 添加成员
	 * 
	 * @param groupId
	 *            联系人id
	 * @param groupId
	 *            组号
	 * @param context
	 *            上下文
	 */
	public void addMemberToGroup(int personId, int groupId) {
		ContentValues values = new ContentValues();
		values.put(
				ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID,
				personId);

		values.put(
				ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
				groupId);

		values.put(
				ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE,
				ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);

		context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI,
				values);
	}

	/**
	 * 从组中删除成员
	 * 
	 * @param personId
	 *            成员id
	 * @param groupId
	 *            组号
	 * @param context
	 *            上下文
	 */
	public void deleteMemberFromGroup(int personId, int groupId) {
		context
				.getContentResolver()
				.delete(ContactsContract.Data.CONTENT_URI,
						ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID
								+ "=? and "
								+ ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
								+ "=? and "
								+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
								+ "=?",
						new String[] {
								"" + personId,
								"" + groupId,
								ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE });
		// Delete a contact from a group
//		Uri uri = Data.CONTENT_URI;
//		Uri.Builder b = uri.buildUpon();
//		b.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true");
//		uri = b.build();
//
//		context.getContentResolver()
//				.delete(uri,
//						Data.RAW_CONTACT_ID
//								+ "="
//								+ personId
//								+ " AND "
//								+ ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
//								+ " =" + groupId, null);
	}

	/**
	 * 修改群组名
	 * 
	 * @param rawContactId
	 */
	public void updataGroup(int groupId, String groupName) {
		System.out.println("update group...");
		ContentValues values = new ContentValues();
		values.put(Groups.TITLE, groupName);
		String where = ContactsContract.Groups._ID + "=? ";
		String[] selectionArgs = new String[] { String.valueOf(groupId) };
		context.getContentResolver().update(Groups.CONTENT_URI, values, where,
				selectionArgs);
	}

	public String getGroupNameByGroupId(long groupId) {
		String groupName = "";
		String[] PROJECTION = new String[] { Groups.TITLE };
		String SELECTION = Groups._ID + "=?";
		Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI,
				PROJECTION, SELECTION, new String[] { groupId + "" }, null);
		while (cursor.moveToNext()) {
			groupName = cursor.getString(cursor.getColumnIndex(Groups.TITLE));
		}
		cursor.close();
		return groupName;
	}

	public int getIdByGroupName(String groupName) {
		int groupId = 0;
		String[] PROJECTION = new String[] { Groups._ID };
		String SELECTION = Groups.TITLE + "=?";
		Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI,
				PROJECTION, SELECTION, new String[] { groupName + "" }, null);
		while (cursor.moveToNext()) {
			groupId = cursor.getInt(cursor.getColumnIndex(Groups._ID));
		}
		cursor.close();
		return groupId;
	}
}
