package ru.khavantsev.ziczac.navigator.db.service;


import android.content.Context;
import android.content.ContextWrapper;
import ru.khavantsev.ziczac.navigator.ApplicationContextProvider;
import ru.khavantsev.ziczac.navigator.db.DBHelper;

public class Service {
    private DBHelper helper;

    protected DBHelper getDBHelper() {
        if (helper == null) {
            helper = new DBHelper(ApplicationContextProvider.getContext());
        }
        return helper;
    }
}
