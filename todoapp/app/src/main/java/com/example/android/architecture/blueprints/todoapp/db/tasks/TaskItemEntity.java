package com.example.android.architecture.blueprints.todoapp.db.tasks;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Room Entity, the database functionality is all managed by the
 * associated model, see the feature package
 */
@Entity
public class TaskItemEntity {

    public static final String TABLE_NAME = "TaskItemEntity";//must be the name of the Entity class
    public static final String COLUMN_CREATE_TIMESTAMP = "create_timestamp";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COMPLETED = "completed";


    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = COLUMN_CREATE_TIMESTAMP, index = true)
    private long creationTimestamp;

    @ColumnInfo(name = COLUMN_TITLE)
    private String title;

    @ColumnInfo(name = COLUMN_DESCRIPTION)
    private String description;

    @ColumnInfo(name = COLUMN_COMPLETED, index = true)
    private boolean completed;


    //for Room to use
    public TaskItemEntity() {
    }

    @Ignore
    public TaskItemEntity(long creationTimestamp, String title, String description) {
        this.creationTimestamp = creationTimestamp;
        this.title = title;
        this.description = description;
        this.completed = false;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
