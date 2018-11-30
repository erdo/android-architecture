package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import android.support.annotation.Nullable;

import co.early.fore.core.Affirm;
import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.observer.ObservableImp;
import co.early.fore.core.time.SystemTimeWrapper;

/**
 *  Holds the current task for things like editing, viewing the detail
 */
public class CurrentTaskModel extends ObservableImp {

    private static final String TAG = CurrentTaskModel.class.getSimpleName();

    private final TaskListModel taskListModel;
    private final SystemTimeWrapper systemTimeWrapper;
    private final Logger logger;

    @Nullable
    private TaskItem currentItem;
    private boolean loading = false;


    public CurrentTaskModel(TaskListModel taskListModel, SystemTimeWrapper systemTimeWrapper, Logger logger, WorkMode notificationMode) {
        super(notificationMode);
        this.taskListModel = Affirm.notNull(taskListModel);
        this.systemTimeWrapper = Affirm.notNull(systemTimeWrapper);
        this.logger = Affirm.notNull(logger);
    }

    public void loadTask(long entityId){

        if (loading){
            return;
        }

        loading = true;
        notifyObservers();

        taskListModel.getItemById(
                entityId,
                successResponse -> updateCurrentItemFromDb(successResponse),
                failResponse -> updateCurrentItemFromDb(null));
    }

    private void updateCurrentItemFromDb(TaskItem taskItem){
        currentItem = taskItem;
        loading = false;
        notifyObservers();
    }

    public void setTitle(String title){

        logger.i(TAG, "setTitle() title:" + title);

        Affirm.notNull(title);

        if (itemLoaded() && !title.equals(currentItem.getTitle())){
            currentItem.setTitle(title);
            notifyObservers();
        }
    }

    public void setDescription(String desc){

        logger.i(TAG, "setDescription() desc:" + desc);

        Affirm.notNull(desc);

        if (itemLoaded() && !desc.equals(currentItem.getDescription())){
            currentItem.setDescription(desc);
            notifyObservers();
        }
    }

    public boolean setCompleted(boolean completed){
        if (itemLoaded() && currentItem.isCompleted() != completed){
            currentItem.setCompleted(completed);
            saveChanges();
            notifyObservers();
            return true;
        }
        return false;
    }

    public String getTitle(){
        return itemLoaded() ? currentItem.getTitle() : "";
    }

    public String getDescription(){
        return itemLoaded() ? currentItem.getDescription() : "";
    }

    public boolean isCompleted(){
        return itemLoaded() ? currentItem.isCompleted() : false;
    }

    public void revertUnsavedChanges(){
        if (itemLoaded()) {
            loadTask(currentItem.getEntity().getId());
        }
    }

    public void saveChanges(){
        if (itemLoaded()) {
            taskListModel.update(currentItem);
        }
    }

    public void deleteCurrentTaskFromDb(){
        if (itemLoaded()) {
            taskListModel.remove(currentItem);
            currentItem = null;
            notifyObservers();
        }
    }

    public void createNewEmptyTask(){
        currentItem = new TaskItem(systemTimeWrapper.currentTimeMillis(), "", "");
        notifyObservers();
    }

    public boolean isValid(){
        return itemLoaded() && currentItem.getTitle().length()>0;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean itemLoaded(){
        return currentItem != null;
    }

    public boolean hasUnsavedChanges(){
        return (itemLoaded() && currentItem.isDirty());
    }
}
