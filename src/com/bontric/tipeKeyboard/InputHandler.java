/**
 *@name InputHandler
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Copyright  2014 Benedikt Wieder, Jakob Frick

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *
 */

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
    private boolean isComposing;

    public InputHandler() {

    }

    public void setIMS(TipeService tService) {
        mTipeService = tService;
    }

    public void sendKey(char c) {
        smallVibrate();
        mTipeService.getCurrentInputConnection().commitText("" + c, 1);
        if ((c == '.' || c == '!' || c == '?')) {

            if (KeyboardHandler.use_auto_capitalization) {
                KeyboardHandler.shift_state = true;
                KeyboardHandler.handleShift();
            }
            resetComposedWord();
            return;
        } else {
            if (!KeyboardHandler.word_separators.contains("" + c)) {
                KeyboardHandler.shift_state = false;
                KeyboardHandler.handleShift();
            }
        }

        if (isComposing) {
            composedWord += c;
            mCandidateView.getSuggestionsForWord(composedWord);
            mCandidateView.setVisibility(CandidateView.VISIBLE);
        }


    }

    public void handleSpace() {
        Log.d("main","ping");
        smallVibrate();
        if (mCandidateView.count() >= 3) {
            mCandidateView.pickSuggestions(1);
        } else {
            sendKey((char) 32);
        }
        resetComposedWord();
    }

    public void handleSpaceOnly() {
        sendKey((char) 32);
        resetComposedWord();
    }

    public void handleDelete() {
        // Match composed word
        smallVibrate();

        if (isComposing) {
            if (composedWord.length() <= 1) {
                composedWord = "";
                //mCandidateView.setVisibility(CandidateView.INVISIBLE);
            } else {
                composedWord = composedWord.substring(0, composedWord.length() - 1);
                mCandidateView.getSuggestionsForWord(composedWord);
            }
        }
        keyDownUp(KeyEvent.KEYCODE_DEL);
    }

    public boolean isComposing() {
        return isComposing;
    }

    public void setComposing(boolean isComposing) {
        this.isComposing = isComposing;
    }

    public void handleEnter() {
        smallVibrate();

        resetComposedWord();
        keyDownUp(KeyEvent.KEYCODE_ENTER);
    }

    /**
     * use this for enter & delete key!
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

        composedWord = "";
        isComposing = true;
        return mCandidateView;
    }


    public void resetComposedWord() {
        composedWord = "";
        if (mCandidateView != null) {
            mCandidateView.clear();
           // mCandidateView.setVisibility(CandidateView.INVISIBLE);
        }
    }


    public void setComposedWord(String soFarComposed) {
        composedWord = soFarComposed;
        mCandidateView.getSuggestionsForWord(composedWord);
        mCandidateView.setVisibility(CandidateView.VISIBLE);
    }

    public void getSuggestionFromCandView(String suggestion) {
        InputConnection ic = mTipeService.getCurrentInputConnection();
        ic.deleteSurroundingText(composedWord.length(), 0);
        ic.commitText(suggestion + (char) 32, suggestion.length() + 1);
        resetComposedWord();
    }

    /*
     * This could be somewhere else :)
     */
    void smallVibrate() {
        if (KeyboardHandler.use_haptic_feedback) {
            mTipeService.getCurVibrator().vibrate(30);
        }
    }



}
