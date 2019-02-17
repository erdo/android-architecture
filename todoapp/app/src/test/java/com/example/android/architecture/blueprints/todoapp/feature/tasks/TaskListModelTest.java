package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemDatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.logging.SystemLogger;
import co.early.fore.core.time.SystemTimeWrapper;

import static co.early.fore.core.testhelpers.CountDownLatchWrapper.runInBatch;

/**
 * Integration test which demonstrates how to test db driven models
 * using a real in memory database rather than a mock
 *
 * We need to use count down latches here because Room's invalidation tracker
 * always fires in a different thread
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class TaskListModelTest {

    @Mock
    private SystemTimeWrapper mockSystemTimeWrapper;

    private TaskItemDatabase taskItemDatabase;
    private WorkMode workMode = WorkMode.SYNCHRONOUS;
    private Logger logger = new SystemLogger();

    private static final TaskItem TASK_ITEM_0 = new TaskItem(0, "buy rice", "");
    private static final TaskItem TASK_ITEM_1 = new TaskItem(1, "get hair cut", "");
    private static final TaskItem TASK_ITEM_2 = new TaskItem(2, "invest in bitcoin", "");

    private static final String NEW_TITLE = "learn to cook";

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        //real in memory db which allows main thread queries
       // taskItemDatabase = TaskItemDatabase.getInstance((Application)InstrumentationRegistry.getTargetContext(), true, workMode);
        taskItemDatabase = TaskItemDatabase.getInstance(RuntimeEnvironment.application, true, workMode);
    }

    @After
    public void tearDown() {
        if (taskItemDatabase.isOpen()) {
            taskItemDatabase.getOpenHelper().close();
        }
        taskItemDatabase.destroyInstance();
    }

    @Test
    public void whenInitialised_withNoData_stateIsCorrect() throws Exception {

        //arrange
        TaskListModel taskListModel = new TaskListModel(taskItemDatabase, logger, mockSystemTimeWrapper, workMode);

        //act

        //assert
        Assert.assertEquals(0, taskListModel.size());
    }

    @Test
    public void whenQueryingTodoItems_withTodoItemsAdded_todoItemsAreCorrect() throws Exception {

        //arrange
        TaskListModel taskListModel = new TaskListModel(taskItemDatabase, logger, mockSystemTimeWrapper, workMode);

        //the Room invalidation tracker fires in a different thread
        CountDownLatch latchForRoomInvalidationTracker = new CountDownLatch(3);
        taskListModel.addObserver(() -> latchForRoomInvalidationTracker.countDown());


        //act
        taskListModel.add(TASK_ITEM_0);
        taskListModel.add(TASK_ITEM_1);
        taskListModel.add(TASK_ITEM_2);


        //Try to ensure all the invalidation trackers have been fired before we continue.
        //In reality, Room batches up the invalidation trackers so we can't be deterministic
        //about how many we will receive, hence the 2s timeout
        latchForRoomInvalidationTracker.await(2, TimeUnit.SECONDS);


        //assert
        Assert.assertEquals(3, taskListModel.size());
        Assert.assertEquals(2, taskListModel.get(0).getCreationTimestamp());
        Assert.assertEquals(1, taskListModel.get(1).getCreationTimestamp());
        Assert.assertEquals(0, taskListModel.get(2).getCreationTimestamp());
    }

    @Test
    public void whenQueryingTodoItems_withTodoItemsAddedAndRemoved_todoItemsAreCorrect() throws Exception {

        //arrange
        TaskListModel taskListModel = new TaskListModel(taskItemDatabase, logger, mockSystemTimeWrapper, workMode);


        //act
        runInBatch(2, taskListModel, () -> {
            taskListModel.add(TASK_ITEM_0);
            taskListModel.add(TASK_ITEM_1);
        });

        //act
        runInBatch(1, taskListModel, () -> {
            taskListModel.remove(taskListModel.get(0));
        });


        //assert
        Assert.assertEquals(1, taskListModel.size());
        Assert.assertEquals(0, taskListModel.get(0).getCreationTimestamp());
    }

    @Test
    public void whenQueryingTodoItems_withTodoItemsAddedAndChanged_todoItemsAreCorrect() throws Exception {

        //arrange
        TaskListModel taskListModel = new TaskListModel(taskItemDatabase, logger, mockSystemTimeWrapper, workMode);


        //act
        runInBatch(1, taskListModel, () -> {
            taskListModel.add(TASK_ITEM_0);
        });

        //act
        runInBatch(1, taskListModel, () -> {
            TaskItem taskItem = taskListModel.get(0);
            taskItem.setTitle(NEW_TITLE);
            taskListModel.update(taskItem);
        });


        //assert
        Assert.assertEquals(1, taskListModel.size());
        Assert.assertEquals(0, taskListModel.get(0).getCreationTimestamp());
        Assert.assertEquals(NEW_TITLE, taskListModel.get(0).getTitle());
    }

    @Test
    public void whenQueryingTodoItems_withTodoItemsAddedAndCleared_todoItemsAreCorrect() throws Exception {

        //arrange
        TaskListModel taskListModel = new TaskListModel(taskItemDatabase, logger, mockSystemTimeWrapper, workMode);


        //act
        runInBatch(2, taskListModel, () -> {
            taskListModel.add(TASK_ITEM_0);
            taskListModel.add(TASK_ITEM_1);
        });


        //act
        runInBatch(1, taskListModel, () -> {
            taskListModel.clear();
        });


        //assert
        Assert.assertEquals(0, taskListModel.size());
    }

    @Test
    public void whenTodoItemIsMarkedAsDone__todoItemsIsRemovedFromList() throws Exception {

        //arrange
        TaskListModel taskListModel = new TaskListModel(taskItemDatabase, logger, mockSystemTimeWrapper, workMode);
        taskListModel.setFilter(Filter.ACTIVE);

        //act
        runInBatch(3, taskListModel, () -> {
            taskListModel.add(TASK_ITEM_0);
            taskListModel.add(TASK_ITEM_1);
            taskListModel.add(TASK_ITEM_2);
        });

        //act
        runInBatch(1, taskListModel, () -> {
            TaskItem taskItem = taskListModel.get(1);
            taskItem.setCompleted(true);
            taskListModel.update(taskItem);
        });


        //assert
        Assert.assertEquals(2, taskListModel.size());
        Assert.assertEquals(2, taskListModel.get(0).getCreationTimestamp());
        Assert.assertEquals(0, taskListModel.get(1).getCreationTimestamp());
    }
}
