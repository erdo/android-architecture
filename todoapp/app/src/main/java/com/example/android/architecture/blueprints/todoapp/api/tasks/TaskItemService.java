package com.example.android.architecture.blueprints.todoapp.api.tasks;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * These stubs are hosted at https://www.mocky.io/
 *
 * http://www.mocky.io/v2/5beaf99e2f0000d731da3d74
 *
 */
public interface TaskItemService {

    @GET("5beaf99e2f0000d731da3d74/")
    Call<List<TaskItemPojo>> getTaskItems(@Query("mocky-delay") String delayScalaDurationFormat);

}
