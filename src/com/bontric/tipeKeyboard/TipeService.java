/**
 *@name SoftKeyboard
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Implementation of the Input Method Service.
 * Handles the IME life cycle and text input.
 * 
 *  
 */
package com.bontric.tipeKeyboard;

import android.annotation.SuppressLint;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard.Key;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.bontric.tipeSettings.TipeSettings;

public class TipeService extends InputMethodService {

	static final boolean DEBUG = false;
	Vibrator mVibrator;
	TipeView mTipeView;

	boolean showCandidates;
	
	public void onCreate() {
		super.onCreate();
		getResources();
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {
		/*
		 * reset settings -> Handles changes in preferences
		 */
		KeyboardHandler.input_connection.setIMS(this);
		/*
		 * Initialize all Views within the TipeViewLayout
		 */
		mTipeView = null;
		mTipeView = (TipeView) this.getLayoutInflater().inflate(
				R.layout.tipe_view, null);
		mTipeView.init();

		return mTipeView;
	}

	public void onFinishInput() {
		super.onFinishInput();
		KeyboardHandler.input_connection.resetComposedWord();
	}

	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);

	}

	public void onInitializeInterface() {

	}

	public Vibrator getCurVibrator() {
		return mVibrator;
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {

		super.onStartInput(attribute, restarting);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		/*
		 * Show candidates for text or in doubt
		 */

		showCandidates =
		((attribute.inputType & InputType.TYPE_MASK_CLASS) != InputType.TYPE_NUMBER_VARIATION_PASSWORD) &&
		((attribute.inputType & InputType.TYPE_MASK_CLASS) != InputType.TYPE_TEXT_VARIATION_PASSWORD) &&
		((attribute.inputType & InputType.TYPE_MASK_CLASS) != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) ;
		
		Log.d("onStartInput", "Show canies " + showCandidates);
		if(!showCandidates){
			this.setCandidatesViewShown(false);
			KeyboardHandler.input_connection.setComposing(false);
		}
			
		
		mTipeView.initKeyboardHandler(this);
		mTipeView.init();
		TipeSettings.settings_changed = false;
	
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		
		// Check if text was selected
		if(newSelEnd - newSelStart > 0){
			if(showCandidates)
				KeyboardHandler.input_connection.setComposedWord(
					 getCurrentInputConnection().getSelectedText(0).toString());
		}
		else {
			//Calculate if you are in a word
			if(getCurrentInputConnection() == null){
			
				String currentInputtxt = getCurrentInputConnection().getTextBeforeCursor(newSelStart, 0).toString();
				int spacePos = currentInputtxt.lastIndexOf(" ") + 1 ;
				
				
				if(showCandidates && newSelStart - spacePos > 0 ){
					KeyboardHandler.input_connection.setComposedWord(
							getCurrentInputConnection().getTextBeforeCursor(
									newSelStart - spacePos, 0).toString());
					
				}
			}
		} 
		
	}

	@Override
	public View onCreateCandidatesView() {
		mTipeView.initKeyboardHandler(this);
		// Create Candites view
		CandidateView cv = KeyboardHandler.input_connection.initCandidateView(this);
		if (KeyboardHandler.show_suggestions) {
			return cv;
		}
		return null;
	}

}
