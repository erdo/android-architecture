package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.api.CustomGlobalErrorHandler;
import com.example.android.architecture.blueprints.todoapp.api.CustomRetrofitBuilder;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import co.early.fore.core.WorkMode;
import co.early.fore.core.callbacks.FailureCallbackWithPayload;
import co.early.fore.core.callbacks.SuccessCallback;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.logging.SystemLogger;
import co.early.fore.core.time.SystemTimeWrapper;
import co.early.fore.retrofit.CallProcessor;
import co.early.fore.retrofit.InterceptorLogging;
import co.early.fore.retrofit.testhelpers.InterceptorStubbedService;
import co.early.fore.retrofit.testhelpers.StubbedServiceDefinition;
import retrofit2.Retrofit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * This is a slightly more end-to-end style of test, but without actually connecting to a network
 * <p>
 * Using {@link InterceptorStubbedService} we
 * replace the server response with a canned response taken from static text files saved
 * in /resources. This all happens in OkHttp land so the model under test is not aware of any
 * difference.
 * <p>
 * As usual for tests, we setup the {@link CallProcessor} with {@link WorkMode#SYNCHRONOUS} so
 * that everything plays out in a single thread.
 * <p>
 * Here we are testing what happens to the model when the server returns: a successful response;
 * a failed response due to a 401; and a failed response because the server responded with some
 * erroneous HTML 404 page
 */
public class TaskFetcherIntegrationTest {

    public static final String LOG_TAG = TaskFetcherIntegrationTest.class.getSimpleName();

    private Logger logger = new SystemLogger();
    private InterceptorLogging interceptorLogging;
    private CallProcessor<UserMessage> callProcessor;

    @Mock
    private SuccessCallback mockSuccessCallback;
    @Mock
    private FailureCallbackWithPayload mockFailureCallbackWithPayload;
    @Mock
    private TaskListModel mockTaskListModel;
    @Mock
    private SystemTimeWrapper mockSystemTimeWrapper;


    private static StubbedServiceDefinition<List<TaskItem>> stubbedSuccess = new StubbedServiceDefinition<>(
            200, //stubbed HTTP code
            "tasks/success.json", //stubbed body response
            StateBuilder.TASK_ITEMS); //expected result

    private static StubbedServiceDefinition<UserMessage> stubbedFailEmpty = new StubbedServiceDefinition<>(
            200, //stubbed HTTP code
            "common/empty.json", //stubbed body response
            UserMessage.ERROR_NETWORK); //expected result

    private static StubbedServiceDefinition<UserMessage> stubbedFailHtml = new StubbedServiceDefinition<>(
            404, //stubbed HTTP code
            "common/html_404.json", //stubbed body response
            UserMessage.ERROR_SERVER); //expected result

    private static StubbedServiceDefinition<UserMessage> stubbedFailSessionTimeout = new StubbedServiceDefinition<>(
            401, //stubbed HTTP code
            "common/session_token_invalid.json", //stubbed body response
            UserMessage.ERROR_SESSION_TIMED_OUT); //expected result


    @Before
    public void setup(){

        MockitoAnnotations.initMocks(this);

        interceptorLogging = new InterceptorLogging(logger);
        callProcessor = new CallProcessor<UserMessage>(new CustomGlobalErrorHandler(logger), logger);
    }


    /**
     * Here we are making sure that the model correctly handles a successful server response
     * containing a list of tasks
     *
     * @throws Exception
     */
    @Test
    public void fetchTasks_Success() throws Exception {

        //arrange
        Retrofit retrofit = stubbedRetrofit(stubbedSuccess);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                retrofit.create(TaskItemService.class),
                callProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);

        //act
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);

        //assert
        verify(mockSuccessCallback, times(1)).success();
        verify(mockFailureCallbackWithPayload, never()).fail(any());
        verify(mockTaskListModel, times(1)).addManyFilterOutDuplicates(argThat(new StateBuilder.MatchesTasksFromServer(logger, LOG_TAG)));
        Assert.assertEquals(false, fetcher.isBusy());
    }


    /**
     * Here we are making sure that the model correctly handles an empty server response
     *
     * @throws Exception
     */
    @Test
    public void fetchTasks_Fail_EmptyResponse() throws Exception {

        //arrange
        Retrofit retrofit = stubbedRetrofit(stubbedFailEmpty);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                retrofit.create(TaskItemService.class),
                callProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);


        //act
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);


        //assert
        verify(mockSuccessCallback, never()).success();
        verify(mockFailureCallbackWithPayload, times(1)).fail(eq(stubbedFailEmpty.expectedResult));
        verify(mockTaskListModel, never()).addManyFilterOutDuplicates(any());
        Assert.assertEquals(false, fetcher.isBusy());
    }


    /**
     * Here we are making sure that the model correctly handles a messed up html 404 server response
     *
     * @throws Exception
     */
    @Test
    public void fetchTasks_Fail_HtmlResponse() throws Exception {

        //arrange
        Retrofit retrofit = stubbedRetrofit(stubbedFailHtml);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                retrofit.create(TaskItemService.class),
                callProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);


        //act
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);


        //assert
        verify(mockSuccessCallback, never()).success();
        verify(mockFailureCallbackWithPayload, times(1)).fail(eq(stubbedFailHtml.expectedResult));
        verify(mockTaskListModel, never()).addManyFilterOutDuplicates(any());
        Assert.assertEquals(false, fetcher.isBusy());
    }


    /**
     * Here we are making sure that the model correctly handles a server response indicating
     * that the session token has expired
     *
     * @throws Exception
     */
    @Test
    public void fetchTasks_Fail_SessionTokenInvalid() throws Exception {

        //arrange
        Retrofit retrofit = stubbedRetrofit(stubbedFailSessionTimeout);
        TaskFetcher fetcher = new TaskFetcher(
                mockTaskListModel,
                retrofit.create(TaskItemService.class),
                callProcessor,
                mockSystemTimeWrapper,
                logger,
                WorkMode.SYNCHRONOUS);


        //act
        fetcher.fetchTaskItems(mockSuccessCallback, mockFailureCallbackWithPayload);


        //assert
        verify(mockSuccessCallback, never()).success();
        verify(mockFailureCallbackWithPayload, times(1)).fail(eq(stubbedFailSessionTimeout.expectedResult));
        verify(mockTaskListModel, never()).addManyFilterOutDuplicates(any());
        Assert.assertEquals(false, fetcher.isBusy());
    }

    private Retrofit stubbedRetrofit(StubbedServiceDefinition stubbedServiceDefinition){
        return CustomRetrofitBuilder.create(
                new InterceptorStubbedService(stubbedServiceDefinition),
                interceptorLogging);
    }

}
