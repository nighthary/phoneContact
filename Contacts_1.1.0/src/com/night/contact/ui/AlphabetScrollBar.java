package com.night.contact.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class AlphabetScrollBar extends View {

	private Paint mPaint = new Paint();
	private String[] mAlphabet = new String[] {
		"#","A", "B", "C", "D", "E", "F", "G","H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
		"R", "S", "T", "U", "V", "W", "X", "Y", "Z"
	};
	private boolean mPressed;
	private int mCurPosIdx = -1;
	private int mOldPosIdx = -1;
	private OnTouchBarListener mTouchListener;
	private TextView LetterNotice;

	public AlphabetScrollBar(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	public AlphabetScrollBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlphabetScrollBar(Context context) {
		super(context);
	}
	
	public void setTextView(TextView LetterNotice) {
		this.LetterNotice = LetterNotice;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = this.getWidth();
		int height = this.getHeight();
		
		int singleLetterH = height/mAlphabet.length;
		
		if(mPressed) {
			//如果处于按下状态，改变背景及相应字体的颜色
			canvas.drawColor(Color.parseColor("#40000000"));
		}
		
		for(int i=0; i<mAlphabet.length; i++) {
			mPaint.setColor(Color.parseColor("#000000"));
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(23);
			
			float x = width/2 - mPaint.measureText(mAlphabet[i])/2;
			float y = singleLetterH*i+singleLetterH;
			
			if(i == mCurPosIdx)
			{
				mPaint.setColor(Color.parseColor("#0000FF"));
				mPaint.setFakeBoldText(true);
			}
			canvas.drawText(mAlphabet[i], x, y, mPaint);
			mPaint.reset();
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {

		int action = arg0.getAction();
		switch(action) {
		case MotionEvent.ACTION_DOWN:
			mPressed = true;
			mCurPosIdx =(int)( arg0.getY()/this.getHeight() * mAlphabet.length);
			if(mTouchListener != null && mOldPosIdx!=mCurPosIdx){
				if((mCurPosIdx>=0) && (mCurPosIdx<mAlphabet.length)) {
					mTouchListener.onTouch(mAlphabet[mCurPosIdx]);
					this.invalidate();
				}
				mOldPosIdx = mCurPosIdx;
			}
			
			LetterNotice.setText(mAlphabet[mCurPosIdx]);
			LetterNotice.setVisibility(View.VISIBLE);
			
			return true;
		case MotionEvent.ACTION_UP:
			
			if (LetterNotice != null) {
				LetterNotice.setVisibility(View.INVISIBLE);
			}
			
			mPressed = false;
			mCurPosIdx = -1;
			this.invalidate();
			return true;
		case MotionEvent.ACTION_MOVE:
			mCurPosIdx =(int)( arg0.getY()/this.getHeight() * mAlphabet.length);
			if(mTouchListener != null && mCurPosIdx!=mOldPosIdx){
				if((mCurPosIdx>=0) && (mCurPosIdx<mAlphabet.length)) {
					mTouchListener.onTouch(mAlphabet[mCurPosIdx]);
					this.invalidate();
				}
				mOldPosIdx = mCurPosIdx;
			}
			
			if(mCurPosIdx >= 0 && mCurPosIdx < mAlphabet.length)
			{
				LetterNotice.setText(mAlphabet[mCurPosIdx]);
				LetterNotice.setVisibility(View.VISIBLE);
			}
			
			return true;
		default:
			return super.onTouchEvent(arg0);
		}
		
	}
	
	/**
	 * 接口
	 */
	public static interface OnTouchBarListener {
		void onTouch(String letter);
	}
	
	/**
	 * 向外公开的方法
	 */
	public void setOnTouchBarListener (OnTouchBarListener listener) {
		mTouchListener = listener;
	}
}
