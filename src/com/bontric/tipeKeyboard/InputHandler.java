package com.bontric.tipeKeyboard;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

public class InputHandler {

	private TipeService mTipeService;

	// Word for composing
	private String composedWord;
	private CandidateView mCandidateView;

	public InputHandler() {

	}

	public void setIMS(TipeService tService) {
		mTipeService = tService;
	}

	public void sendKey(char c) {
		/*
		 * this could be interesting.. maybe it helps learning :) but you don't
		 * recognize it very much..
		 */
		smallVibrate(c);

		composedWord += c;
		mCandidateView.getSuggestionsForWord(composedWord);
		mTipeService.setCandidatesViewShown(true);

		mTipeService.getCurrentInputConnection().commitText("" + c, 1);

		/*
		 * Handle capitalization okay this is hardcoded.. but i want to test
		 * this feature fore now :)
		 */
		if (KeyboardHandler.use_auto_capitalization
				&& (c == '.' || c == '!' || c == '?')) {
			KeyboardHandler.shift_state = true;
			KeyboardHandler.handleShift();
		} else {
			if (!KeyboardHandler.word_separators.contains("" + c)) {
				KeyboardHandler.shift_state = false;
				KeyboardHandler.handleShift();
			}
		}
	}

	public void handleSpace() {
		smallVibrate();

		resetComposedWord();
		sendKey((char) 32);

	}

	public void handleDelete() {
		// Match composed word
		smallVibrate();

		if (composedWord.length() <= 1)
			composedWord = "";
		else
			composedWord = composedWord.substring(0, composedWord.length() - 1);
		mCandidateView.getSuggestionsForWord(composedWord);
		if (composedWord.isEmpty())
			mTipeService.setCandidatesViewShown(false);
		else
			mTipeService.setCandidatesViewShown(true);

		keyDownUp(KeyEvent.KEYCODE_DEL);
	}

	public void handleEnter() {
		smallVibrate();

		resetComposedWord();
		keyDownUp(KeyEvent.KEYCODE_ENTER);
	}

	/**
	 * use this for enter & delete key!
	 * 
	 * @param keyEventCode
	 */
	private void keyDownUp(int keyEventCode) {
		mTipeService.getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		mTipeService.getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	/*------------------------------------------------------------
	 *	From here functions for candidate view
	 *	@Jakob Frick
	 *------------------------------------------------------------*/

	public CandidateView initCandidateView(InputMethodService ims) {
		mCandidateView = new CandidateView(ims);
		mCandidateView.setInputHandler(this);
		resetComposedWord();

		return mCandidateView;
	}

	private void resetComposedWord() {
		composedWord = "";
		mTipeService.setCandidatesViewShown(false);
	}

	public void getSuggestionFromCandView(String suggestion) {
		InputConnection ic = mTipeService.getCurrentInputConnection();

		ic.deleteSurroundingText(composedWord.length(), 0);
		ic.commitText(suggestion + " ", composedWord.length() + 1);
		resetComposedWord();
	}

	/*
	 * This should be somewhere else :)
	 */
	public void smallVibrate() {
		if (KeyboardHandler.use_haptic_feedback) {
			mTipeService.getCurVibrator().vibrate(30);
		}
	}

	public void smallVibrate(int length) {
		if (KeyboardHandler.use_haptic_feedback) {
			mTipeService.getCurVibrator().vibrate(length);
		}
	}

}
