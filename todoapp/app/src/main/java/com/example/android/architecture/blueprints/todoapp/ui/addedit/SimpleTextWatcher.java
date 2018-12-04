package com.example.android.architecture.blueprints.todoapp.ui.addedit;

import android.text.Editable;
import android.text.TextWatcher;

import co.early.fore.core.Affirm;



public class SimpleTextWatcher implements TextWatcher {

    private final TextChangedListener textChangedListener;

    public SimpleTextWatcher(TextChangedListener textChangedListener) {
        this.textChangedListener = Affirm.notNull(textChangedListener);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //no opp
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //no opp
    }

    @Override
    public void afterTextChanged(Editable s) {
        textChangedListener.changed(s.toString());
    }

    public interface TextChangedListener {
        void changed(String newText);
    }
}

