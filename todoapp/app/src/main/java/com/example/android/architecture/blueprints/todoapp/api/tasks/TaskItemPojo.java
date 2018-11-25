package com.example.android.architecture.blueprints.todoapp.api.tasks;

/**
 *
 *
 * <Code>
 *
 *  The server returns us a list of taskitems that look like this:
 *
 *  {
 *    "title":"bread",
 *    "description":"buy bread at the shop",
 *    "completed":false
 *  }
 *
 * </Code>
 *
 *
 *
 */
public class TaskItemPojo {

    public String title;
    public String description;
    public boolean completed;

    public TaskItemPojo(String title, String description, boolean completed) {
        this.title = title;
        this.description = description;
        this.completed = completed;
    }
}
