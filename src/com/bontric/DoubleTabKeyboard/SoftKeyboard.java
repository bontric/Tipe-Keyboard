/*
 * @author Benedikt Wieder
 */

package com.bontric.DoubleTabKeyboard;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
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
	
	private CanidateView mCandidateView;
	private String curSugestPrototype;
	
	private int mLastDisplayWidth;
	private String mCurCharset;
	private String mCurSymset;

	final int KEYCODE_ENTER = -13;
	final int KEYCODE_SPACE = -11;
	final int KEYCODE_DELETE = -12;
	final int KEYCODE_SHIFT = -10;
	final int KEYCODE_SYM = -6;
	SharedPreferences sharedPref;
	private boolean isSwipe = true;

	public void onCreate() {
		super.onCreate();
		getResources();
		/*
		 * loads default settings on first start! ducking important..
		 */
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		
	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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
		this.mInputView.setSoftKeyboard(this);
		mInputView.setPreviewEnabled(false);

		return this.mInputView;
	}
	

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
    	Log.d("CreateCandiateView", "Here is onCreateCandiateView");
    	
        mCandidateView = new CanidateView(this);
        mCandidateView.setService(this);
        curSugestPrototype = "";
        return mCandidateView;
    }
        
	public void onFinishInput() {
		super.onFinishInput();
		
		curSugestPrototype = "";
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
		//TODO init suggestions list
		
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

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		isSwipe = sharedPref.getBoolean(DtSettingsMain.swypeActive, false);
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

		// quickfix for now.. fixing this depends on future updates.. so keep
		// this in mind @ben
		Key k = getKey(KEYCODE_SYM);
		mInputView.setCharset(mCurCharset);
		k.label = "SYM";
		// ----

		this.mInputView.closing();
		this.mShiftState = false;

	}

	@Override public void onDisplayCompletions(CompletionInfo[] completions) {
		mCandidateView.getSuggestionsForWord(curSugestPrototype);
	}
	
	public void chooseSuggestion(String composedWord){
		mInputView.setLevelDownState(false);
		InputConnection ic = getCurrentInputConnection();
		//So messy but the best we got so far  .....
		ic.deleteSurroundingText(curSugestPrototype.length(), 0);
		ic.commitText(composedWord+" ", composedWord.length()+1);
		curSugestPrototype = "";
		setCandidatesViewShown(false);
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
					sendKey((int) mInputView.getCharset().charAt(
							mInputView.getCharCode(primaryCode)));
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
		//Add to word 
		curSugestPrototype += (char) keyCode;
		mCandidateView.getSuggestionsForWord(curSugestPrototype);
		setCandidatesViewShown(true);
		
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
				
				if(curSugestPrototype.length() <= 1)
					curSugestPrototype = "";
				else
					curSugestPrototype = curSugestPrototype.substring(0, curSugestPrototype.length()-1);
				
				mCandidateView.getSuggestionsForWord(curSugestPrototype);
				if(curSugestPrototype.isEmpty() || !mCandidateView.hasSuggestions())
					setCandidatesViewShown(false);
				else
					setCandidatesViewShown(true);
				
				keyDownUp(KeyEvent.KEYCODE_DEL);
			}
			break;
		case KEYCODE_SHIFT:
			mShiftState = !mShiftState;
			handleShift();
			break;
		case KEYCODE_SPACE:
			sendKey(32);
			curSugestPrototype = "";
			break;
		case KEYCODE_ENTER:

			keyDownUp(KeyEvent.KEYCODE_ENTER);
			curSugestPrototype = "";
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
