package com.example.android.architecture.blueprints.todoapp.ui.addedit;

import android.text.Editable;
import android.text.TextWatcher;

import co.early.fore.core.Affirm;
import co.early.fore.core.ui.SyncableView;


public class SyncerTextWatcher implements TextWatcher {

    private final SyncableView syncableView;
    private final TextChangedListener textChangedListener;

    public SyncerTextWatcher(TextChangedListener textChangedListener, SyncableView syncableView) {
        this.textChangedListener = Affirm.notNull(textChangedListener);
        this.syncableView = Affirm.notNull(syncableView);
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
        textChangedListener.changed(s);
        syncableView.syncView();
    }

    public interface TextChangedListener {
        void changed(Editable newText);
    }
}

