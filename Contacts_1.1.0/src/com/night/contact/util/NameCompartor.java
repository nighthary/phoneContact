package com.night.contact.util;

import java.util.Comparator;

import com.night.contact.bean.SortEntry;

/**
 * 姓名比较器
 * 	对联系人按姓名进行排序
 * @author NightHary
 *
 */
public class NameCompartor implements Comparator<SortEntry>{

	@Override
	public int compare(SortEntry lhs, SortEntry rhs) {
		String str1 = lhs.mPY;
		String str2 = rhs.mPY;
		return str1.compareToIgnoreCase(str2);
	}
}
