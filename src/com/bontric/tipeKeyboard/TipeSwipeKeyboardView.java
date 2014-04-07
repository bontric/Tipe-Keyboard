/**
 *@name TipeSwipeKeyboardView
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Extends TipeKeyboardView to override the onTouch() function
 * of View. This implements the Tap and swipe function for the keyboard.
 * Also implements a long press listener
 */
package com.bontric.tipeKeyboard;

import com.bontric.tipeSettings.TipeSettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class TipeSwipeKeyboardView extends TipeKeyboardView {
	private boolean isNonCharacterKey = false;
	private Point startPos = null;
	private Point endPos = null;
	SharedPreferences sharedPref;
	protected boolean mLongpressDetectionActive = false;
	private Handler longPressHandler = new Handler();
	private final int longpressTimeout = 500; // final for now

	public TipeSwipeKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public TipeSwipeKeyboardView(Context context, AttributeSet attrs,
			int defStyle) {

		super(context, attrs, defStyle);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

	}

	/**
	 * This is *of course* for long press detection. -Untested Implementation!-
	 */
	private Runnable longPressActionRunnable = new Runnable() {
		public void run() {
			if (mLongpressDetectionActive) {
//				Log.d("Main","longpress!");
//				setDrawAlternativeChars(true);
//				invalidate();
			}
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
				TipeSettings.swipeSensitivity, (float) 1);

		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:

			startPos = getEventMedianPos(event);

			Key k = getPointToKey(startPos);
			if (k != null && k.codes[0] < 0) {
				isNonCharacterKey = true;
			} else {
				if (!mLongpressDetectionActive) {
					/*
					 * Start long press runnable -> is exectuted (default 500ms)
					 * delayed
					 */
					setLongPressedKey(getPointToKey(new Point(startPos.x,startPos.y)).codes[0]);
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
				 * reset timer for long press when finger moves
				 */
				startPos = getEventMedianPos(event);
				setLongPressedKey(getPointToKey(new Point(startPos.x,startPos.y)).codes[0]);
				longPressHandler.removeCallbacks(longPressActionRunnable);
				longPressHandler.postDelayed(longPressActionRunnable,
						longpressTimeout);
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mLongpressDetectionActive) {
				/*
				 * As soon as finger releases off screen long press detection is
				 * removed
				 */
				longPressHandler.removeCallbacks(longPressActionRunnable);
				mLongpressDetectionActive = false;
				setDrawAlternativeChars(false);
			}
			if (startPos != null) {
				endPos = getEventMedianPos(event);
				if (getPointToKey(endPos) == null
						|| !(getPointToKey(endPos).codes[0] >= 0 ^ isNonCharacterKey)) {
					Key startKey = getPointToKey(startPos);
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
				/*
				 * extend swipe vector by customizable factor (sensitivity)
				 */
				swipeVec.x = startPos.x
						+ (int) ((endPos.x - startPos.x) * sensitivity);
				swipeVec.y = startPos.y
						+ (int) ((endPos.y - startPos.y) * sensitivity);
				Key first = getPointToKey(swipeVec);
				if (first != null && first.codes[0] >= 0
						&& first.codes[0] <= 35) {

					gotKey = true;
					this.getOnKeyboardActionListener()
							.onRelease(first.codes[0]);

				}
			} else {
				Point lastPos = getEventMedianPos(event);
				Key first = getPointToKey(lastPos);
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

	// Calculates the median of all points of an motion event
	public Point getEventMedianPos(MotionEvent event) {
		int pointerCount = event.getPointerCount();
		Point medianPoint = new Point();
		medianPoint.x = (int) event.getX(0);
		medianPoint.y = (int) event.getY(0);

		for (int c = 1; c < pointerCount; c++) {
			medianPoint.x = (int) (medianPoint.x + (int) event.getX(c)) / 2;
			medianPoint.y = (int) (medianPoint.y + (int) event.getY(c)) / 2;
		}
		return medianPoint;
	}

	// Just returns the first key to a given point on the screen
	public Key getPointToKey(Point point) {
		Key firstKey = null;
		for (Key k : this.getKeyboard().getKeys()) {
			if (k.isInside(point.x, point.y)) {
				firstKey = k;
				break;
			}

		}
		return firstKey;
	}

}
