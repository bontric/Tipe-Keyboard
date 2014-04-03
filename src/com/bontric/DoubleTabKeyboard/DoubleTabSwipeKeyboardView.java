/*
 * @author Jakob Frick
 */
package com.bontric.DoubleTabKeyboard;

import com.bontric.DtSettings.DtSettingsMain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class DoubleTabSwipeKeyboardView extends DoubleTabKeyboardView {
	private boolean isNonCharacterKey = false;
	private Point startPos = null;
	private Point endPos = null;
	SharedPreferences sharedPref;
	protected boolean mLongpressDetectionActive = false;
	private Handler longPressHandler = new Handler();
	private final int longpressTimeout = 500; // final for now

	public DoubleTabSwipeKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public DoubleTabSwipeKeyboardView(Context context, AttributeSet attrs,
			int defStyle) {

		super(context, attrs, defStyle);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

	}

	/**
	 * This is *of course* for long press detection. not guaranteed to work for
	 * now.. reference:
	 * http://stackoverflow.com/questions/1877417/how-to-set-a-timer-in-android
	 */
	private Runnable longPressActionRunnable = new Runnable() {
		public void run() {
			// if (mLongpressDetectionActive) {
			// Log.d("Main", "Ben's Longpress detection works!");
			// setDrawAlternativeChars(true);
			// invalidate();
			// }
		}
	};

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		/**
		 * Definitions for sliding behavior: swiping off a non-character key to
		 * a character key or an empty area => do nothing
		 * 
		 * (Delete Key IS an exception since it is Long-pressable!
		 * 
		 * swiping off a non-character key to a non-character key => interpret
		 * end position
		 * 
		 * swiping off a character key to a non-character key => do nothing
		 * 
		 * Any touch related behavior using swipe not defined here is considered
		 * a bug. In which case it should be reported (or fixed)
		 */
		int eventaction = event.getAction();
		boolean gotKey;
		gotKey = false;
		double sensitivity = 1 + 0.25 * sharedPref.getFloat(
				DtSettingsMain.swipeSensitivity, (float) 1);

		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:

			startPos = getEventMedianPos(event);

			Key k = getKeyToPoint(startPos);
			if (k != null && k.codes[0] < 0) {
				isNonCharacterKey = true;
			} else {
				if (!mLongpressDetectionActive) {
					mLongpressDetectionActive = true;
					setDrawAlternativeChars(false);
					longPressHandler.postDelayed(longPressActionRunnable,
							longpressTimeout);
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mLongpressDetectionActive) {
				/*
				 * reset timer when finger moves
				 */
				longPressHandler.removeCallbacks(longPressActionRunnable);
				longPressHandler.postDelayed(longPressActionRunnable,
						longpressTimeout);
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mLongpressDetectionActive) {
				longPressHandler.removeCallbacks(longPressActionRunnable);
				mLongpressDetectionActive = false;
				setDrawAlternativeChars(false);
			}
			if (startPos != null) {
				endPos = getEventMedianPos(event);
				if (getKeyToPoint(endPos) == null
						|| !(getKeyToPoint(endPos).codes[0] >= 0 ^ isNonCharacterKey)) {
					Key startKey = getKeyToPoint(startPos);
					if (startKey != null && startKey.repeatable) {

						super.onTouchEvent(event);
					}
					startPos = null;
					endPos = null;
					isNonCharacterKey = false;
					levelDownState = false;
					invalidate();
					return true;
				}
				Point swipeVec = new Point();
				swipeVec.x = startPos.x
						+ (int) ((endPos.x - startPos.x) * sensitivity);
				swipeVec.y = startPos.y
						+ (int) ((endPos.y - startPos.y) * sensitivity);
				Key first = getKeyToPoint(swipeVec);
				if (first != null && first.codes[0] >= 0
						&& first.codes[0] <= 35) {

					gotKey = true;
					this.getOnKeyboardActionListener()
							.onRelease(first.codes[0]);

				}
			} else {
				Point lastPos = getEventMedianPos(event);
				Key first = getKeyToPoint(lastPos);
				if (first == null)
					return true;

				this.getOnKeyboardActionListener().onKey(first.codes[0],
						first.codes);

			}
			isNonCharacterKey = false;
			levelDownState = false;
			invalidate();

			startPos = null;
			endPos = null;
			break;
		}

		if (!gotKey) {
			super.onTouchEvent(event);
		}

		// tell the system that we handled the event and no further processing
		// is required
		return true;
	}

	public static int getLongPressTimeout() {
		return 0;

	}

	public boolean onLongPress(Keyboard.Key popupKey) {
		Log.d("Main", "LongPress detected");
		return super.onLongPress(popupKey);
	}

}
