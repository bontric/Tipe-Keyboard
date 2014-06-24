/**
 *@name SoftKeyboard
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Implementation of the Input Method Service.
 * Handles the IME life cycle and text input.
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

import android.annotation.SuppressLint;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import com.bontric.tipeSettings.TipeSettings;

public class TipeService extends InputMethodService {

    static final boolean DEBUG = false;
    private Vibrator mVibrator;
    private TipeView mTipeView = null;

    private boolean showCandidates = true;

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
        mTipeView = (TipeView) this.getLayoutInflater().inflate(
                R.layout.tipe_view, null);
        if (KeyboardHandler.use_tap_tap_mode) {
            mTipeView = (TapTapView) this.getLayoutInflater().inflate(
                    R.layout.taptap_view, null);

        }

        return mTipeView;
    }

    public void onFinishInput() {
        if (KeyboardHandler.input_connection != null) {
            KeyboardHandler.input_connection.resetComposedWord();
        }
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

        KeyboardHandler.input_connection.setIMS(this);
        /*
         * well this should work.. documentation on this is kinda confusing..
		 */
        showCandidates = (attribute.inputType & EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != EditorInfo.TYPE_TEXT_VARIATION_PASSWORD &&
                KeyboardHandler.show_suggestions;

        Log.d("onStartInput", "Show canies " + showCandidates);
        if (!showCandidates) {
            this.setCandidatesViewShown(false);
            KeyboardHandler.input_connection.setComposing(false);
        } else {
            this.setCandidatesViewShown(true);
            KeyboardHandler.input_connection.setComposing(true);
        }

        mTipeView = (TipeView) this.getLayoutInflater().inflate(
                R.layout.tipe_view, null);
        if (KeyboardHandler.use_tap_tap_mode) {
            mTipeView = (TapTapView) this.getLayoutInflater().inflate(
                    R.layout.taptap_view, null);

        }


        this.setInputView(mTipeView);

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
        if (getCurrentInputConnection() == null || getCurrentInputConnection().getSelectedText(0) == "") {
            return;
        }
        // Check if text was selected
        if (newSelEnd - newSelStart > 0) {
            if (showCandidates) {
                CharSequence cs = getCurrentInputConnection().getSelectedText(0);
                if (cs == null) return;
                String s = cs.toString();
                KeyboardHandler.input_connection.setComposedWord(s);
            }
        } else {
            /*
            Check where the cursor is and which part of a word it is selecting.
            TODO Reconstruct the candidates with more possible situations and useful behavior in mind. @Me
             */

            if (getCurrentInputConnection() != null) {
                CharSequence csBefore = getCurrentInputConnection().getTextBeforeCursor(newSelStart, 0);
                CharSequence csAfter = getCurrentInputConnection().getTextAfterCursor(100, 0);// sorry for this, but this is temporary until I fix the whole thing
                if(csAfter == null || csAfter == null) return;

                String selected = Util.getWordBetweenSeperators(csBefore.toString(),csAfter.toString());
                KeyboardHandler.input_connection.beforeAfterLength = Util.getBeforeAfterLength(csBefore.toString(),csAfter.toString());

                if (showCandidates && selected != null) {
                    KeyboardHandler.input_connection.setComposedWord(selected);

                } else {
                    KeyboardHandler.input_connection.resetComposedWord();
                }
            }
        }

    }

    @Override
    public View onCreateCandidatesView() {
        mTipeView.initKeyboardHandler(this);
        return KeyboardHandler.input_connection.initCandidateView(this);
    }

    /*
     * Make sure keyboard is not covering any content. Ref.:
     * https://groups.google.com/forum/#!topic/android-developers/yp7c7zsUSlo
     */
    @Override
    public void onComputeInsets(InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
    }


}
