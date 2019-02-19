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

package com.example.android.architecture.blueprints.todoapp.ui.tasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskFetcher;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskItem;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.ui.BaseActivity;
import com.example.android.architecture.blueprints.todoapp.ui.addedit.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.ui.widget.ScrollChildSwipeRefreshLayout;

import co.early.fore.core.observer.Observer;
import co.early.fore.core.ui.SyncableView;

/**
 * Display a list of {@link TaskItem}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment implements SyncableView {

    //models
    private TaskListModel taskListModel;
    private TaskFetcher taskFetcher;
    private CurrentTaskModel currentTaskModel;

    //UI elements
    private TasksAdapter listAdapter;
    private View noTasksView;
    private ImageView noTaskIcon;
    private TextView noTaskMsg;
    private TextView noTaskAddView;
    private LinearLayout tasksView;
    private TextView filteringLabelView;
    private RecyclerView listView;
    private FloatingActionButton fab;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    //single observer reference
    private Observer observer = this::syncView;


    public static TasksFragment newInstance() {
        return new TasksFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setupModelReferences();

        View root = setupUiReferences(inflater, container);

        setupAdapter();

        setupClickListeners();

        return root;
    }

    private void setupModelReferences(){
        taskListModel = App.inst().getAppComponent().getTaskListModel();
        taskFetcher = App.inst().getAppComponent().getTaskFetcher();
        currentTaskModel = App.inst().getAppComponent().getCurrentTaskModel();
    }

    private View setupUiReferences(LayoutInflater inflater, ViewGroup container) {

        View root = inflater.inflate(R.layout.tasks_frag, container, false);

        // Set up tasks view
        listView = root.findViewById(R.id.tasks_list);
        filteringLabelView = root.findViewById(R.id.filteringLabel);
        tasksView = root.findViewById(R.id.tasksLL);

        // Set up  no tasks view
        noTasksView = root.findViewById(R.id.noTasks);
        noTaskIcon = root.findViewById(R.id.noTasksIcon);
        noTaskMsg = root.findViewById(R.id.noTasksMsg);
        noTaskAddView = root.findViewById(R.id.noTasksAdd);

        // Set up floating action button
        fab = getActivity().findViewById(R.id.fab_add_task);

        // Set up progress indicator
        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        return root;
    }

    private void setupAdapter(){

        listAdapter = new TasksAdapter(taskListModel, currentTaskModel, new TasksAdapter.TaskActionsCallBack() {
            @Override
            public void taskMarkedComplete() {
                ((TasksActivity)getContext()).showMessage(getString(R.string.task_marked_complete));
            }

            @Override
            public void taskMarkedActive() {
                ((TasksActivity)getContext()).showMessage(getString(R.string.task_marked_active));
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        listView.setLayoutManager(linearLayoutManager);
        listView.setAdapter(listAdapter);
    }

    private void setupClickListeners(){

        noTaskAddView.setOnClickListener(v -> {
            currentTaskModel.createNewEmptyTask();
            AddEditTaskActivity.startAddActivityForResult(getActivity());
        });

        fab.setOnClickListener(v -> {
            currentTaskModel.createNewEmptyTask();
            AddEditTaskActivity.startAddActivityForResult(getActivity());
        });

        swipeRefreshLayout.setOnRefreshListener(() -> taskFetcher.fetchTaskItems(
                () -> {},//success is no op, but maybe you would want to move to another activity etc (observers handle UI updates)
                failureMessage -> {
                    if (getContext() != null) {
                        ((BaseActivity) getContext()).showMessage(failureMessage.getString(getResources()));
                    }
                }));
    }

    //below makes the UI reactive

    @Override
    public void onResume() {
        super.onResume();
        taskListModel.addObserver(observer);
        taskFetcher.addObserver(observer);
        syncView();
    }

    @Override
    public void onPause() {
        super.onPause();
        taskListModel.removeObserver(observer);
        taskFetcher.removeObserver(observer);
    }

    @Override
    public void syncView() {

        tasksView.setVisibility(taskListModel.hasVisibleTasks() ? View.VISIBLE :View.GONE);

        noTasksView.setVisibility(taskListModel.hasVisibleTasks() ? View.GONE :View.VISIBLE);
        noTaskMsg.setText(taskListModel.getCurrentFilter().noTasksStringResId);
        noTaskIcon.setImageDrawable(getResources().getDrawable(taskListModel.getCurrentFilter().noTasksDrawableResId));
        noTaskAddView.setVisibility(taskListModel.hasVisibleTasks() ? View.GONE : View.VISIBLE);

        filteringLabelView.setText(getResources().getString(taskListModel.getCurrentFilter().labelStringResId));
        swipeRefreshLayout.setRefreshing(taskFetcher.isBusy());

        listAdapter.notifyDataSetChangedAuto();
    }
}
