/**
 *@name TipeKeyboardView
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Extends KeyboardView. This implements the layer based Keyboard visuals.
 *  
 */
package com.bontric.tipeKeyboard;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.bontric.tipeSettings.TipeSettings;;
public class TipeKeyboardView extends KeyboardView {
	private String charset;
	protected boolean levelDownState;
	private Paint paint;
	private int pressedKey;
	SharedPreferences sharedPref;
	SoftKeyboard mSoftKeyboard;
	private boolean mDrawAlternativeChars;

	public TipeKeyboardView(Context context, AttributeSet attrs) {

		super(context, attrs);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

	}

	public TipeKeyboardView(Context context, AttributeSet attrs,
			int defStyle) {

		super(context, attrs, defStyle);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Initialize the paint process
	 */
	public void init(String charset) {
		this.charset = charset;
		this.levelDownState = false;
		this.paint = new Paint();
		this.setBackgroundColor(sharedPref.getInt(
				TipeSettings.backgroundColor, Color.BLACK));
		paint.setTextSize(getResources().getDimension(R.dimen.font_height));
		paint.setColor(sharedPref.getInt(TipeSettings.normFontColor,
				Color.WHITE));
		paint.setFakeBoldText(true);
		paint.setTextAlign(Align.CENTER);
	}

	/**
	 * returns weather we're on first (false) or second(true) layer
	 */
	public boolean getLevelDownState() {
		return this.levelDownState;
	}

	/**
	 * sets weather we're on first (false) or second(true) layer
	 */
	public void setLevelDownState(boolean b) {

		this.levelDownState = b;
	}

	public void setPressedKey(int primaryCode) {

		this.setLevelDownState(true);
		this.pressedKey = primaryCode;
	}

	/**
	 * this returns which button was pressed relative to "pressedKey" Call this
	 * in "layerDown" state ONLY!
	 */
	public int getCharCode(int primaryCode) {

		return (pressedKey / 6) * 6 + (primaryCode / 6);
	}

	/**
	 * draw upper level of keyboard (contains all characters of charset)
	 */
	public void levelUp(Canvas canvas) {

		drawBackgrounds(canvas);
		int i = 0;
		for (Key key : this.getKeyboard().getKeys()) {
			if (key.codes[0] >= 0 && key.codes[0] < charset.length()) {
				String label = "" + charset.charAt(key.codes[0]);
				PointF center = getTextCenterToDraw(label, new RectF(key.x,
						key.y, key.x + key.width, key.y + key.height), paint);
				canvas.drawText(label, center.x, center.y, paint);
				++i;
			}
			if (i >= charset.length()) {
				break;
			}

		}

		this.setLevelDownState(false);
	}

	/**
	 * Go to second level. This will put the 6 characters in the area pressed
	 * before on all 6 buttons. Therefore it is important to Set the pressed key
	 * before invalidating the view!
	 */
	public void levelDown(Canvas canvas) {
		/*
		 * this function is not well programmed and is very specific. I'll fix
		 * this up one day
		 */
		drawBackgrounds(canvas);
		// TODO a litte bit hackly
		Key key = this.getKeyboard().getKeys().get(4);
		for (int i = (pressedKey / 6) * 6; i < (pressedKey / 6) * 6 + 6; ++i) {

			String label = "" + charset.charAt(i);
			PointF center = getTextCenterToDraw(label, new RectF(3
					* ((i % 6) % 3) * key.width,
					2 * ((i % 6) / 3) * key.height, 3 * ((i % 6) % 3)
							* key.width + 3 * key.width, 2 * ((i % 6) / 3)
							* key.height + 2 * key.height), paint);

			canvas.drawText(label, center.x, center.y + key.height, paint);

		}

		this.setLevelDownState(true);
	}

	/**
	 * 
	 * @param isActive
	 * weather we need to draw Alternatives for char ( used for long press handling
	 * 
	 */
	public void setDrawAlternativeChars(boolean isActive) {
		mDrawAlternativeChars = isActive;
	}

	public boolean getDrawAlternativeChars() {
		return mDrawAlternativeChars;
	}

	/**
	 * draw backgrounds of char area
	 */
	private void drawBackgrounds(Canvas canvas) {

		Paint bgPaint = new Paint();

		for (Key key : this.getKeyboard().getKeys()) {
			if (key.codes[0] > -1) {

				if ((key.codes[0] / 6) % 2 == 0) {
					bgPaint.setColor(sharedPref.getInt(
							TipeSettings.darkBgColor, Color.BLACK));
					canvas.drawRect(key.x, key.y, key.x + key.width, key.y
							+ key.height, bgPaint);

				} else {
					bgPaint.setColor(sharedPref.getInt(
							TipeSettings.lightBgColor, Color.GRAY));

					canvas.drawRect(key.x, key.y, key.x + key.width, key.y
							+ key.height, bgPaint);

				}
			}

		}

	}

	public void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		if (levelDownState) {
			levelDown(canvas);
		} else {
			levelUp(canvas);
		}
	}

	/**
	 * returns a point which holds the coorinates to center given text within a
	 * rectangle -> center.draw()
	 */
	public static PointF getTextCenterToDraw(String text, RectF region,
			Paint paint) {

		Rect textBounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), textBounds);
		float x = region.centerX() - textBounds.width() * 0.4f;
		float y = region.centerY() + textBounds.height() * 0.4f;
		return new PointF(x, y);
	}

	public void setCharset(String cs) {
		this.charset = cs;
	}

	public String getCharset() {
		return this.charset;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setSoftKeyboard(SoftKeyboard sk) {
		this.mSoftKeyboard = sk;
	}

}
