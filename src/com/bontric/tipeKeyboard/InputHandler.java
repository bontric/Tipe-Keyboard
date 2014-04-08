package com.bontric.tipeKeyboard;

import android.view.KeyEvent;

public class InputHandler {

	private TipeService mTipeService;

	public InputHandler() {

	}

	public void setIMS(TipeService tService) {
		mTipeService = tService;
	}

	public void sendKey(char c) {
		
		mTipeService.getCurrentInputConnection().commitText("" + c, 1);
		KeyboardHandler.shiftState = false;
		KeyboardHandler.handleShift();
	}

	public void handleSpace() {
		sendKey((char) 32);
	}

	public void handleDelete() {
		keyDownUp(KeyEvent.KEYCODE_DEL);
	}

	public void handleEnter() {
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
	
	public void getSuggestionFromCandView(String suggestion){
		
	}
	
	
}
