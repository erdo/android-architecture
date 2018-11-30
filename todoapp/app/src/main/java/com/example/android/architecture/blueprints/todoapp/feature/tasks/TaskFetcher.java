package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemPojo;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

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
public class TaskFetcher extends ObservableImp {

    public static final String LOG_TAG = TaskFetcher.class.getSimpleName();

    //notice how we use the TaskListModel, we don't go directly to the db layer
    private final TaskListModel taskListModel;
    private final TaskItemService service;
    private final CallProcessor<UserMessage> callProcessor;
    private final SystemTimeWrapper systemTimeWrapper;
    private final WorkMode workMode;
    private final Logger logger;

    private int connections;

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

        if (connections>8){
            failureCallbackWithPayload.fail(UserMessage.ERROR_BUSY);
            return;
        }

        connections++;
        notifyObservers();

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
        return connections>0;
    }

    public int getConnections() {
        return connections;
    }

    private void complete(){

        logger.i(LOG_TAG, "complete()");

        connections--;
        notifyObservers();
    }
}
