package com.bontric.DoubleTabKeyboard;

import android.annotation.SuppressLint;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.bontric.DoubleTab.R;

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
		this.mInputView.setOnKeyboardActionListener(this);
		this.mInputView.setKeyboard(this.mQwertyKeyboard);
		this.charset = (String) this.getResources().getText(R.string.charset);
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
	public void onKey(int primaryCode, int[] keyCodes) {

	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		this.mCurKeyboard = this.mQwertyKeyboard;
		this.mCurKeyboard.setImeOptions(getResources(),
				attribute.imeOptions);
//		switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
//		case EditorInfo.TYPE_CLASS_NUMBER: // 2
//			this.mCurKeyboard.setImeOptions(getResources(),
//					attribute.imeOptions);
//			
//		default:
//			this.mCurKeyboard = this.mQwertyKeyboard;
//			break;
//		}
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
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
	public void onPress(int primaryCode) {

		if (0 <= primaryCode && charset.length() > primaryCode) {
			mInputView.setPressedKey(primaryCode);
			mInputView.setLevelDownState(true);
			mInputView.invalidate();
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		Log.d("Main", "" + primaryCode);
		if (0 <= primaryCode && charset.length() > primaryCode) {
			sendKey((int) charset.charAt(mInputView.getCharCode(primaryCode)));
			if (mShiftState) {
				mShiftState = false;
			}
			handleShift();
		} else {
			// note to myself: bad programming style!
			switch (primaryCode) {
			case (-12):
				handleBackspace();

				break;

			case -10:
				mShiftState = true;
				handleShift();
				break;
			case -11:
				sendKey(' ');
				break;
			case -13:
				keyDownUp(KeyEvent.KEYCODE_ENTER);
				break;
			case -6:
				charset = (String) this.getResources().getText(R.string.Sym);
				mInputView.setCharset(charset);
				break;
			case -1:
				charset = (String) this.getResources()
						.getText(R.string.charset);
				mInputView.setCharset(charset);
				break;
			}
		}
		mInputView.setLevelDownState(false);

		mInputView.invalidate();

	}

	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	private void handleShift() {
		if (mShiftState) {
			charset = (String) this.getResources().getText(R.string.CHARSET);
			mInputView.setCharset(charset);
			mInputView.invalidate();
		} else {
			charset = (String) this.getResources().getText(R.string.charset);
			mInputView.setCharset(charset);
			mInputView.invalidate();
		}

	}

	@Override
	public void swipeDown() {
		// this.handleClose();
	}

	@Override
	public void swipeLeft() {
		Log.d("Main", "swipe left");

	}

	@Override
	public void swipeRight() {
		Log.d("Main", "swipe right");

	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub

	}

}
