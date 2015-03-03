package com.night.contact.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.night.contact.ui.R;

/**
 * ²¦ºÅÖ÷½çÃæ
 * 
 * @author NightHary
 * 
 */
@SuppressLint("InflateParams")
public class DialFragment extends Fragment implements OnClickListener{

	// µ±Ç°FragmentµÄÖ÷view
	private View dialView;
	// Ìî³äÆ÷
	private LayoutInflater inflater;

	
	private Button dialNum1;
	private Button dialNum2;
	private Button dialNum3;
	private Button dialNum4;
	private Button dialNum5;
	private Button dialNum6;
	private Button dialNum7;
	private Button dialNum8;
	private Button dialNum9;
	private Button dialNum0;
	private Button keyboard_show;//*ºÅ
	private Button out_btn;//#ºÅ
	private Button phone_view;//ºÅÂë¿ò
	private Button delete;//Çå¿ÕºÅÂë
	private LinearLayout bohaopan;
	
	private String dialNumber="";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = getActivity().getLayoutInflater();
		dialView = inflater.inflate(R.layout.home_dial_page, null);
		
		findId(dialView);
		dialNum0.setOnClickListener(this);
		dialNum1.setOnClickListener(this);
		dialNum2.setOnClickListener(this);
		dialNum3.setOnClickListener(this);
		dialNum4.setOnClickListener(this);
		dialNum5.setOnClickListener(this);
		dialNum6.setOnClickListener(this);
		dialNum6.setOnClickListener(this);
		dialNum7.setOnClickListener(this);
		dialNum8.setOnClickListener(this);
		dialNum9.setOnClickListener(this);
		keyboard_show.setOnClickListener(this);
		out_btn.setOnClickListener(this);
		phone_view.setOnClickListener(this);
		delete.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup p = (ViewGroup) dialView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return dialView;
	}

	private void findId(View dialView){
		dialNum0 = (Button) dialView.findViewById(R.id.dialNum0);
		dialNum1 = (Button) dialView.findViewById(R.id.dialNum1);
		dialNum2 = (Button) dialView.findViewById(R.id.dialNum2);
		dialNum3 = (Button) dialView.findViewById(R.id.dialNum3);
		dialNum4 = (Button) dialView.findViewById(R.id.dialNum4);
		dialNum5 = (Button) dialView.findViewById(R.id.dialNum5);
		dialNum6 = (Button) dialView.findViewById(R.id.dialNum6);
		dialNum7 = (Button) dialView.findViewById(R.id.dialNum7);
		dialNum8 = (Button) dialView.findViewById(R.id.dialNum8);
		dialNum9 = (Button) dialView.findViewById(R.id.dialNum9);
		keyboard_show = (Button) dialView.findViewById(R.id.keyboard_show);
		out_btn = (Button) dialView.findViewById(R.id.out_btn);
		phone_view = (Button) dialView.findViewById(R.id.phone_view);
		delete = (Button) dialView.findViewById(R.id.delete);
		bohaopan = (LinearLayout) dialView.findViewById(R.id.bohaopan);
	}
	
	private static DialFragment instance = null;

	public DialFragment() {
	}

	public static DialFragment getInstance() {
		if (instance == null) {
			instance = new DialFragment();
		}
		return instance;
	}

	@Override
	public void onClick(View v) {
		dialNumber  = phone_view.getText().toString().trim();
		switch(v.getId()){
		case R.id.dialNum0:
			dialNumber += 0;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum1:
			dialNumber += 1;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum2:
			dialNumber += 2;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum3:
			dialNumber += 3;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum4:
			dialNumber += 4;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum5:
			dialNumber += 5;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum6:
			dialNumber += 6;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum7:
			dialNumber += 7;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum8:
			dialNumber += 8;
			phone_view.setText(dialNumber);
			break;
		case R.id.dialNum9:
			dialNumber += 9;
			phone_view.setText(dialNumber);
			break;
		case R.id.keyboard_show:
			bohaopan.setVisibility(View.INVISIBLE);
			break;
		case R.id.out_btn:
			bohaopan.setVisibility(View.VISIBLE);
			break;
		case R.id.phone_view:
			String number = phone_view.getText().toString().trim();
			Intent intent = new Intent(Intent.ACTION_CALL, Uri
					.parse("tel://" + number));
			startActivity(intent);
			break;
		case R.id.delete:
			
			int length = dialNumber.length();
			if(length>1){
				dialNumber = dialNumber.substring(0,length-1);
			}else{
				dialNumber = "";
			}
			phone_view.setText(dialNumber);
			break;
		}
	}
}
