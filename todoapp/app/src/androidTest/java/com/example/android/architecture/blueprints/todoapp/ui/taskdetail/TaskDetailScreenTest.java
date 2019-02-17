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

package com.example.android.architecture.blueprints.todoapp.ui.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestAppModule;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static co.early.fore.core.testhelpers.CountDownLatchWrapper.runInBatch;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TaskDetailScreenTest {

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target Activity.
     */
    @Rule
    public ActivityTestRule<TaskDetailActivity> mTaskDetailActivityTestRule =
            new ActivityTestRule<TaskDetailActivity>(TaskDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                }
            };

    @Before
    public void setup(){
        App.inst().injectTestAppModule(new TestAppModule(App.inst()));
        TaskListModel taskListModel = App.inst().getAppComponent().getTaskListModel();

        runInBatch(3, taskListModel, () -> { //this makes sure Room's invalidationTracker has fired before we continue
            taskListModel.clear();
            taskListModel.add(TestAppModule.LOCAL_TASKITEM_0_ACTIVE);
            taskListModel.add(TestAppModule.LOCAL_TASKITEM_1_COMPLETE);
        });

        runInBatch(1, taskListModel, () -> {
            taskListModel.setCompleted(true, 0);
        });
    }

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(EspressoIdlingResource.getIdlingResource());
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(EspressoIdlingResource.getIdlingResource());
    }


    private void loadActiveTask() {
        TaskListModel taskListModel = App.inst().getAppComponent().getTaskListModel();
        CurrentTaskModel currentTaskModel = App.inst().getAppComponent().getCurrentTaskModel();
        currentTaskModel.loadTask(taskListModel.get(1).getEntityId());

        // Lazily start the Activity from the ActivityTestRule
        mTaskDetailActivityTestRule.launchActivity(null);
    }

    private void loadCompletedTask() {
        TaskListModel taskListModel = App.inst().getAppComponent().getTaskListModel();
        CurrentTaskModel currentTaskModel = App.inst().getAppComponent().getCurrentTaskModel();
        currentTaskModel.loadTask(taskListModel.get(0).getEntityId());

        // Lazily start the Activity from the ActivityTestRule
        mTaskDetailActivityTestRule.launchActivity(null);
    }


    @Test
    public void activeTaskDetails_DisplayedInUi() throws Exception {
        loadActiveTask();

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TestAppModule.LOCAL_TASKITEM_0_ACTIVE.getTitle())));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TestAppModule.LOCAL_TASKITEM_0_ACTIVE.getDescription())));
        onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedTaskDetails_DisplayedInUi() throws Exception {
        loadCompletedTask();

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TestAppModule.LOCAL_TASKITEM_1_COMPLETE.getTitle())));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TestAppModule.LOCAL_TASKITEM_1_COMPLETE.getDescription())));
        onView(withId(R.id.task_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndTaskPersist() {
        loadActiveTask();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mTaskDetailActivityTestRule.getActivity());

        // Check that the task is shown
        onView(withId(R.id.task_detail_title)).check(matches(withText(TestAppModule.LOCAL_TASKITEM_0_ACTIVE.getTitle())));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TestAppModule.LOCAL_TASKITEM_0_ACTIVE.getDescription())));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

}
