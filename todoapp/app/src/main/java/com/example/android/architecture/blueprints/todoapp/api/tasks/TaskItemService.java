package com.example.android.architecture.blueprints.todoapp.api.tasks;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * These stubs are hosted at https://www.mocky.io/
 *
 * http://www.mocky.io/v2/5c055d963300005f00e81252
 *
 */
public interface TaskItemService {

    @GET("5c055d963300005f00e81252/")
    Call<List<TaskItemPojo>> getTaskItems(@Query("mocky-delay") String delayScalaDurationFormat);

}
