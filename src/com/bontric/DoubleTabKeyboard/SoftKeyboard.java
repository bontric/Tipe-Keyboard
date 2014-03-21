/*
 * @author Benedikt Wieder
 */



package com.bontric.DoubleTabKeyboard;

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

	// private StringBuilder mComposing;
	private boolean mShiftState;
	private DoubleTabKeyboardView mInputView;
	private DoubleTabKeyboard mQwertyKeyboard;
	private DoubleTabKeyboard mCurKeyboard;
	private int mLastDisplayWidth;
	private String charset;

	private final int KEYCODE_ENTER = -13;
	private final int KEYCODE_SPACE = -11;
	private final int KEYCODE_DELETE = -12;
	private final int KEYCODE_SHIFT = -10;
	private final int KEYCODE_SYM = -6;

	private boolean swypeActive = true;

	public SoftKeyboard() {
	}

	public void handleBackspace() {
		this.keyDownUp(KeyEvent.KEYCODE_DEL);
	}

	private void sendKey(int keyCode) {
		char curCharacter = (char) keyCode;
		getCurrentInputConnection().commitText("" + curCharacter, 1);

	}

	public void onCreate() {
		super.onCreate();
		getResources();

	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {
		this.mInputView = (DoubleTabKeyboardView) this.getLayoutInflater()
				.inflate(R.layout.input, null);
		/*
		 * remember this place @ben 
		 */
		this.mInputView.setOnKeyboardActionListener(this);
		this.mInputView.setKeyboard(this.mQwertyKeyboard);
		this.charset = (String) this.getResources().getText(
				R.string.defaultCharset);
		this.mInputView.init(charset);
		mInputView.setPreviewEnabled(false);

		return this.mInputView;

	}

	public void onFinishInput() {
		super.onFinishInput();

		this.setCandidatesViewShown(false);

		this.mCurKeyboard = mQwertyKeyboard;
		if (this.mInputView != null) {
			this.mInputView.closing();
		}
	}

	public void onInitializeInterface() {
		if (this.mQwertyKeyboard != null) {
			int displayWidth = getMaxWidth();

			if (displayWidth == mLastDisplayWidth) {
				return;
			}

			mLastDisplayWidth = displayWidth;
		}

		this.mQwertyKeyboard = new DoubleTabKeyboard(this, R.xml.base);

	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		this.mCurKeyboard = this.mQwertyKeyboard;
		this.mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		swypeActive = sharedPref.getBoolean(DtSettingsMain.bWS, false);

		super.onStartInputView(attribute, restarting);
		this.mInputView.setKeyboard(this.mCurKeyboard);
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
		if (!swypeActive) {
			if (0 <= primaryCode && charset.length() > primaryCode) {
				if (!mInputView.getLevelDownState()) {
					mInputView.setPressedKey(primaryCode);
				} else {
					sendKey((int) charset.charAt(mInputView
							.getCharCode(primaryCode)));
					if (mShiftState) {
						mShiftState = false;
					}
					handleShift();
					mInputView.setLevelDownState(false);
					// kind of dirty quickfix.. want to change this
				}

				mInputView.invalidate();
			} else {
				handleNonInputKeys(primaryCode);
			}
		}
	}

	@Override
	public void onPress(int primaryCode) {
		if (swypeActive) {
			if (0 <= primaryCode && charset.length() > primaryCode) {
				mInputView.setPressedKey(primaryCode);
				mInputView.invalidate();
			}
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		if (swypeActive) {
			if (0 <= primaryCode && charset.length() > primaryCode) {
				sendKey((int) charset.charAt(mInputView
						.getCharCode(primaryCode)));
				if (mShiftState) {
					mShiftState = false;
				}
				handleShift();
				mInputView.setLevelDownState(false);
				// kind of dirty quickfix.. want to change this
			} else {
				handleNonInputKeys(primaryCode);
			}

		}
	}

	private void handleNonInputKeys(int primaryCode) {
		switch (primaryCode) {
		case KEYCODE_DELETE:
			handleBackspace();
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
		if (k.label.equals(new String("SYM"))) {
			charset = (String) this.getResources().getText(R.string.SymbolSet);
			mInputView.setCharset(charset);
			k.label = "QWERZ";
		} else {
			charset = (String) this.getResources().getText(
					R.string.defaultCharset);
			mInputView.setCharset(charset);
			k.label = "SYM";
		}
		//mInputView.invalidate();

	}

	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	/**
	 * 
	 * 
	 */
	private void handleShift() {

		if (mInputView.getCharset() != (String) this.getResources().getText(
				R.string.SymbolSet)) {
			if (mShiftState) {
				charset = (String) this.getResources().getText(
						R.string.defaultCharsetShift);
				mInputView.setCharset(charset);
			} else {
				charset = (String) this.getResources().getText(
						R.string.defaultCharset);
				mInputView.setCharset(charset);
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
