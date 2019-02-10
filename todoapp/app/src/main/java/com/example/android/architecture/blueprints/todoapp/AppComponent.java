package com.example.android.architecture.blueprints.todoapp;

import com.example.android.architecture.blueprints.todoapp.feature.tasks.CurrentTaskModel;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskFetcher;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;

import javax.inject.Singleton;

import co.early.fore.core.logging.Logger;
import dagger.Component;

@Singleton
@Component (modules={AppModule.class})
public interface AppComponent {

    //expose dependencies we want accessible from anywhere
    Logger getLogger();
    CurrentTaskModel getCurrentTaskModel();
    TaskFetcher getTaskFetcher();
    TaskListModel getTaskListModel();

//    //submodules follow
//    XxxComponent plus(XxxxModule xxxModule);
}

