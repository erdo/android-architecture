package com.example.android.architecture.blueprints.todoapp.db.tasks;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemEntity.COLUMN_COMPLETED;
import static com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemEntity.COLUMN_CREATE_TIMESTAMP;
import static com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemEntity.TABLE_NAME;


/**
 * Data Access class, the app shouldn't be accessing this class directly, its all wrapped up in
 * the associated model class in the feature package which handles threading and notifications
 * for you.
 */
@Dao
public abstract class TaskItemDao {

    @Insert
    public abstract long insertTaskItem(TaskItemEntity taskItemEntity);

    @Insert
    public abstract void insertManyTaskItems(List<TaskItemEntity> taskItemEntities);

    @Update
    public abstract int updateTaskItem(TaskItemEntity taskItemEntity);

    @Delete
    public abstract int deleteTaskItem(TaskItemEntity taskItemEntity);

    @Query("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_CREATE_TIMESTAMP + " DESC, id")
    public abstract List<TaskItemEntity> getAllTaskItems();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_COMPLETED + " = :completed " + " ORDER BY " + COLUMN_CREATE_TIMESTAMP + " DESC, id")
    public abstract List<TaskItemEntity> getTaskItems(boolean completed);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    public abstract Integer getRowCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COLUMN_COMPLETED + " = 1")
    public abstract Integer getDoneRowCount();

    @Query("DELETE FROM " + TABLE_NAME)
    public abstract int clear();

    @Query("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_COMPLETED + " = 1")
    public abstract int clearCompleted();

}
