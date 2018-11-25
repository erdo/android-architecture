package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.R;

public enum Filter {

    COMPLETED(R.string.label_completed, R.string.no_tasks_completed, R.drawable.ic_verified_user_24dp),
    ACTIVE(R.string.label_active, R.string.no_tasks_active, R.drawable.ic_check_circle_24dp),
    ALL(R.string.label_all,R.string.no_tasks_all, R.drawable.ic_assignment_turned_in_24dp);

    public final int labelStringResId;
    public final int noTasksStringResId;
    public final int noTasksDrawableResId;

    Filter(int labelStringResId, int noTasksStringResId, int noTasksDrawableResId) {
        this.labelStringResId = labelStringResId;
        this.noTasksStringResId = noTasksStringResId;
        this.noTasksDrawableResId = noTasksDrawableResId;
    }
}
