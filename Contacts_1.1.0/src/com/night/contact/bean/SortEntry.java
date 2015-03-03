package com.night.contact.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
/**
 * 联系人实体
 * 
 * @author NightHary
 *
 */
public class SortEntry implements Parcelable{
	public String mID; // 在数据库中的ID号
	public String mName; // 姓名
	public String mPY; // 姓名拼音
	public String mNum; // 电话号码
	public String mFisrtSpell; // 中文名首字母 例:张雪冰:zxb
	public int mchoose; // 是否选中 0--未选中 1---选中
	public int mOrder; // 在原Cursor中的位置
	public String lookUpKey;
	public int photoId;
	public int groupId;
	public String groupName;
	public Bitmap contactPhoto;// 照片
	public String formattedNumber;
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mID);
		dest.writeString(mName);
		dest.writeString(mPY);
		dest.writeString(mNum);
		dest.writeString(mFisrtSpell);
		dest.writeInt(mchoose);
		dest.writeInt(mOrder);
		dest.writeString(lookUpKey);
		dest.writeInt(photoId);
		dest.writeInt(groupId);
		dest.writeString(groupName);
		dest.writeString(formattedNumber);
	}
	

	// 重写Creator
	public static final Parcelable.Creator<SortEntry> CREATOR = new Creator<SortEntry>() {

		@Override
		public SortEntry createFromParcel(Parcel source) {
			SortEntry contact = new SortEntry();
			contact.mID = source.readString();
			contact.mName = source.readString();
			contact.mPY = source.readString();
			contact.mNum = source.readString();
			contact.mFisrtSpell = source.readString();
			contact.mchoose = source.readInt();
			contact.mOrder = source.readInt();
			contact.lookUpKey = source.readString();
			contact.photoId = source.readInt();
			contact.groupId = source.readInt();
			contact.groupName = source.readString();
			contact.formattedNumber = source.readString();
			return contact;
		}

		@Override
		public SortEntry[] newArray(int size) {
			return new SortEntry[size];
		}

	};
}
