package com.example.android.architecture.blueprints.todoapp.feature.tasks;

import com.example.android.architecture.blueprints.todoapp.api.tasks.TaskItemPojo;
import com.example.android.architecture.blueprints.todoapp.message.UserMessage;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.early.fore.core.callbacks.FailureCallbackWithPayload;
import co.early.fore.core.callbacks.SuccessCallbackWithPayload;
import co.early.fore.core.logging.Logger;
import co.early.fore.net.retrofit2.CallProcessorRetrofit2;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 *
 */
public class StateBuilder {

    static final String TITLE_00 = "eggs";
    static final String TITLE_01 = "milk";
    static final String TITLE_02 = "bread";
    static final String DESCRIPTION = ", buy from the shop";

    static final List<TaskItem> TASK_ITEMS = Stream.of(
            new TaskItem(0, TITLE_00, TITLE_00 + DESCRIPTION),
            new TaskItem(0, TITLE_01, TITLE_01 + DESCRIPTION),
            new TaskItem(0, TITLE_02, TITLE_02 + DESCRIPTION)
    ).collect(Collectors.toList());

    static final List<TaskItemPojo> TASK_POJOS = Stream.of(
            new TaskItemPojo(TITLE_00, TITLE_00 + DESCRIPTION, false),
            new TaskItemPojo(TITLE_01, TITLE_01 + DESCRIPTION, false),
            new TaskItemPojo(TITLE_02, TITLE_02 + DESCRIPTION, false))
            .collect(Collectors.toList());

    private CallProcessorRetrofit2<UserMessage> mockCallProcessor;

    StateBuilder(CallProcessorRetrofit2<UserMessage> mockCallProcessor) {
        this.mockCallProcessor = mockCallProcessor;
    }

    StateBuilder getTasksSuccess(final List<TaskItemPojo> tasksPojo) {

        final ArgumentCaptor<SuccessCallbackWithPayload> callback = ArgumentCaptor.forClass(SuccessCallbackWithPayload.class);

        doAnswer(__ -> {
            callback.getValue().success(tasksPojo);
            return null;
        })

                .when(mockCallProcessor)
                .processCall(any(), any(), callback.capture(), any());

        return this;
    }

    StateBuilder getTasksFail(final UserMessage userMessage) {

        final ArgumentCaptor<FailureCallbackWithPayload> callback = ArgumentCaptor.forClass(FailureCallbackWithPayload.class);

        doAnswer(__ -> {
            callback.getValue().fail(userMessage);
            return null;
        })
                .when(mockCallProcessor)
                .processCall(any(), any(), any(), callback.capture());

        return this;
    }

    static class MatchesTasksFromServer extends ArgumentMatcher<List<TaskItem>> {

        private final Logger logger;
        private final String LOG_TAG;

        public MatchesTasksFromServer(Logger logger, String LOG_TAG) {
            this.logger = logger;
            this.LOG_TAG = LOG_TAG;
        }

        @Override
        public boolean matches(Object argument) {
            List<TaskItem> expected = StateBuilder.TASK_ITEMS;
            List<TaskItem> actual = (List<TaskItem>)argument;

            for (int i = 0; i < expected.size(); i++) {
                TaskItem expectedItem = expected.get(i);
                TaskItem actualItem = actual.get(i);

                if (!(expectedItem.getTitle().equals(actualItem.getTitle()) &&
                        expectedItem.getDescription().equals(actualItem.getDescription()) &&
                        expectedItem.isCompleted() == actualItem.isCompleted())){

                    logger.w(LOG_TAG, expectedItem.getTitle() + " "
                            + expectedItem.getDescription() + " "
                            + expectedItem.isCompleted());
                    logger.w(LOG_TAG, "--does not equal--");
                    logger.w(LOG_TAG, actualItem.getTitle() + " "
                            + actualItem.getDescription() + " "
                            + actualItem.isCompleted());

                    return false;
                }
            }
            return true;
        }
    }

}
