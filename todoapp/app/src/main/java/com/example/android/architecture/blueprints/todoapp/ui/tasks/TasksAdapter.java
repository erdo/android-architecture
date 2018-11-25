package com.example.android.architecture.blueprints.todoapp.ui.tasks;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.App;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskItem;
import com.example.android.architecture.blueprints.todoapp.feature.tasks.TaskListModel;
import com.example.android.architecture.blueprints.todoapp.ui.taskdetail.TaskDetailActivity;

import co.early.fore.adapters.ChangeAwareAdapter;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class TasksAdapter extends ChangeAwareAdapter<TasksAdapter.ViewHolder> {

    private final TaskListModel taskListModel;
    private final TaskActionsCallBack taskActionsCallBack;

    public TasksAdapter(TaskListModel taskListModel, TaskActionsCallBack taskActionsCallBack) {
        super(taskListModel);
        this.taskListModel = taskListModel;
        this.taskActionsCallBack = taskActionsCallBack;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final TaskItem item = taskListModel.get(position);

        holder.title.setText(item.getTitleForList());
        holder.completeCB.setChecked(item.isCompleted());

        holder.itemView.setBackgroundDrawable(App.instance().getResources().getDrawable(
                item.isCompleted() ? R.drawable.list_completed_touch_feedback : R.drawable.touch_feedback));

        holder.completeCB.setOnClickListener(v -> {
            int betterPosition = holder.getAdapterPosition();
            if (betterPosition != NO_POSITION) {
                taskListModel.toggleCompleted(betterPosition);
                if (taskListModel.get(betterPosition).isCompleted()){
                    taskActionsCallBack.taskMarkedComplete();
                } else{
                    taskActionsCallBack.taskMarkedActive();
                }
            }
        });

        holder.itemView.setOnClickListener(view -> {
            int betterPosition = holder.getAdapterPosition();
            if (betterPosition != NO_POSITION) {
                TaskDetailActivity.start(App.instance(), betterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskListModel.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView title;
        protected CheckBox completeCB;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            completeCB = view.findViewById(R.id.complete);
        }
    }

    public interface TaskActionsCallBack{
        void taskMarkedComplete();
        void taskMarkedActive();
    }
}
