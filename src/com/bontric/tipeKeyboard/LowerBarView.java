package com.bontric.tipeKeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class LowerBarView extends View{
	ActionArea shiftButton;
	
	public LowerBarView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
	}

	public LowerBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LowerBarView(Context context) {
		super(context);
	}
	
	public void init() {
		this.getLayoutParams().height = KeyboardHandler.LOWER_BAR_VIEW_HEIGHT;
		this.setBackgroundColor(KeyboardHandler.CharViewDarkColor);
		shiftButton = new ActionArea(getX(),getY(),getWidth()/4,getHeight(),KeyboardHandler.CharViewDarkColor,"SHIFT") {
			@Override
			public void onTouch() {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	
	public void onDraw(Canvas canvas){
		shiftButton.draw(canvas);
	}
}
