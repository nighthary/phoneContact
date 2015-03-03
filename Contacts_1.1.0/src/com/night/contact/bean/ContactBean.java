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
public class ContactBean implements Parcelable {

	private int rawcontactId;
	private int contactId;
	private String displayName;// 姓名
	private String phoneNum;// 电话
	private Long photoId;// 照片ID
	private String sortKey;// 排序
	private String lookUpKey;// 联系人信息
	private Bitmap contactPhoto;// 照片
	private String photoUri;// 头像路径
	private String address;// 地址
	private String email;// 邮箱
	private int _order;// 位置
	private String contactPY;// 联系人的拼音
	private String contactFirstSpell;// 首字母
	private int groupId;

	public int getRawcontactId() {
		return rawcontactId;
	}

	public void setRawcontactId(int rawcontactId) {
		this.rawcontactId = rawcontactId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getContactPY() {
		return contactPY;
	}

	public void setContactPY(String contactPY) {
		this.contactPY = contactPY;
	}

	public String getContactFirstSpell() {
		return contactFirstSpell;
	}

	public void setContactFirstSpell(String contactFirstSpell) {
		this.contactFirstSpell = contactFirstSpell;
	}

	public int get_order() {
		return _order;
	}

	public void set_order(int _order) {
		this._order = _order;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(String photoUri) {
		this.photoUri = photoUri;
	}

	public int getContactId() {
		return contactId;
	}

	public void setContactId(int contactId) {
		this.contactId = contactId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getLookUpKey() {
		return lookUpKey;
	}

	public void setLookUpKey(String lookUpKey) {
		this.lookUpKey = lookUpKey;
	}

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public Bitmap getContactPhoto() {
		return contactPhoto;
	}

	public void setContactPhoto(Bitmap contactPhoto) {
		this.contactPhoto = contactPhoto;
	}

	public String getSortKey() {
		return sortKey;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(contactId);
		dest.writeString(displayName);
		dest.writeString(phoneNum);
		dest.writeInt(groupId);
		dest.writeLong(photoId);
		dest.writeString(address);
		dest.writeString(email);
		dest.writeInt(rawcontactId);
	}

	// 重写Creator
	public static final Parcelable.Creator<ContactBean> CREATOR = new Creator<ContactBean>() {

		@Override
		public ContactBean createFromParcel(Parcel source) {
			ContactBean contact = new ContactBean();
			contact.contactId = source.readInt();
			contact.displayName = source.readString();
			contact.phoneNum = source.readString();
			contact.groupId = source.readInt();
			contact.photoId = source.readLong();
			contact.address = source.readString();
			contact.email = source.readString();
			contact.rawcontactId = source.readInt();
			return contact;
		}

		@Override
		public ContactBean[] newArray(int size) {
			return new ContactBean[size];
		}

	};
}
