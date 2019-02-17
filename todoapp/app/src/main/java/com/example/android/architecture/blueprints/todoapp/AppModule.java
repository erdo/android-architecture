package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;

import com.example.android.architecture.blueprints.todoapp.api.CustomGlobalErrorHandler;
import com.example.android.architecture.blueprints.todoapp.api.CustomGlobalRequestInterceptor;
import com.example.android.architecture.blueprints.todoapp.api.CustomRetrofitBuilder;
import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemService;
import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemDatabase;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import javax.inject.Singleton;

import co.early.fore.core.Affirm;
import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.AndroidLogger;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.time.SystemTimeWrapper;
import co.early.fore.retrofit.CallProcessor;
import co.early.fore.retrofit.InterceptorLogging;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

/**
 * For the non-Dagger way of setting up your object graph, see ObjectGraph.java
 */
@Module
public class AppModule {

    protected final Application app;
    private final static String LOG_TAG = AppModule.class.getSimpleName();

    public AppModule(Application app) {
        this.app = Affirm.notNull(app);
    }


    /**
     * Common
     */

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    public WorkMode provideWorkMode() {
        return WorkMode.ASYNCHRONOUS;
    }


    @Provides
    @Singleton
    public Logger provideLogger() {
        AndroidLogger androidLogger = new AndroidLogger("todo-mvo_");
        androidLogger.i(LOG_TAG, "created logger");
        return androidLogger;
    }

    @Provides
    @Singleton
    public SystemTimeWrapper provideSystemTimeWrapper() {
        return new SystemTimeWrapper();
    }


    /**
     * Database
     */

    @Provides
    @Singleton
    public TaskItemDatabase provideTaskItemDatabase(WorkMode workMode) {
        return TaskItemDatabase.getInstance(app, false, workMode);
    }


    /**
     * Networking
     */

    @Provides
    @Singleton
    public Retrofit provideRetrofit(Logger logger) {
        logger.i(LOG_TAG, "provideRetrofit()");
        return CustomRetrofitBuilder.create(
                new CustomGlobalRequestInterceptor(logger),
                new InterceptorLogging(logger));//logging interceptor should be the last one
    }

    @Provides
    @Singleton
    public CallProcessor<UserMessage> provideCallProcessor(Logger logger) {
        logger.i(LOG_TAG, "provideCallProcessor()");
        return new CallProcessor<UserMessage>(new CustomGlobalErrorHandler(logger), logger);
    }

    @Provides
    @Singleton
    public TaskItemService provideTaskItemService(Retrofit retrofit) {
        return retrofit.create(TaskItemService.class);
    }

}
