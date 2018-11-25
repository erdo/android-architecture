package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;

import com.example.android.architecture.blueprints.todoapp.api.CustomGlobalErrorHandler;
import com.example.android.architecture.blueprints.todoapp.api.CustomGlobalRequestInterceptor;
import com.example.android.architecture.blueprints.todoapp.api.CustomRetrofitBuilder;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemDatabase;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskFetcher;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import java.util.HashMap;
import java.util.Map;

import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.AndroidLogger;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.time.SystemTimeWrapper;
import co.early.fore.retrofit.CallProcessor;
import co.early.fore.retrofit.InterceptorLogging;
import retrofit2.Retrofit;

import static co.early.fore.core.Affirm.notNull;


class ObjectGraph {

    private volatile boolean initialized = false;
    private final Map<Class<?>, Object> dependencies = new HashMap<>();


    void setApplication(Application application) {
        setApplication(application, WorkMode.ASYNCHRONOUS);
    }

    void setApplication(Application application, final WorkMode workMode) {

        notNull(application);
        notNull(workMode);


        // create dependency graph
        // this list can get long, formatting one parameter per line helps with merging
        AndroidLogger logger = new AndroidLogger();
        SystemTimeWrapper systemTimeWrapper = new SystemTimeWrapper();
        TaskItemDatabase taskItemDatabase = TaskItemDatabase.getInstance(
                application,
                false,
                workMode);
        TaskListModel taskListModel = new TaskListModel(
                taskItemDatabase,
                logger,
                systemTimeWrapper,
                workMode);
        // networking classes common to all models
        Retrofit retrofit = CustomRetrofitBuilder.create(
                new CustomGlobalRequestInterceptor(logger),
                new InterceptorLogging(logger));//logging interceptor should be the last one
        CallProcessor<UserMessage> callProcessor = new CallProcessor<UserMessage>(
                new CustomGlobalErrorHandler(logger),
                logger);
        TaskFetcher taskFetcher = new TaskFetcher(
                taskListModel,
                retrofit.create(TaskItemService.class),
                callProcessor,
                systemTimeWrapper,
                logger,
                workMode);


        // add models to the dependencies map if you will need them later
        dependencies.put(TaskFetcher.class, taskFetcher);
        dependencies.put(TaskListModel.class, taskListModel);
        dependencies.put(Logger.class, logger);

    }

    void init() {
        if (!initialized) {
            initialized = true;

            // run any necessary initialization code once object graph has been created here

            get(TaskListModel.class).fetchLatestFromDb();

        }
    }

    <T> T get(Class<T> model) {

        notNull(model);
        T t = model.cast(dependencies.get(model));
        notNull(t);

        return t;
    }

    <T> void putMock(Class<T> clazz, T object) {

        notNull(clazz);
        notNull(object);

        dependencies.put(clazz, object);
    }

}
