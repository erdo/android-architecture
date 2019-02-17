package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;

import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemPojo;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemDatabase;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskItem;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Singleton;

import co.early.fore.core.WorkMode;
import co.early.fore.core.callbacks.SuccessCallbackWithPayload;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.logging.SystemLogger;
import co.early.fore.retrofit.CallProcessor;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
@Module
public class TestAppModule extends AppModule {

    private final static String LOG_TAG = TestAppModule.class.getSimpleName();

    public static final TaskItem LOCAL_TASKITEM_0_ACTIVE = new TaskItem(0, "local task title 0", "local task description 0");
    public static final TaskItem LOCAL_TASKITEM_1_COMPLETE = new TaskItem(1, "local task title 1", "local task description 1");
    public static final TaskItemPojo SERVER_TASKPOJO_0 = new TaskItemPojo("server task title", "server task description", false);

    public static final List<TaskItemPojo> TASK_POJOS_FROM_SERVER = Stream.of(
            SERVER_TASKPOJO_0)
            .collect(Collectors.toList());

    public static final List<TaskItem> LOCAL_TASK_ITEMS_FOR_DB = Stream.of(
            LOCAL_TASKITEM_0_ACTIVE,
            LOCAL_TASKITEM_1_COMPLETE)
            .collect(Collectors.toList());

    public TestAppModule(Application app) {
        super(app);
    }


    /**
     * Common
     */

    @Override
    @Provides
    @Singleton
    public WorkMode provideWorkMode() {
        return WorkMode.SYNCHRONOUS;
    }

    @Override
    @Provides
    @Singleton
    public Logger provideLogger() {
        SystemLogger systemLogger = new SystemLogger();
        systemLogger.i(LOG_TAG, "created logger");
        return systemLogger;
    }


    /**
     * Database
     */

    @Override
    @Provides
    @Singleton
    public TaskItemDatabase provideTaskItemDatabase(WorkMode workMode) {
        return TaskItemDatabase.getInstance(app, true, workMode);
    }


    /**
     * Networking
     */

    @Override
    @Provides
    @Singleton
    public CallProcessor<UserMessage> provideCallProcessor(Logger logger) {
        logger.i(LOG_TAG, "provideCallProcessor()");

        List<TaskItemPojo> TASK_POJOS_FROM_SERVER = Stream.of(
                new TaskItemPojo("task title", "task description", false))
                .collect(Collectors.toList());

        CallProcessor<UserMessage> mockCallProcessor = mock(CallProcessor.class);

        final ArgumentCaptor<SuccessCallbackWithPayload> callback = ArgumentCaptor.forClass(SuccessCallbackWithPayload.class);
        doAnswer(__ -> {
            callback.getValue().success(TASK_POJOS_FROM_SERVER);
            return null;
        })
                .when(mockCallProcessor)
                .processCall(any(), any(), any(), callback.capture(), any());

        return mockCallProcessor;
    }

    @Override
    @Provides
    @Singleton
    public TaskItemService provideTaskItemService(Retrofit retrofit) {
        return mock(TaskItemService.class);
    }

}
