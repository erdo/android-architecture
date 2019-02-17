package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemPojo;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.early.fore.core.WorkMode;
import co.early.fore.core.callbacks.FailureCallbackWithPayload;
import co.early.fore.core.callbacks.SuccessCallback;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.logging.SystemLogger;
import co.early.fore.core.observer.Observer;
import co.early.fore.core.time.SystemTimeWrapper;
import co.early.fore.retrofit.CallProcessor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Tests for this model cover a few areas:
 * <p>
 * 1) Construction: we check that the model is constructed in the correct state
 * 2) Receiving data: we check that the model behaves appropriately when receiving various success and fail responses from the CallProcessor
 * 3) Observers and State: we check that the model updates its observers correctly and presents it's current state accurately
 *
 */
public class TaskFetcherUnitTest {

    public static final String LOG_TAG = TaskFetcherIntegrationTest.class.getSimpleName();

    private static Logger logger = new SystemLogger();


    private static final String TITLE = "task title";
    private static final String DESCRIPTION = "task description";

    private static final List<TaskItemPojo> TASK_POJOS_FROM_SERVER = Stream.of(
            new TaskItemPojo(TITLE, DESCRIPTION, false))
            .collect(Collectors.toList());

    private static final List<TaskItem> TASK_ITEMS_FROM_MODEL = Stream.of(
            new TaskItem(0, TITLE, DESCRIPTION))
            .collect(Collectors.toList());


    @Mock
    private SuccessCallback mockSuccessCallback;
    @Mock
    private FailureCallbackWithPayload mockFailureCallbackWithPayload;
    @Mock
    private TaskListModel mockTaskListModel;
    @Mock
    private SystemTimeWrapper mockSystemTimeWrapper;
    @Mock
    private CallProcessor<UserMessage> mockCallProcessor;
    @Mock
    private TaskItemService mockTaskItemService;
    @Mock
    private Observer mockObserver;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void initialConditions() throws Exception {

        //arrange
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                mockTaskItemService,
                mockCallProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);

        //act

        //assert
        Assert.assertEquals(false, fetcher.isBusy());
    }


    @Test
    public void fetchTasks_MockSuccess() throws Exception {

        //arrange
        new StateBuilder(mockCallProcessor)
                .getTasksSuccess(TASK_POJOS_FROM_SERVER);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                mockTaskItemService,
                mockCallProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);


        //act
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);


        //assert
        verify(mockSuccessCallback, times(1)).success();
        verify(mockFailureCallbackWithPayload, never()).fail(any());
        verify(mockTaskListModel, Mockito.times(1)).addManyFilterOutDuplicates(TASK_ITEMS_FROM_MODEL);
        Assert.assertEquals(false, fetcher.isBusy());
    }


    @Test
    public void fetchTasks_MockFailure() throws Exception {

        //arrange
        new StateBuilder(mockCallProcessor)
                .getTasksFail(UserMessage.ERROR_MISC);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                mockTaskItemService,
                mockCallProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);


        //act
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);


        //assert
        verify(mockSuccessCallback, never()).success();
        verify(mockFailureCallbackWithPayload, times(1)).fail(UserMessage.ERROR_MISC);
        verify(mockTaskListModel, never()).addManyFilterOutDuplicates(any());
        Assert.assertEquals(false, fetcher.isBusy());
    }


    /**
     *
     * NB all we are checking here is that observers are called AT LEAST once
     *
     * We don't really want tie our tests (OR any observers in production code)
     * to an expected number of times this method might be called. (This would be
     * testing an implementation detail and make the tests unnecessarily brittle)
     *
     * The contract says nothing about how many times the observers will get called,
     * only that they will be called if something changes ("something" is not defined
     * and can change between implementations).
     *
     * See the fore docs for more information about this: https://erdo.github.io/android-fore/05-extras.html#notification-counting
     *
     * @throws Exception
     */
    @Test
    public void observersNotifiedAtLeastOnce() throws Exception {

        //arrange
        new StateBuilder(mockCallProcessor)
                .getTasksFail(UserMessage.ERROR_MISC);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                mockTaskItemService,
                mockCallProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);


        //act
        fetcher.addObserver(mockObserver);
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);

        //assert
        verify(mockObserver, atLeastOnce()).somethingChanged();
    }


}
