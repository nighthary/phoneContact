package com.night.contact.util;

/**
 * 用于保存一些常用的参数，比如在使用intent传递数据时的key
 * 
 * @author NightHary
 * 
 * @param GROUP_LIST  由ContactFragment向GroupActivity传递的群组数组
 * 
 * @param ADD_CONTACT 由新增联系人向MainActivity传递的一个Flag
 * 
 * @param CONTACT_DETIAL_KEY  查看联系人时传递的联系人信息
 * 
 * @param  CONTACT_DETIAL_TYPE 参数的来源
 */
public class Parameter {
	
	public static String GROUP_LIST = "groupList";
	
	public static String ADD_CONTACT = "addContact";
	
	public static String CONTACT_DETIAL_KEY = "contact_detial_key";
	
	public static String CONTACT_DETIAL_TYPE = "contact_detial_type";
	
	public static String CONTACT_EDIT_KEY = "contact_edit_back_key";
	
	public static String CONTACT_EDIT_TYPE = "contact_edit_back_type";
	
	public static String ADD_CONTACT_TITLE = "新增联系人";
	
	public static String EDIT_CONTACT_TITLE = "编辑联系人";
}
