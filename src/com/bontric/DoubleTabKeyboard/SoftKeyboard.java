/*
 * @author Benedikt Wieder
 */

package com.bontric.DoubleTabKeyboard;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.bontric.DoubleTab.R;
import com.bontric.DtSettings.DtSettingsMain;

public class SoftKeyboard extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener {

	static final boolean DEBUG = false;

	private boolean mShiftState;
	private DoubleTabKeyboardView mInputView;
	private DoubleTabKeyboard mCurKeyboard;
	private int mLastDisplayWidth;
	private String mCurCharset;
	private String mCurSymset;

	private final int KEYCODE_ENTER = -13;
	private final int KEYCODE_SPACE = -11;
	private final int KEYCODE_DELETE = -12;
	private final int KEYCODE_SHIFT = -10;
	private final int KEYCODE_SYM = -6;

	private boolean isSwipe = true;

	public void onCreate() {
		super.onCreate();
		getResources();

	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		isSwipe = sharedPref.getBoolean(DtSettingsMain.swypeActive, false);
		this.mInputView = null;

		if (isSwipe) {
			this.mInputView = (DoubleTabSwipeKeyboardView) this
					.getLayoutInflater().inflate(R.layout.swipeinput, null);
		} else {
			this.mInputView = (DoubleTabKeyboardView) this.getLayoutInflater()
					.inflate(R.layout.input, null);
		}

		/*
		 * remember this place @ben
		 */
		this.mInputView.setOnKeyboardActionListener(this);
		this.mInputView.setKeyboard(this.mCurKeyboard);

		mInputView.setPreviewEnabled(false);

		return this.mInputView;
	}

	public void onFinishInput() {
		super.onFinishInput();

		this.setCandidatesViewShown(false);

		if (this.mInputView != null) {
			this.mInputView.closing();
		}

	}

	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);
		this.mInputView.init(mCurCharset);
	}

	public void onInitializeInterface() {
		if (this.mCurKeyboard != null) {
			int displayWidth = getMaxWidth();

			if (displayWidth == mLastDisplayWidth) {
				return;
			}

			mLastDisplayWidth = displayWidth;
		}

		this.mCurKeyboard = new DoubleTabKeyboard(this, R.xml.base);

	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {

		this.setInputView(this.onCreateInputView());

		super.onStartInput(attribute, restarting);
		this.mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		isSwipe = sharedPref.getBoolean(DtSettingsMain.swypeActive, false);
		boolean useCustomCharset = sharedPref.getBoolean(
				DtSettingsMain.useCustomCharset, false);
		boolean useCustomSymset = sharedPref.getBoolean(
				DtSettingsMain.useCustomSymset, false);
		boolean useAdvancedCharset = sharedPref.getBoolean(
				DtSettingsMain.useAdvancedCharset, false);
		
		if (useAdvancedCharset) {
			this.mCurCharset = (String) this.getResources().getText(
					R.string.advancedCharset);
		} else {
			if (useCustomCharset) {
				this.mCurCharset = sharedPref.getString(
						DtSettingsMain.cusCharset, "Err   or!");
			} else {
				String getLangCharset = sharedPref.getString(
						DtSettingsMain.cusLanguage, "");
				
				this.mCurCharset = getLangCharset;
			}
		}
		if (useCustomSymset) {
			this.mCurSymset = sharedPref.getString(DtSettingsMain.cusSymset,
					"Err   or!");
		} else {
			this.mCurSymset = (String) this.getResources().getText(
					R.string.SymbolSet);
		}

		this.mInputView.init(mCurCharset);

		super.onStartInputView(attribute, restarting);
		this.mInputView.setKeyboard(this.mCurKeyboard); // needs check@ben

		// quickfix for now.. fixing this depends on future updates.. so keep
		// this in mind @ben
		Key k = getKey(KEYCODE_SYM);
		mInputView.setCharset(mCurCharset);
		k.label = "SYM";
		// ----

		this.mInputView.closing();
		this.mShiftState = false;

	}

	@Override
	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();

		if (ic == null)
			return;

		ic.beginBatchEdit();

		ic.commitText(text, 0);
		ic.endBatchEdit();

	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		if (!isSwipe) {
			if (0 <= primaryCode && mCurCharset.length() > primaryCode) {
				if (!mInputView.getLevelDownState()) {
					mInputView.setPressedKey(primaryCode);
				} else {
					sendKey((int) mInputView.getCharset().charAt(mInputView
							.getCharCode(primaryCode)));
					if (mShiftState) {
						mShiftState = false;
					}
					handleShift();
					mInputView.setLevelDownState(false);
				}

				mInputView.invalidate();
			} else {
				handleNonInputKeys(primaryCode);
			}
		}
	}

	@Override
	public void onPress(int primaryCode) {
		if (isSwipe) {
			if (0 <= primaryCode && mCurCharset.length() > primaryCode) {
				mInputView.setPressedKey(primaryCode);
				mInputView.invalidate();
			}
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		if (isSwipe) {
			if (0 <= primaryCode && mCurCharset.length() > primaryCode) {
				sendKey((int) mInputView.getCharset().charAt(
						mInputView.getCharCode(primaryCode)));

				if (mShiftState) {
					mShiftState = false;
				}
				handleShift();
				mInputView.setLevelDownState(false);
			} else {
				handleNonInputKeys(primaryCode);
			}

		}
	}

	public void handleBackspace() {
		/*
		 * meh this is dirty... i feel so dirty... but it works.. for now..
		 */

		this.keyDownUp(KeyEvent.KEYCODE_DEL);

	}

	private void sendKey(int keyCode) {
		char curCharacter = (char) keyCode;
		getCurrentInputConnection().commitText("" + curCharacter, 1);

	}

	/*
	 * handle action for non-input key like SPACE /DELETE etc.
	 */
	private void handleNonInputKeys(int primaryCode) {
		switch (primaryCode) {
		case KEYCODE_DELETE:
			if (mInputView.getLevelDownState()) {
				mInputView.setLevelDownState(false);
				mInputView.invalidate();
			} else {
				handleBackspace();
			}
			break;
		case KEYCODE_SHIFT:
			mShiftState = !mShiftState;
			handleShift();
			break;
		case KEYCODE_SPACE:
			sendKey(32);
			break;
		case KEYCODE_ENTER:

			keyDownUp(KeyEvent.KEYCODE_ENTER);

			break;
		case KEYCODE_SYM:
			handleSYM();
			break;

		}

	}

	private void handleSYM() {
		Key k = getKey(KEYCODE_SYM);
		if (mInputView.getCharset().equals(mCurSymset)) {
			mInputView.setCharset(mCurCharset);
			k.label = "SYM";

		} else {
			mInputView.setCharset(mCurSymset);
			k.label = "QWERZ";
		}
		mInputView.invalidate();

	}

	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	private void handleShift() {

		if (!mInputView.getCharset().equals(mCurSymset)) {
			/*
			 * String.toUpperCase() is handling the German "ß" wrong (replaces
			 * it with "SS")... so this is a rather bad 'n' dirty fix..but i
			 * have no other idea than rewriting the toUpperCase() function
			 */
			if (mShiftState) {
				String cs = mCurCharset.replace('ß', '\uffff');
				mCurCharset = cs.toUpperCase(Locale.GERMAN).replace('\uffff',
						'ß');
				mInputView.setCharset(mCurCharset);
				/*
				 * note this keyboard is for german use right now.. work on
				 * locale one day @ben
				 */
			} else {

				String cs = mCurCharset.replace('ß', '\uffff');
				mCurCharset = cs.toLowerCase(Locale.GERMAN).replace('\uffff',
						'ß');
				mInputView.setCharset(mCurCharset);
			}
		} else {
			mShiftState = false;// there's no shift in symbol view

		}
		mInputView.invalidate();
	}

	/**
	 * return Keyboard.Key of mCurKeyboard by looking up the primary code
	 */
	private Key getKey(int primaryCode) {

		for (Key k : mCurKeyboard.getKeys()) {
			if (k.codes[0] == primaryCode) {
				return k;
			}
		}
		return null;
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {

	}

	@Override
	public void swipeUp() {

	}

}
