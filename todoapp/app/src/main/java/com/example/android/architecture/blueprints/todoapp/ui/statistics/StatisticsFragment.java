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

package com.example.android.architecture.blueprints.todoapp.ui.statistics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskFetcher;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.ui.BaseActivity;
import com.example.android.architecture.blueprints.todoapp.ui.widget.ScrollChildSwipeRefreshLayout;

import co.early.fore.core.observer.Observer;
import co.early.fore.core.ui.SyncableView;

/**
 * Main UI for the statistics screen.
 */
public class StatisticsFragment extends Fragment implements SyncableView {

    //models
    private TaskListModel taskListModel;
    private TaskFetcher taskFetcher;

    // UI elements
    private TextView mStatisticsTV;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    //single observer reference
    private Observer observer = this::syncView;



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
        taskFetcher = App.get(TaskFetcher.class);
        taskListModel = App.get(TaskListModel.class);
    }

    private View setupUiReferences(LayoutInflater inflater, ViewGroup container) {

        View root = inflater.inflate(R.layout.statistics_frag, container, false);

        mStatisticsTV = root.findViewById(R.id.statistics);
        // Set up progress indicator
        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        return root;
    }

    private void setupClickListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> taskFetcher.fetchTaskItems(
                () -> {
                },//success is no op, but maybe you would want to move to another activity etc (observers handle UI updates)
                failureMessage -> ((BaseActivity)getContext()).showMessage(failureMessage.getString(getResources()))));
    }

    @Override
    public void onResume() {
        super.onResume();
        taskFetcher.addObserver(observer);
        taskListModel.addObserver(observer);
        syncView();
    }

    @Override
    public void onPause() {
        super.onPause();
        taskFetcher.removeObserver(observer);
        taskListModel.removeObserver(observer);
    }

    @Override
    public void syncView() {
        mStatisticsTV.setText(
                taskListModel.getAllTasksCount() == 0 ?
                    getResources().getString(R.string.statistics_no_tasks) :
                    getResources().getString(R.string.statistics_active_tasks) + " "
                    + taskListModel.getActiveTasksCount() + "\n" + getResources().getString(
                    R.string.statistics_completed_tasks) + " " + taskListModel.getCompletedTasksCount());
        swipeRefreshLayout.setRefreshing(taskFetcher.isBusy());
    }
}
