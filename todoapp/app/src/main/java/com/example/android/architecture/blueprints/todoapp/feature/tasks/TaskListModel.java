package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import android.arch.persistence.room.InvalidationTracker;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemDatabase;
import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.early.fore.adapters.DiffCalculator;
import co.early.fore.adapters.DiffSpec;
import co.early.fore.adapters.Diffable;
import co.early.fore.core.Affirm;
import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.observer.ObservableImp;
import co.early.fore.core.threading.AsyncBuilder;
import co.early.fore.core.time.SystemTimeWrapper;

import static com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemEntity.TABLE_NAME;
import static com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel.RefreshStatus.ADDITIONAL_REFRESH_WAITING;
import static com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel.RefreshStatus.IDLE;
import static com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel.RefreshStatus.REQUESTED;
import static com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel.RefreshStatus.TAKEN_DB_LIST_SNAPSHOT;


/**
 * This model wraps the database and all access to the db should go through here. It's setup to
 * maintain an in-memory list that can drive a view adapter on the UI thread. (In this case we are
 * not using a cursor directly).
 * <p>
 * The only changes that are made to the in memory list are done via a refresh with the latest db
 * data so that nothing gets out of sync. i.e. any changes go directly to the database and come
 * back later on the UI thread as a result of a db refresh.
 * <p>
 * For performance reasons we avoid unnecessary db refreshes using refreshStatus to drop calls
 * where we can - this is only really necessary for extreme situations where you may be updating
 * large amounts of data continuously, but it's here for completeness none the less.
 * <p>
 * As we may be getting updates here from the network or other threads, we need to synchronize access
 * to the db via the dao objects for total robustness - again if we didn't bother synchronizing here
 * you would only see issues occasionally or in extreme situations, but we do it here for completeness
 * anyway.
 * <p>
 */
public class TaskListModel extends ObservableImp implements Diffable {

    public static final String LOG_TAG = TaskListModel.class.getSimpleName();

    private final TaskItemDatabase taskItemDatabase;
    private final Logger logger;
    private final SystemTimeWrapper systemTimeWrapper;
    private final WorkMode workMode;

    //we use this to synchronize access to the dao
    private final Object dbMonitor = new Object();

    //we don't use a cursor here, so we do maintain an in memory list of the entire db
    private List<TaskItem> taskItems = new ArrayList<>();

    // after about 1000 rows, DiffResult begins to get way too slow, so we forget
    // about animating changes to the list after that
    private DiffSpec latestDiffSpec;
    private int maxSizeForDiffUtil = 1000;

    private volatile int totalNumberOfTasks = 0;
    private volatile int totalNumberOfCompletedTasks = 0;

    /**
     * We are keeping this <strong>filter</strong> flag here because we only have one window into the data.
     * <p>
     * If we wanted to have more than one window, we'd probably use a different strategy:
     * <p>
     * Lets say we had a "main" page where you can toggle between seeing all the todos, and only
     * the todos that aren't done yet AND we also had an "admin" page where you would see all
     * the todos, no matter what the filter flag said.
     * <p>
     * In that case we would probably have two separate models for each of those views. The admin
     * page could be driven by this class (but with the filter flag removed) and the main page
     * would be driven by a smaller model which got its list data from this class but filtered it
     * based on a filter flag as follows:
     * <p>
     * <strong>Old pre Build.VERSION_CODES.N version to filter for done items:</strong>
     * <code>
     * finalTodoItemsList.clear();
     * for (TodoItemEntity todoItemEntity : allTodoItemsEntityList) {
     * if (filter =! Filter.ACTIVE || !todoItemEntity.isDone()) {
     * finalTodoItemsList.add(new TodoItem(todoItemEntity));
     * }
     * }
     * </code>
     * <p>
     * <strong>Java streams version to filter items:</strong>
     * <code>
     * finalTodoItemsList = allTodoItemsEntityList.stream()
     * .filter(todoItemEntity -> filter =! Filter.ACTIVE || !todoItemEntity.isDone())
     * .map(todoItemEntity -> new TodoItem(todoItemEntity))
     * .collect(Collectors.toList());
     * </code>
     * <p>
     * <strong>Kotlin version to filter items:</strong>
     * <code>
     * finalTodoItemsList.clear()
     * finalTodoItemsList.addAll(allTodoItemsEntityList
     * .filter {todoItemEntity -> filter =! Filter.ACTIVE || !todoItemEntity.isDone()}
     * .map {todoItemEntity -> TodoItem(todoItemEntity))
     * </code>
     * <p>
     * <strong>RxJava version to filter items:</strong>
     * <code>
     * finalTodoItemsList.clear();
     * Observable.fromIterable(allTodoItemsEntityList)//this line not necessary if you already have an observable
     * .filter(todoItemEntity -> filter =! Filter.ACTIVE || todoItemEntity.isDone())
     * .map(todoItemEntity -> new TodoItem(todoItemEntity))
     * .subscribe(todoItem -> finalTodoItemsList.add(todoItem));
     * </code>
     *
     *
     * For our current purposes however, we can just rely on SQL doing the work for us
     * and this works fine
     */
    private volatile Filter filter = Filter.ALL;


    // This helps performance, but it won't be necessary until you get to
    // updating your db multiple times a second with a db larger than a few thousand items,
    // if you want a super simple but slightly less performant implementation, just delete all
    // the references to RefreshStatus
    enum RefreshStatus {
        IDLE,
        REQUESTED,
        TAKEN_DB_LIST_SNAPSHOT,
        ADDITIONAL_REFRESH_WAITING
    }

    private volatile RefreshStatus refreshStatus = IDLE;


    public TaskListModel(TaskItemDatabase taskItemDatabase, Logger logger, SystemTimeWrapper systemTimeWrapper, WorkMode workMode) {
        super(workMode);

        this.taskItemDatabase = Affirm.notNull(taskItemDatabase);
        this.logger = Affirm.notNull(logger);
        this.systemTimeWrapper = Affirm.notNull(systemTimeWrapper);
        this.workMode = Affirm.notNull(workMode);

        latestDiffSpec = createFullDiffSpec(systemTimeWrapper);

        //hook into the database invalidation tracker and forward the updates to our own observers
        taskItemDatabase.getInvalidationTracker().addObserver(new InvalidationTracker.Observer(TABLE_NAME) {
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
                fetchLatestFromDb();
            }
        });
    }

    public void fetchLatestFromDb() {

        logger.i(LOG_TAG, "1 fetchLatestFromDb()");

        synchronized (refreshStatus) {


            switch (refreshStatus) {
                case IDLE:
                    refreshStatus = REQUESTED;
                    break;
                case TAKEN_DB_LIST_SNAPSHOT:
                    //we are now committed and we need to leave this to finish before refreshing again
                    refreshStatus = ADDITIONAL_REFRESH_WAITING;
                case REQUESTED:
                case ADDITIONAL_REFRESH_WAITING:
                    //we can forget about this, it's already in hand
                    return;
            }


            //noinspection unchecked
            new AsyncBuilder<List<TaskItem>, Pair<List<TaskItem>, DiffUtil.DiffResult>>(workMode)
                    .doInBackground(oldList -> {

                        logger.i(LOG_TAG, "2 asking for latest data");

                        synchronized (refreshStatus) {
                            if (refreshStatus == REQUESTED) {
                                refreshStatus = TAKEN_DB_LIST_SNAPSHOT;
                            }
                        }

                        synchronized (dbMonitor) {
                            totalNumberOfTasks = taskItemDatabase.taskItemDao().getRowCount();
                            totalNumberOfCompletedTasks = taskItemDatabase.taskItemDao().getDoneRowCount();
                        }

                        List<TaskItem> newList = new ArrayList<>();
                        List<TaskItemEntity> dbList = new ArrayList<>();

                        synchronized (dbMonitor) {
                            switch (filter) {
                                case COMPLETED:
                                    dbList = taskItemDatabase.taskItemDao().getTaskItems(true);
                                    break;
                                case ACTIVE:
                                    dbList = taskItemDatabase.taskItemDao().getTaskItems(false);
                                    break;
                                case ALL:
                                    dbList = taskItemDatabase.taskItemDao().getAllTaskItems();
                                    break;
                            }
                        }

                        for (TaskItemEntity taskItemEntity : dbList) {
                            newList.add(new TaskItem(taskItemEntity));
                        }

                        logger.i(LOG_TAG, "3 old list size (" + oldList[0].size() + ") new list size:(" + newList.size() + ")");

                        // work out the differences in the lists
                        DiffUtil.DiffResult diffResult;
                        if (oldList[0].size() < maxSizeForDiffUtil && newList.size() < maxSizeForDiffUtil) {
                            diffResult = new DiffCalculator<TaskItem>().createDiffResult(oldList[0], newList);
                        } else {
                            diffResult = null;
                        }

                        //hop back to the UI thread to update the UI
                        return new Pair<>(newList, diffResult);
                    })
                    .onPostExecute(payload -> {

                        logger.i(LOG_TAG, "4 updating in memory copy");

                        //we defer to whatever the db says here so that we don't get out of sync
                        taskItems.clear();
                        taskItems.addAll(payload.first);
                        latestDiffSpec = new DiffSpec(payload.second, systemTimeWrapper);

                        //notify immediately so that the changes are picked up
                        notifyObservers();

                        //see the note at the top about RefreshStatus for a simple version
                        boolean triggerNewRefresh = false;
                        synchronized (refreshStatus) {
                            if (refreshStatus == ADDITIONAL_REFRESH_WAITING) {
                                triggerNewRefresh = true;
                            }
                            refreshStatus = IDLE;
                        }
                        if (triggerNewRefresh) {
                            fetchLatestFromDb();
                        }

                    })
                    .execute(taskItems);
        }
    }

    //common db operations

    public void add(TaskItem taskItem) {

        logger.i(LOG_TAG, "add()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        new AsyncBuilder<TaskItem, Long>(workMode)
                .doInBackground(taskItems -> {
                    synchronized (dbMonitor) {
                        return taskItemDatabase.taskItemDao().insertTaskItem(taskItems[0].getEntity());
                    }
                })
                .execute(taskItem);
    }

    public void remove(TaskItem taskItem) {

        logger.i(LOG_TAG, "remove()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        new AsyncBuilder<TaskItem, Integer>(workMode)
                .doInBackground(taskItems -> {
                    synchronized (dbMonitor) {
                        return taskItemDatabase.taskItemDao().deleteTaskItem(taskItems[0].getEntity());
                    }
                })
                .execute(taskItem);
    }

    public void update(TaskItem taskItem) {

        logger.i(LOG_TAG, "update()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        new AsyncBuilder<TaskItemEntity, Integer>(workMode)
                .doInBackground(taskItems -> {
                    synchronized (dbMonitor) {
                        return taskItemDatabase.taskItemDao().updateTaskItem(taskItems[0]);
                    }
                })
                .execute(taskItem.getEntity());
    }

    public void addMany(List<TaskItem> taskItems) {

        logger.i(LOG_TAG, "addMany()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        //noinspection unchecked
        new AsyncBuilder<List<TaskItem>, Void>(workMode)
                .doInBackground(newTaskItems -> {

                    List<TaskItemEntity> taskItemEntities = new ArrayList<>(newTaskItems.length);

                    for (TaskItem taskItem : newTaskItems[0]) {
                        taskItemEntities.add(taskItem.getEntity());
                    }

                    synchronized (dbMonitor) {
                        taskItemDatabase.taskItemDao().insertManyTaskItems(taskItemEntities);
                    }
                    return null;
                })
                .execute(taskItems);
    }

    public void addManyFilterOutDuplicates(List<TaskItem> taskItems) {

        logger.i(LOG_TAG, "addManyFilterOutDuplicates()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        //noinspection unchecked
        new AsyncBuilder<List<TaskItem>, Void>(workMode)
                .doInBackground(newTaskItems -> {

                    List<TaskItemEntity> taskItemEntities = new ArrayList<>(newTaskItems.length);

                    synchronized (dbMonitor) {

                        List<TaskItemEntity> dbItems = taskItemDatabase.taskItemDao().getAllTaskItems();

                        for (TaskItem newItem : newTaskItems[0]) {

                            boolean duplicate = false;

                            for(TaskItemEntity dbItem : dbItems) {
                                //naive method to decide if we have a duplicate or not
                                if (newItem.getTitle().equals(dbItem.getTitle())){
                                    duplicate = true;
                                    break;
                                }
                            }

                            if (!duplicate) {
                                taskItemEntities.add(newItem.getEntity());
                            }
                        }

                        taskItemDatabase.taskItemDao().insertManyTaskItems(taskItemEntities);
                    }
                    return null;
                })
                .execute(taskItems);
    }

    public void clear() {

        logger.i(LOG_TAG, "clear()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        new AsyncBuilder<Void, Integer>(workMode)
                .doInBackground(voids -> {
                    synchronized (dbMonitor) {
                        return taskItemDatabase.taskItemDao().clear();
                    }
                })
                .execute((Void) null);
    }

    public void clearCompleted() {

        logger.i(LOG_TAG, "clearCompleted()");

        //fire to the db and forget - the invalidation tracker will keep us informed of changes
        new AsyncBuilder<Void, Integer>(workMode)
                .doInBackground(voids -> {
                    synchronized (dbMonitor) {
                        return taskItemDatabase.taskItemDao().clearCompleted();
                    }
                })
                .execute((Void) null);
    }

    public void add(String title, String description) {

        Affirm.notNull(title);
        Affirm.notNull(description);

        add(new TaskItem(systemTimeWrapper.currentTimeMillis(), title, description));
    }


    //other getters/setters for our model

    public void setFilter(Filter filter) {
        this.filter = Affirm.notNull(filter);
        fetchLatestFromDb(); //notifyObservers() will get called at the end of the db fetch
    }

    public Filter getCurrentFilter() {
        return filter;
    }

    public int getMaxSizeForDiffUtil() {
        return maxSizeForDiffUtil;
    }

    public void setMaxSizeForDiffUtil(int maxSizeForDiffUtil) {
        this.maxSizeForDiffUtil = maxSizeForDiffUtil;
        notifyObservers();
    }

    public boolean isValidItemTitle(String title) {
        return (title == null ? false : (title.length() > 0));
    }


    // methods that let us drive a view adapter easily

    public TaskItem get(int index) {
        checkIndex(index);
        return taskItems.get(index);
    }

    public int size() {
        return taskItems.size();
    }

    public boolean hasVisibleTasks() {
        return size() > 0;
    }

    public int getAllTasksCount() {
        return totalNumberOfTasks;
    }

    public int getCompletedTasksCount() {
        return totalNumberOfCompletedTasks;
    }

    public int getActiveTasksCount() {
        return totalNumberOfTasks - totalNumberOfCompletedTasks;
    }

    public void setCompleted(boolean completed, int index) {
        TaskItem item = get(index);
        item.setCompleted(completed);
        update(item);
    }

    public void toggleCompleted(int index) {
        TaskItem item = get(index);
        item.setCompleted(!item.isCompleted());
        update(item);
    }

    private void checkIndex(int index) {
        if (taskItems.size() == 0) {
            throw new IndexOutOfBoundsException("taskItems has no items in it, can not get index:" + index);
        } else if (index < 0 || index > taskItems.size() - 1) {
            throw new IndexOutOfBoundsException("taskItems index needs to be between 0 and " + (taskItems.size() - 1) + " not:" + index);
        }
    }


    /**
     * If the DiffResult is old, then we assume that whatever changes
     * were made to the list last time were never picked up by a
     * recyclerView (maybe because the list was not visible at the time).
     * In this case we clear the DiffResult and create a fresh one with a
     * full diff spec.
     *
     * @return the latest DiffResult for the list
     */
    @Override
    public DiffSpec getAndClearLatestDiffSpec(long maxAgeMs) {

        DiffSpec latestDiffSpecAvailable = latestDiffSpec;
        latestDiffSpec = createFullDiffSpec(systemTimeWrapper);

        if (systemTimeWrapper.currentTimeMillis() - latestDiffSpecAvailable.timeStamp < maxAgeMs) {
            return latestDiffSpecAvailable;
        } else {
            return latestDiffSpec;
        }
    }

    private DiffSpec createFullDiffSpec(SystemTimeWrapper stw) {
        return new DiffSpec(null, stw);
    }

}
