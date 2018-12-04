package com.example.android.architecture.blueprints.todoapp.api;

import co.early.fore.core.logging.Logger;
import co.early.fore.retrofit.InterceptorLogging;
import okhttp3.Interceptor;

/**
 * This class only exists so that it can be replaced for the mock build
 */
public class InterceptorProvider {

    public static Interceptor[] getInterceptors(Logger logger){

        Interceptor[] interceptors = {
                new CustomGlobalRequestInterceptor(logger),
                new InterceptorLogging(logger) //logging interceptor should be the last one
        };

        return interceptors;
    }
}
