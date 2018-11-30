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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.ui.BaseActivity;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

/**
 * Displays an add or edit task screen.
 */
public class AddEditTaskActivity extends BaseActivity {

    public static final int REQUEST_CODE_EDIT_TASK = 1;
    public static final int REQUEST_CODE_ADD_TASK = 2;

    private static final String EXTRA_KEY_EDITING = "editing";

    //models
    private CurrentTaskModel currentTaskModel;

    private ActionBar mActionBar;

    public static void startEditActivityForResult(Activity activity) {
        Intent intent = buildForTask(activity, true);
        activity.startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
    }

    public static void startAddActivityForResult(Activity activity) {
        Intent intent = buildForTask(activity, false);
        activity.startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
    }

    public static Intent buildForTask(Context context, boolean editing) {
        Intent intent = new Intent(context, AddEditTaskActivity.class);
        intent.putExtra(EXTRA_KEY_EDITING, editing);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addtask_act);

        setupModelReferences();

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);


        boolean editing = getIntent().getBooleanExtra(EXTRA_KEY_EDITING, false);

        mActionBar.setTitle(editing ? R.string.edit_task : R.string.add_task);

        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (addEditTaskFragment == null) {

            addEditTaskFragment = editing ? AddEditTaskFragment.newEditInstance() : AddEditTaskFragment.newAddInstance();

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addEditTaskFragment, R.id.contentFrame);
        }
    }

    private void setupModelReferences(){
        currentTaskModel = App.get(CurrentTaskModel.class);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        currentTaskModel.revertUnsavedChanges();
    }
}
