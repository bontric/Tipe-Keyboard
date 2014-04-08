package com.bontric.tipeKeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class LowerBarView extends View {
	private ActionArea shiftButton;
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
		this.setBackgroundColor(KeyboardHandler.BackgroundColor);
		LayoutParams params = new LinearLayout.LayoutParams(
				KeyboardHandler.KEYBOARD_WIDTH,
				KeyboardHandler.LOWER_BAR_VIEW_HEIGHT);
		this.setLayoutParams(params);

		int width = params.width;
		int height = params.height;
		Bitmap tempIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.sym_keyboard_shift);

		shiftButton = new ActionArea(0, 0, width / 3, height,
				KeyboardHandler.BackgroundColor, tempIcon) {

			@Override
			public void onTouch() {
				KeyboardHandler.shiftState = !KeyboardHandler.shiftState;
				KeyboardHandler.handleShift();

			}
		};

		tempIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.sym_keyboard_space);

		spaceButton = new ActionArea(width / 3, 0, width / 3, height,
				KeyboardHandler.BackgroundColor, tempIcon) {
			@Override
			public void onTouch() {
				KeyboardHandler.inputConnection.handleSpace();

			}
		};

		tempIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.sym_keyboard_delete);
		deleteButton = new ActionArea(2 * width / 3, 0, width / 3, height,
				KeyboardHandler.BackgroundColor, tempIcon) {
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

	/*
	 * ###############################################################
	 * Unflexible longpress inmplementation
	 */

	private Runnable longPressActionRunnable = new Runnable() {
		public void run() {
			deleteButton.onTouch();
			longPressHandler.postDelayed(longPressActionRunnable,
					repeatIntervall);

		}
	};
	private Handler longPressHandler = new Handler();
	private final int longpressTimeout = 100; // final for now
	private final int repeatIntervall = 70;

	/*
	 * ################################################################
	 */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		PointF touched = util.getEventMedianPos(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (deleteButton.contains(touched)) {
				longPressHandler.postDelayed(longPressActionRunnable,
						longpressTimeout);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			longPressHandler.removeCallbacks(longPressActionRunnable);
			if (deleteButton.contains(touched)) {
				longPressHandler.postDelayed(longPressActionRunnable,
						longpressTimeout);
			}
			break;
		case MotionEvent.ACTION_UP:

			longPressHandler.removeCallbacks(longPressActionRunnable);
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


}
