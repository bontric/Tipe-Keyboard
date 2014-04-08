package com.bontric.tipeKeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class LowerBarView extends View {
	ActionArea shiftButton;
	private ActionArea spaceButton;
	private ActionArea deleteButton;

	public LowerBarView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
		init();
	}

	public LowerBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LowerBarView(Context context) {
		super(context);
		init();
	}

	public void init() {
		this.setBackgroundColor(Color.GREEN);
		LayoutParams params = new LinearLayout.LayoutParams(
				KeyboardHandler.KEYBOARD_WIDTH,
				KeyboardHandler.LOWER_BAR_VIEW_HEIGHT);
		this.setLayoutParams(params);
		this.requestLayout();

		int width = params.width;
		int height = params.height;
		shiftButton = new ActionArea(getX(), getY(), width / 4, height,
				KeyboardHandler.CharViewLightColor, "^") {

			@Override
			public void onTouch() {
				KeyboardHandler.shiftState = !KeyboardHandler.shiftState;
				KeyboardHandler.handleShift();

			}
		};
		spaceButton = new ActionArea(getX() + width / 4, getY(), width / 2,
				height, KeyboardHandler.CharViewDarkColor, "SPACE") {
			@Override
			public void onTouch() {
				KeyboardHandler.inputConnection.handleSpace();

			}
		};
		deleteButton = new ActionArea(getX() + 3 * width / 4, getY(),
				width / 4, height, KeyboardHandler.CharViewLightColor, "DEL") {
			@Override
			public void onTouch() {
				KeyboardHandler.inputConnection.handleDelete();

			}
		};
		this.invalidate();
	}

	@SuppressLint("NewApi")
	public void onDraw(Canvas canvas) {
		shiftButton.draw(canvas);
		spaceButton.draw(canvas);
		deleteButton.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		PointF touched = getEventMedianPos(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if (shiftButton.contains(touched)) {
				shiftButton.onTouch();
			} else if (spaceButton.contains(touched)) {
				spaceButton.onTouch();
			} else if (deleteButton.contains(touched)) {
				deleteButton.onTouch();
			}
			break;
		}

		return true;
	}

	// Calculates the median of all points of an motion event
	public PointF getEventMedianPos(MotionEvent event) {
		int pointerCount = event.getPointerCount();
		PointF medianPoint = new PointF();
		medianPoint.x = event.getX(0);
		medianPoint.y = event.getY(0);

		for (int c = 1; c < pointerCount; c++) {
			medianPoint.x = (medianPoint.x + event.getX(c)) / 2;
			medianPoint.y = (medianPoint.y + event.getY(c)) / 2;
		}
		return medianPoint;
	}
}
