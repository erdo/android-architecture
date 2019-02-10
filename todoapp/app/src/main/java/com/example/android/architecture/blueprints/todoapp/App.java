package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;


/**
 * Try not to fill your own version of this class with lots of code, if possible move
 * any additional code out to a model somewhere
 */
public class App extends Application {

    private static App inst;
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        inst = this;
        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

    public void injectTestAppModule(AppModule testAppModule) {
        appComponent = DaggerAppComponent.builder().appModule(testAppModule).build();
    }

    public static App inst() {
        return inst;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    /**
     * To prevent initialisation stuff happening before we have had a chance to set our mocks
     * during tests, we separate out the init() stuff, to be called by the base activity of the app
     *
     * http://stackoverflow.com/questions/4969553/how-to-prevent-activityunittestcase-from-calling-application-oncreate
     *
     */
    public static void init() {
        // run any initialisation code here
    }

}
