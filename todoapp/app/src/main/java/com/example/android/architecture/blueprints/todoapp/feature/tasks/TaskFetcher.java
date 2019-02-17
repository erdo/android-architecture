package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemPojo;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import co.early.fore.core.Affirm;
import co.early.fore.core.WorkMode;
import co.early.fore.core.callbacks.FailureCallbackWithPayload;
import co.early.fore.core.callbacks.SuccessCallback;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.observer.ObservableImp;
import co.early.fore.core.time.SystemTimeWrapper;
import co.early.fore.retrofit.CallProcessor;

/**
 * Gets a list of tasks from the network, checks for duplicates and adds them to the database
 *
 * This app doesn't handle full synchronization with data on a server, local
 * changes are not pushed to the internet for example.
 */
@Singleton
public class TaskFetcher extends ObservableImp {

    public static final String LOG_TAG = TaskFetcher.class.getSimpleName();

    //notice how we use the TaskListModel, we don't go directly to the db layer
    private final TaskListModel taskListModel;
    private final TaskItemService service;
    private final CallProcessor<UserMessage> callProcessor;
    private final SystemTimeWrapper systemTimeWrapper;
    private final WorkMode workMode;
    private final Logger logger;

    private boolean busy;

    @Inject
    public TaskFetcher(TaskListModel taskListModel, TaskItemService service, CallProcessor<UserMessage> callProcessor,
                       SystemTimeWrapper systemTimeWrapper, Logger logger, WorkMode workMode) {
        super(workMode);
        this.taskListModel = Affirm.notNull(taskListModel);
        this.service = Affirm.notNull(service);
        this.callProcessor = Affirm.notNull(callProcessor);
        this.systemTimeWrapper = Affirm.notNull(systemTimeWrapper);
        this.logger = Affirm.notNull(logger);
        this.workMode = Affirm.notNull(workMode);
    }

    public void fetchTaskItems(final SuccessCallback successCallback, final FailureCallbackWithPayload<UserMessage> failureCallbackWithPayload){

        logger.i(LOG_TAG, "fetchTaskItems()");

        Affirm.notNull(successCallback);
        Affirm.notNull(failureCallbackWithPayload);

        if (busy){
            failureCallbackWithPayload.fail(UserMessage.ERROR_BUSY);
            return;
        }

        busy = true;
        notifyObservers();

        // if you want to parse custom errors here, please see the retrofit example in the fore docs
        // for an easy way to support this
        // https://github.com/erdo/android-fore/blob/master/example04retrofit/src/main/java/foo/bar/example/foreretrofit/api/fruits/FruitsCustomError.java
        callProcessor.processCall(service.getTaskItems("5s"), workMode,
                successResponse -> handleNetworkSuccess(successCallback, successResponse),
                failureMessage -> handleNetworkFailure(failureCallbackWithPayload, failureMessage));

    }

    private void handleNetworkSuccess(SuccessCallback successCallBack, List<TaskItemPojo> taskItemPojos){
        addTaskItemsToDatabase(taskItemPojos);
        successCallBack.success();
        complete();
    }

    private void handleNetworkFailure(FailureCallbackWithPayload<UserMessage> failureCallbackWithPayload, UserMessage failureMessage){
        failureCallbackWithPayload.fail(failureMessage);
        complete();
    }

    private void addTaskItemsToDatabase(List<TaskItemPojo> taskItemPojos){

        List<TaskItem> taskItems = new ArrayList<>(taskItemPojos.size());

        for (TaskItemPojo taskItemPojo : taskItemPojos){
            taskItems.add(new TaskItem(systemTimeWrapper.currentTimeMillis(), taskItemPojo.title, taskItemPojo.description));
        }

        taskListModel.addManyFilterOutDuplicates(taskItems);
    }

    public boolean isBusy() {
        return busy;
    }

    private void complete(){

        logger.i(LOG_TAG, "complete()");

        busy = false;
        notifyObservers();
    }
}
