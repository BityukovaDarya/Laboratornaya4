package ru.tpu.lab4;

import android.app.Application;

public class App extends Application {
    private static App sInstance;
    private StudentsManager studentsManager;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        studentsManager = new StudentsManager();
    }

    public StudentsManager getStudentsManager() {
        return studentsManager;
    }
}
