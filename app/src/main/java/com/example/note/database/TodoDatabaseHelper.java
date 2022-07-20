package com.example.note.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TodoDatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = "TodoDatabaseHelper";
    private static final String CREATE_TODO_LIST = "create table todolist ("
            + "id integer primary key autoincrement, "
            + "deadline integer, "
            + "content text, "
            + "status integer)";

    public TodoDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TODO_LIST);
        Log.i(TAG, "todolist table is created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
