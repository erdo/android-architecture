package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.db.tasks.TaskItemEntity;

import co.early.fore.adapters.DiffComparator;
import co.early.fore.core.Affirm;

/**
 * Encapsulates a TaskItem, holds a reference to its equivalent in the database layer {@link TaskItemEntity}
 * <p>
 * TaskItem objects are tied to a database entry{@link TaskItemEntity}, and any calls on the setters
 * here will cause the instance to become dirty. A list of these items will be refreshed automatically
 * as long as the updates are saved to the database (see the model class in the features package).
 * The dirty flag is used by DiffUtil so that it can detect a change to the item when it compares
 * it with a fresh instance taken from the database. Once an item becomes dirty it remains that way
 * until it is replaced by a db copy.
 */
public class TaskItem implements DiffComparator<TaskItem> {

    private final TaskItemEntity taskItemEntity;
    private boolean dirty = false;

    public TaskItem(long creationTimestamp, String title, String description) {
        this(new TaskItemEntity(creationTimestamp, title, description));
    }

    public TaskItem(TaskItemEntity taskItemEntity) {
        this.taskItemEntity = Affirm.notNull(taskItemEntity);
    }


    public String getTitle() {
        return taskItemEntity.getTitle();
    }

    void setTitle(String title) {
        taskItemEntity.setTitle(title);
        dirty = true;
    }

    public String getDescription() {
        return taskItemEntity.getDescription();
    }

    void setDescription(String description) {
        taskItemEntity.setDescription(description);
        dirty = true;
    }

    @Nullable
    public String getTitleForList() {
        if (taskItemEntity.getTitle() == null || taskItemEntity.getTitle().length() == 0) {
            return taskItemEntity.getDescription();
        } else {
            return taskItemEntity.getTitle();
        }
    }

    public boolean isCompleted() {
        return taskItemEntity.isCompleted();
    }

    void setCompleted(boolean completed) {
        taskItemEntity.setCompleted(completed);
        dirty = true;
    }

    public long getCreationTimestamp() {
        return taskItemEntity.getCreationTimestamp();
    }

    public boolean isDirty() {
        return dirty;
    }

    public long getEntityId(){
        return taskItemEntity.getId();
    }

    TaskItemEntity getEntity(){
        return taskItemEntity;
    }

    /**
     * Used by {@see DiffUtil}
     * <p>
     * Do the two instances represent the same real world item? even though they maybe
     * different instances. For example, one could be its representation in a list view, the other
     * could be its representation in a database entity, but if they represent the same item
     * conceptually then this method should return true
     *
     * @param other
     * @return true if the items represent the same real world / conceptual item
     */
    @Override
    public boolean itemsTheSame(TaskItem other) {
        return other != null
               // && getCreationTimestamp() == other.getCreationTimestamp()
                && getEntity().getId() == other.getEntity().getId();
    }

    /**
     * Used by {@see DiffUtil}
     * <p>
     * Note this really means: do they look the same in a list on the display. As such is usually
     * related to a particular view.
     * <p>
     * This only gets called if {@link#itemsTheSame()} already returns true
     *
     * @param other
     * @return
     */
    @Override
    public boolean contentsTheSame(TaskItem other) {
        if (isDirty()){
            return false;
        } else if (isCompleted() != other.isCompleted()){
            return false;
        } else {
            return true;
        }
    }

}
