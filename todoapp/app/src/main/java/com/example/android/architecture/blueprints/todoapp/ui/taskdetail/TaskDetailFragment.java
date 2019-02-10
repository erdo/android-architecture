/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.ui.taskdetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.ui.addedit.AddEditTaskActivity;

import co.early.fore.core.observer.Observer;
import co.early.fore.core.ui.SyncableView;

/**
 * Main UI for the task detail screen.
 */
public class TaskDetailFragment extends Fragment implements SyncableView {

    //models
    private CurrentTaskModel currentTaskModel;

    //UI elements
    private TextView mDetailTitle;
    private TextView mDetailDescription;
    private CheckBox mDetailCompleteStatus;
    private FloatingActionButton fab;

    //single observer reference
    private Observer observer = this::syncView;


    public static TaskDetailFragment newInstance() {
        return new TaskDetailFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setupModelReferences();

        View root = setupUiReferences(inflater, container);

        setupClickListeners();

        return root;
    }

    private void setupModelReferences(){
        currentTaskModel = App.inst().getAppComponent().getCurrentTaskModel();
    }

    private View setupUiReferences(LayoutInflater inflater, ViewGroup container) {

        View root = inflater.inflate(R.layout.taskdetail_frag, container, false);

        mDetailTitle = root.findViewById(R.id.task_detail_title);
        mDetailDescription = root.findViewById(R.id.task_detail_description);
        mDetailCompleteStatus = root.findViewById(R.id.task_detail_complete);

        // Set up floating action button
        fab = getActivity().findViewById(R.id.fab_edit_task);

        return root;
    }

    private void setupClickListeners(){
        fab.setOnClickListener(v -> AddEditTaskActivity.startEditActivityForResult(getActivity()));
        mDetailCompleteStatus.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (currentTaskModel.setCompleted(isChecked)) {
                        ((TaskDetailActivity) getActivity()).showMessage(getString(
                                isChecked ? R.string.task_marked_complete : R.string.task_marked_active));
                    }
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

        mDetailTitle.setVisibility(currentTaskModel.itemLoaded() ? View.VISIBLE : View.GONE);
        mDetailCompleteStatus.setVisibility(currentTaskModel.itemLoaded() ? View.VISIBLE : View.GONE);

        mDetailTitle.setText(currentTaskModel.getTitle());
        mDetailDescription.setText(currentTaskModel.isLoading() ? getString(R.string.loading) : currentTaskModel.getDescription());
        mDetailCompleteStatus.setChecked(currentTaskModel.isCompleted());
    }
}
