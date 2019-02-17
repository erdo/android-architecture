package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemPojo;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import org.mockito.ArgumentCaptor;

import java.util.List;

import co.early.fore.core.callbacks.FailureCallbackWithPayload;
import co.early.fore.core.callbacks.SuccessCallbackWithPayload;
import co.early.fore.retrofit.CallProcessor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 *
 */
public class StateBuilder {

    private CallProcessor<UserMessage> mockCallProcessor;

    StateBuilder(CallProcessor<UserMessage> mockCallProcessor) {
        this.mockCallProcessor = mockCallProcessor;
    }

    StateBuilder getTasksSuccess(final List<TaskItemPojo> tasksPojo) {

        final ArgumentCaptor<SuccessCallbackWithPayload> callback = ArgumentCaptor.forClass(SuccessCallbackWithPayload.class);

        doAnswer(__ -> {
            callback.getValue().success(tasksPojo);
            return null;
        })

                .when(mockCallProcessor)
                .processCall(any(), any(), any(), callback.capture(), any());

        return this;
    }

    StateBuilder getTasksFail(final UserMessage userMessage) {

        final ArgumentCaptor<FailureCallbackWithPayload> callback = ArgumentCaptor.forClass(FailureCallbackWithPayload.class);

        doAnswer(__ -> {
            callback.getValue().fail(userMessage);
            return null;
        })
        .when(mockCallProcessor)
        .processCall(any(), any(), any(), any(), callback.capture());

        return this;
    }

}
