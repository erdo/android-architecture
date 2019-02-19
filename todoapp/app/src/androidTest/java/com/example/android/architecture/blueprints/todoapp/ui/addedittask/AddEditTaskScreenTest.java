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

package com.example.android.architecture.blueprints.todoapp.ui.addedittask;

import android.content.Intent;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestAppModule;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.ui.addedit.AddEditTaskActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static co.early.fore.core.testhelpers.CountDownLatchWrapper.runInBatch;
import static com.example.android.architecture.blueprints.todoapp.R.id.toolbar;

/**
 * Tests for the add task screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEditTaskScreenTest {


    /**
     * {@link IntentsTestRule} is an {@link ActivityTestRule} which inits and releases Espresso
     * Intents before and after each test run.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<AddEditTaskActivity> mActivityTestRule =
            new ActivityTestRule<AddEditTaskActivity>(AddEditTaskActivity.class, false, false);

    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    public void setup() {
        App.inst().injectTestAppModule(new TestAppModule(App.inst()));
        App.inst().getAppComponent().getTaskListModel().clear();
    }

    @Test
    public void emptyTask_isNotSaved() {
        // Launch activity to add a new task
        launchNewTaskActivity(false);

        // Add invalid title and description combination
        onView(withId(R.id.add_task_title)).perform(clearText());
        onView(withId(R.id.add_task_description)).perform(clearText());
        // Try to save the task
        onView(withId(R.id.fab_edit_task_done)).perform(click());

        // Verify that the activity is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()));
    }

    @Test
    public void toolbarTitle_newTask_persistsRotation() {
        // Launch activity to add a new task
        launchNewTaskActivity(false);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_task)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // Check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.add_task)));
    }

    @Test
    public void toolbarTitle_editTask_persistsRotation() {
        // Put a task in the repository and start the activity to edit it
        TaskListModel taskListModel = App.inst().getAppComponent().getTaskListModel();
        CurrentTaskModel currentTaskModel = App.inst().getAppComponent().getCurrentTaskModel();

        runInBatch(1, taskListModel, () -> { //this makes sure Room's invalidationTracker has fired before we continue
            taskListModel.add(TestAppModule.LOCAL_TASKITEM_0_ACTIVE);
        });

        currentTaskModel.loadTask(taskListModel.get(0).getEntityId());

        launchNewTaskActivity(true);

        // Check that the toolbar shows the correct title
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_task)));

        // Rotate activity
        TestUtils.rotateOrientation(mActivityTestRule.getActivity());

        // check that the toolbar title is persisted
        onView(withId(toolbar)).check(matches(withToolbarTitle(R.string.edit_task)));
    }

    private void launchNewTaskActivity(boolean edit) {

        Intent intent = AddEditTaskActivity.buildForTask(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                edit);

        mActivityTestRule.launchActivity(intent);
    }

    /**
     * Matches the toolbar title with a specific string resource.
     *
     * @param resourceId the ID of the string resource to match
     */
    public static Matcher<View> withToolbarTitle(final int resourceId) {
        return new BoundedMatcher<View, Toolbar>(Toolbar.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with toolbar title from resource id: ");
                description.appendValue(resourceId);
            }

            @Override
            protected boolean matchesSafely(Toolbar toolbar) {
                CharSequence expectedText = "";
                try {
                    expectedText = toolbar.getResources().getString(resourceId);
                } catch (Resources.NotFoundException ignored) {
                    /* view could be from a context unaware of the resource id. */
                }
                CharSequence actualText = toolbar.getTitle();
                return expectedText.equals(actualText);
            }
        };
    }
}
