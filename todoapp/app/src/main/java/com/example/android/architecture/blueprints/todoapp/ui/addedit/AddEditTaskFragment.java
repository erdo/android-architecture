/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.ui.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.ui.widget.CustomEditText;

import co.early.fore.core.observer.Observer;
import co.early.fore.core.ui.SyncableView;


/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
public class AddEditTaskFragment extends Fragment implements SyncableView {

    private static final String BUNDLE_KEY_EDITING = "editing";

    //models
    private CurrentTaskModel currentTaskModel;

    // UI elements
    private FloatingActionButton fab;
    private FloatingActionButton fabRevert;
    private CustomEditText mTitle;
    private CustomEditText mDescription;

    //single observer reference
    private Observer observer = this::syncView;

    private boolean editing;

    public static AddEditTaskFragment newEditInstance() {
        return newInstance(true);
    }

    public static AddEditTaskFragment newAddInstance() {
        return newInstance(false);
    }

    private static AddEditTaskFragment newInstance(boolean editing) {
        AddEditTaskFragment fragment = new AddEditTaskFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_KEY_EDITING, editing);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        editing = getArguments().getBoolean(BUNDLE_KEY_EDITING);

        setupModelReferences();

        View root = setupUiReferences(inflater, container);

        setupClickListeners();

        return root;
    }

    private void setupModelReferences(){
        currentTaskModel = App.get(CurrentTaskModel.class);
    }

    private View setupUiReferences(LayoutInflater inflater, ViewGroup container) {

        View root = inflater.inflate(R.layout.addtask_frag, container, false);

        mTitle = root.findViewById(R.id.add_task_title);
        mDescription = root.findViewById(R.id.add_task_description);

        // Set up floating action buttons
        fab = getActivity().findViewById(R.id.fab_edit_task_done);
        fabRevert = getActivity().findViewById(R.id.fab_edit_task_undo);

        return root;
    }

    private void setupClickListeners(){

        mTitle.addTextChangedListener(new SyncerTextWatcher(
                newText -> currentTaskModel.setTitle(newText.toString()), this));
        mDescription.addTextChangedListener(new SyncerTextWatcher(
                newText -> currentTaskModel.setDescription(newText.toString()), this));

        fab.setOnClickListener(v -> {
            currentTaskModel.saveChanges();
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        });

        fabRevert.setOnClickListener(v -> {
            currentTaskModel.revertUnsavedChanges();
        });
    }


    //below makes the UI reactive

    @Override
    public void onResume() {
        super.onResume();
        currentTaskModel.addObserver(observer);
        syncView();
    }

    @Override
    public void onPause() {
        super.onPause();
        currentTaskModel.removeObserver(observer);
    }

    @Override
    public void syncView() {

        mTitle.setTextIfDifferent(currentTaskModel.getTitle());
        mDescription.setTextIfDifferent(currentTaskModel.getDescription());

        fab.setImageResource(editing ? R.drawable.ic_save : R.drawable.ic_add);
        fabRevert.setVisibility(editing ? View.VISIBLE : View.INVISIBLE);

        fab.setEnabled(currentTaskModel.hasUnsavedChanges() && currentTaskModel.isValid());
        fabRevert.setEnabled(currentTaskModel.hasUnsavedChanges());
    }
}
