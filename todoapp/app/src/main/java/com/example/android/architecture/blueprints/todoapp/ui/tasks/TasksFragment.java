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
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskFetcher;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.ui.addedit.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.ui.widget.ScrollChildSwipeRefreshLayout;

import co.early.fore.core.observer.Observer;
import co.early.fore.core.ui.SyncableView;

/**
 * Display a list of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment implements SyncableView {

    //models
    private TaskListModel taskListModel;
    private TaskFetcher taskFetcher;

    //UI elements

    private TasksAdapter mListAdapter;

    private View mNoTasksView;

    private ImageView mNoTaskIcon;

    private TextView mNoTaskMainView;

    private TextView mNoTaskAddView;

    private LinearLayout mTasksView;

    private TextView mFilteringLabelView;

    private RecyclerView listView;

    private FloatingActionButton fab;

    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    //single observer reference
    Observer observer = this::syncView;


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
        taskListModel = App.get(TaskListModel.class);
        taskFetcher = App.get(TaskFetcher.class);
    }

    private View setupUiReferences(LayoutInflater inflater, ViewGroup container) {

        View root = inflater.inflate(R.layout.tasks_frag, container, false);

        // Set up tasks view
        listView = root.findViewById(R.id.tasks_list);
        mFilteringLabelView = root.findViewById(R.id.filteringLabel);
        mTasksView = root.findViewById(R.id.tasksLL);

        // Set up  no tasks view
        mNoTasksView = root.findViewById(R.id.noTasks);
        mNoTaskIcon = root.findViewById(R.id.noTasksIcon);
        mNoTaskMainView = root.findViewById(R.id.noTasksMain);
        mNoTaskAddView = root.findViewById(R.id.noTasksAdd);

        // Set up floating action button
        fab = getActivity().findViewById(R.id.fab_add_task);
        fab.setImageResource(R.drawable.ic_add);

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

        mListAdapter = new TasksAdapter(taskListModel, new TasksAdapter.TaskActionsCallBack() {
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
        listView.setAdapter(mListAdapter);
    }

    private void setupClickListeners(){

        mNoTaskAddView.setOnClickListener(v -> AddEditTaskActivity.startActivityForResult(getActivity()));

        fab.setOnClickListener(v -> AddEditTaskActivity.startActivityForResult(getActivity()));

        swipeRefreshLayout.setOnRefreshListener(() -> taskFetcher.fetchTaskItems(
                () -> {},//success is no op, but maybe you would want to move to another activity etc
                failureMessage -> ((TasksActivity)getContext()).showMessage(failureMessage.getString(getResources()))));
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

        mTasksView.setVisibility(taskListModel.hasVisibleTasks() ? View.VISIBLE :View.GONE);

        mNoTasksView.setVisibility(taskListModel.hasVisibleTasks() ? View.GONE :View.VISIBLE);
        mNoTaskMainView.setText(taskListModel.getCurrentFilter().noTasksStringResId);
        mNoTaskIcon.setImageDrawable(getResources().getDrawable(taskListModel.getCurrentFilter().noTasksDrawableResId));
        mNoTaskAddView.setVisibility(taskListModel.hasVisibleTasks() ? View.GONE : View.VISIBLE);

        mFilteringLabelView.setText(getResources().getString(taskListModel.getCurrentFilter().labelStringResId));
        swipeRefreshLayout.setRefreshing(taskFetcher.isBusy());

        mListAdapter.notifyDataSetChanged();
    }
}
