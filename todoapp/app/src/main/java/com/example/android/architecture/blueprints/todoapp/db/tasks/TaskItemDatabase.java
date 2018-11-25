package com.example.android.architecture.blueprints.todoapp.db.tasks;

import android.app.Application;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import co.early.fore.core.Affirm;
import co.early.fore.core.WorkMode;


/**
 * Room Database class, the app shouldn't be accessing this class directly, its all wrapped up by
 * the associated model, see the feature package
 */
@Database(entities = {TaskItemEntity.class}, exportSchema = false, version = 1)
public abstract class TaskItemDatabase extends RoomDatabase {


    private static TaskItemDatabase instance;


    public static TaskItemDatabase getInstance(Application application, boolean inMemoryDb, WorkMode workMode) {

        Affirm.notNull(application);
        Affirm.notNull(workMode);

        if (instance == null) {
            instance = buildInstance(application, inMemoryDb, workMode);
        }

        return instance;
    }

    private static TaskItemDatabase buildInstance(Application application, boolean inMemoryDb, WorkMode workMode) {

        RoomDatabase.Builder<TaskItemDatabase> builder;

        if (inMemoryDb) {
            builder = Room.inMemoryDatabaseBuilder(application, TaskItemDatabase.class);
        } else {
            builder = Room.databaseBuilder(application, TaskItemDatabase.class,
                    TaskItemDatabase.class.getSimpleName() + "DB");
        }

        // addMigrations(builder);

        if (workMode == WorkMode.SYNCHRONOUS) {
            builder.allowMainThreadQueries();
        }

        return builder.build();
    }

    public static void destroyInstance() {
        instance = null;
    }

    public abstract TaskItemDao taskItemDao();

}
