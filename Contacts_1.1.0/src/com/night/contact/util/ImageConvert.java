package com.night.contact.util;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;

public class ImageConvert {
	
	
	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	public Bitmap getImageToView(Context context,Intent data,ImageButton btn_img) {
		Bundle extras = data.getExtras();
		Bitmap photo  = null;
		if (extras != null) {
			photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(context.getResources(),photo);
			btn_img.setImageDrawable(drawable);
//			//保存头像到系统头像目录
//			boolean a = saveImage(photo,contactId);
//			System.out.println(a);
		}
		return photo;
	}
	
//	private boolean saveImage(Bitmap photo,int contactId){
//		try {
//			File file = new File(context.getApplicationContext().getFilesDir().getPath(), String.valueOf(contactId));
//			FileOutputStream fos = new FileOutputStream(file);
//			fos.write(convetToByte(photo));
//			fos.close();
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
	
	/**
	 * 把图片转化成byte
	 * @param bitmap
	 * @return
	 */
	public static byte[] bitmapToByte(Bitmap bitmap){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
		return baos.toByteArray();
	}
	
	/**
	 * byte转化成bitmap
	 * @param bbyte数组
	 * @return
	 */
	public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
