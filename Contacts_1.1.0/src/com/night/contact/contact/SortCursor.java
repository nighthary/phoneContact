package com.night.contact.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts.Data;
import android.text.TextUtils;

import com.night.contact.DAO.ContactDAO;
import com.night.contact.bean.SortEntry;
import com.night.contact.util.PinyinUtils;
import com.night.contact.util.UnicodeGBK2Alpha;

/**
 * 联系人查询cursor
 * 
 * @author NightHary
 * 
 */
@SuppressLint({ "DefaultLocale", "UseSparseArrays" })
public class SortCursor extends CursorWrapper {
	private ArrayList<SortEntry> mSortList;
	private ArrayList<SortEntry> mFilterList;// 筛选后的数据list
	private Cursor mCursor;
	private int mPos;
	private static Context m_context;
	private Map<String, SortEntry> contactIdMap;

	public static void init(Context context) {
		m_context = context;
	}

	public SortCursor(Cursor cursor) {
		super(cursor);
		System.out.println("SortCursor...");
		mCursor = cursor;
		mSortList = new ArrayList<SortEntry>();
		contactIdMap = new HashMap<String, SortEntry>();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			String raw_contact_id = cursor.getString(cursor
					.getColumnIndex(Data.RAW_CONTACT_ID));
			String name = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			if (contactIdMap.containsKey(raw_contact_id)) {
			} else {
				SortEntry entry = null;
				if (!contactIdMap.containsValue(name)) {
					entry = new SortEntry();
					entry.mID = raw_contact_id;
					entry.mName = name;

					entry.mOrder = cursor.getPosition();
					entry.mPY = PinyinUtils.getPingYin1(entry.mName);
					String number = "";
					int phoneCount = cursor
							.getInt(cursor
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (phoneCount > 0) {
						Cursor phoneCursor = m_context
								.getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID
												+ "=" + raw_contact_id,null,
										null);
						while (phoneCursor.moveToNext()) {
							String phoneNumber = phoneCursor
									.getString(phoneCursor
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							number += phoneNumber + "#";
						}
						phoneCursor.close();
					} else {
						number = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					entry.mNum = number;
					entry.mFisrtSpell = PinyinUtils.getFirstSpell(entry.mName);
					entry.mchoose = 0;
					entry.lookUpKey = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY));
					entry.photoId = cursor
							.getInt(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
					int groupId = new ContactDAO(m_context)
							.getGroupIdByContactId(Integer.parseInt(entry.mID));
					entry.groupId = groupId;
					mSortList.add(entry);
					System.out.println("id:"+raw_contact_id+"name:"+name+"number:"+number);
				}
				contactIdMap.put(entry.mID, entry);
			}
		}
		System.out.println("loaded...");
		Collections.sort(mSortList, new ComparatorPY());

	}

	private class ComparatorPY implements Comparator<SortEntry> {

		@Override
		public int compare(SortEntry lhs, SortEntry rhs) {
			String str1 = lhs.mPY;
			String str2 = rhs.mPY;
			return str1.compareToIgnoreCase(str2);
		}
	}

	@Override
	public boolean moveToPosition(int position) {
		mPos = position;
		if (position < mSortList.size() && position >= 0) {
			return mCursor.moveToPosition(mSortList.get(position).mOrder);
		}

		if (position < 0) {
			mPos = -1;
		}
		if (position >= mSortList.size()) {
			mPos = mSortList.size();
		}
		return mCursor.moveToPosition(position);
	}

	@Override
	public boolean moveToFirst() {
		return moveToPosition(0);
	}

	@Override
	public boolean moveToLast() {
		return moveToPosition(getCount() - 1);
	}

	@Override
	public boolean moveToNext() {
		return moveToPosition(mPos + 1);
	}

	@Override
	public boolean moveToPrevious() {
		return moveToPosition(mPos - 1);
	}

	public int binarySearch(String letter) {
		for (int index = 0; index < mSortList.size(); index++) {
			if (mSortList.get(index).mPY.substring(0, 1).compareToIgnoreCase(
					letter) == 0) {
				return index;
			}
		}
		return -1;
	}

	public ArrayList<SortEntry> GetContactsArray() {

		return mSortList;
	}

	/**
	 * 获取要添加的成员
	 * 
	 * @param groupId
	 *            添加到那一组
	 * @return
	 */
	public ArrayList<SortEntry> getAddContactsArray(int groupId) {
		ArrayList<SortEntry> addContactsList = new ArrayList<>();
		for (SortEntry entry : mSortList) {
			if (entry.groupId == 0) {
				addContactsList.add(entry);
			}
		}
		return addContactsList;
	}

	/**
	 * 获取要移除的成员
	 * 
	 * @param groupId
	 *            从某一组移除成员
	 * @return
	 */
	public ArrayList<SortEntry> getRemoveContactsArray(int groupId) {
		ArrayList<SortEntry> removeContactsList = new ArrayList<>();
		for (SortEntry entry : mSortList) {
			if (entry.groupId == groupId) {
				removeContactsList.add(entry);
			}
		}
		return removeContactsList;
	}

	/**
	 * 主界面搜索联系人
	 * 
	 * @param keyword
	 * @return
	 */
	public ArrayList<SortEntry> filterSearch(String keyword) {
		mFilterList = new ArrayList<SortEntry>();
		mFilterList.clear();

		if (keyword.startsWith("0") || keyword.startsWith("1")) {
			for (SortEntry contact : mSortList) {
				if (contact.mNum != null && contact.mName != null) {
					if (contact.mNum.contains(keyword)
							|| contact.mName.contains(keyword)) {
						mFilterList.add(contact);
					}
				}
			}
			return mFilterList;
		}

		final String result = PinyinUtils.getPingYin1(keyword);
		for (SortEntry contact : mSortList) {
			if (contains(contact, result)) {
				mFilterList.add(contact);
			}
		}
		return mFilterList;
	}

	/**
	 * 分组管理--添加/移除成员--搜索联系人
	 * 
	 * @param keyword
	 * @return
	 */
	public ArrayList<SortEntry> filterSearch1(String keyword,
			ArrayList<SortEntry> sortList) {
		mFilterList = new ArrayList<SortEntry>();
		mFilterList.clear();

		if (keyword.startsWith("0") || keyword.startsWith("1")) {
			for (SortEntry contact : sortList) {
				if (contact.mNum != null && contact.mName != null) {
					if (contact.mNum.contains(keyword)
							|| contact.mName.contains(keyword)) {
						mFilterList.add(contact);
					}
				}
			}
			return mFilterList;
		}

		final String result = PinyinUtils.getPingYin1(keyword);
		for (SortEntry contact : sortList) {
			if (contains(contact, result)) {
				mFilterList.add(contact);
			}
		}
		return mFilterList;
	}

	/**
	 * 根据拼音搜索
	 * 
	 * @param str
	 *            正则表达式
	 * @param pyName
	 *            拼音
	 * @param isIncludsive
	 *            搜索条件是否大于6个字符
	 * @return
	 */
	public static boolean contains(SortEntry contact, String search) {
		if (TextUtils.isEmpty(contact.mName) || TextUtils.isEmpty(search)) {
			return false;
		}
		boolean flag = false;

		// 简拼匹配，如果输入在字符串长度大于6就不按首字母匹配
		if (search.length() < 6) {
			String firstLetters = UnicodeGBK2Alpha
					.getSimpleCharsOfString(contact.mName);

			Pattern firstLetterMatcher = Pattern.compile("^" + search,
					Pattern.CASE_INSENSITIVE);
			flag = firstLetterMatcher.matcher(firstLetters).find();
		}

		if (!flag) {// 全拼
			Pattern pattern2 = Pattern
					.compile(search, Pattern.CASE_INSENSITIVE);
			Matcher matcher2 = pattern2.matcher(PinyinUtils
					.getPingYin1(contact.mName));
			flag = matcher2.find();
		}
		return flag;
	}

	public ArrayList<SortEntry> FilterSearch(String keyword) {
		mFilterList = new ArrayList<SortEntry>();
		mFilterList.clear();
		// 遍历mArrayList
		for (int i = 0; i < mSortList.size(); i++) {
			// 如果遍历到List包含所输入字符串
			if (mSortList.get(i).mNum.indexOf(keyword) > 0
					|| isStrInString(mSortList.get(i).mPY, keyword)
					|| mSortList.get(i).mName.contains(keyword)
					|| isStrInString(mSortList.get(i).mFisrtSpell, keyword)) {
				// 将遍历到的元素重新组成一个list

				SortEntry entry = new SortEntry();
				entry.mName = mSortList.get(i).mName;
				entry.mID = mSortList.get(i).mID;
				entry.mOrder = i;
				entry.mPY = mSortList.get(i).mPY;
				entry.mNum = mSortList.get(i).mNum;
				mFilterList.add(entry);
			}
		}
		return mFilterList;
	}

	public boolean isStrInString(String bigStr, String smallStr) {
		if (bigStr.toUpperCase().indexOf(smallStr.toUpperCase()) > -1) {
			return true;
		} else {
			return false;
		}
	}

	public String getName(int index) {
		return mSortList.get(index).mName;

	}

	public String getNumber(int index) {
		return mSortList.get(index).mNum;

	}

	public String getID(int index) {
		return mSortList.get(index).mID;
	}

	public String getLookUpKey(int index) {
		return mSortList.get(index).lookUpKey;
	}

	public int getPhotoId(int index) {
		return mSortList.get(index).photoId;
	}

	// 过滤群组
	public ArrayList<SortEntry> filterGroup(int groupId) {
		ArrayList<SortEntry> groupList = new ArrayList<SortEntry>();
		groupList.clear();
		for (SortEntry entry : mSortList) {
			if (entry.groupId == groupId) {
				groupList.add(entry);
			}
		}
		return groupList;
	}
}
