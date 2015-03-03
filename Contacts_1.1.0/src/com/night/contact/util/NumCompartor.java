package com.night.contact.util;

import java.util.Comparator;

import com.night.contact.bean.SortEntry;

public class NumCompartor implements Comparator<SortEntry> {

	@Override
	public int compare(SortEntry lhs, SortEntry rhs) {
		
		Long num1 = formatNumber(lhs.mNum);
		Long num2 = formatNumber(rhs.mNum);
		return num1.compareTo(num2);
	}

	/**
	 * 格式化电话号码
	 * @param number
	 * @return
	 */
	private Long formatNumber(String number){
		String formatNumber = "";
		Long num = null;
		if(number == null){
			throw new NullPointerException("电话号码为空");
		}
		
		String temp = number.replace(" ","");
		try {
			if(number.contains("+86")){
				formatNumber = temp.substring(3,number.length()-1);
				num = Long.parseLong(formatNumber);
			}else{
				num = Long.parseLong(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}
}
