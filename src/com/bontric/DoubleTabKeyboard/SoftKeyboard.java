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

	private boolean delayDeleteCheck = false;
	private boolean mShiftState;
	private DoubleTabKeyboardView mInputView;
	private DoubleTabKeyboard mQwertyKeyboard;
	private DoubleTabKeyboard mCurKeyboard;
	private int mLastDisplayWidth;
	private String mCurCharset;
	private String mCurSymset;

	private final int KEYCODE_ENTER = -13;
	private final int KEYCODE_SPACE = -11;
	private final int KEYCODE_DELETE = -12;
	private final int KEYCODE_SHIFT = -10;
	private final int KEYCODE_SYM = -6;

	private boolean swypeActive = true;

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

	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);
		this.mCurCharset = (String) this.getResources().getText(
				R.string.defaultCharset);
		this.mInputView.init(mCurCharset);
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
		swypeActive = sharedPref.getBoolean(DtSettingsMain.swypeActive, false);
		boolean useCustomCharset = sharedPref.getBoolean(
				DtSettingsMain.useCustomCharset, false);
		boolean useCustomSymset = sharedPref.getBoolean(
				DtSettingsMain.useCustomSymset, false);
		if (useCustomCharset) {
			this.mCurCharset = sharedPref.getString(DtSettingsMain.cusCharset,
					"Err   or!");
		} else {
			this.mCurCharset = (String) this.getResources().getText(
					R.string.defaultCharset);
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
			if (0 <= primaryCode && mCurCharset.length() > primaryCode) {
				if (!mInputView.getLevelDownState()) {
					mInputView.setPressedKey(primaryCode);
				} else {
					sendKey((int) mCurCharset.charAt(mInputView
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

		if (swypeActive) {
			if (0 <= primaryCode && mCurCharset.length() > primaryCode) {
				mInputView.setPressedKey(primaryCode);
				mInputView.invalidate();
			}
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		if (swypeActive) {
			if (0 <= primaryCode && mCurCharset.length() > primaryCode) {
				sendKey((int) mCurCharset.charAt(mInputView
						.getCharCode(primaryCode)));
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
		if (getCurrentInputConnection().getTextBeforeCursor(1, 0).equals(" ")
				&& !delayDeleteCheck) {
			delayDeleteCheck = true;
		} else {

			this.keyDownUp(KeyEvent.KEYCODE_DEL);
			delayDeleteCheck = false;
		}

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
		if (k.label.equals(new String("SYM"))) {

			mInputView.setCharset(mCurSymset);
			k.label = "QWERZ";
		} else {
			mInputView.setCharset(mCurCharset);
			k.label = "SYM";
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

		if (mInputView.getCharset() != (String) this.getResources().getText(
				R.string.SymbolSet)) {
			if (mShiftState) {
				mCurCharset = mInputView.getCharset().toUpperCase(
						Locale.GERMANY);
				mInputView.setCharset(mCurCharset);
				/*
				 * note this keyboard is for german use right now.. work on
				 * locale one day @ben
				 */
			} else {

				mCurCharset = mInputView.getCharset().toLowerCase(
						Locale.GERMANY);
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
