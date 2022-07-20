package com.example.note.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class NoteDatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = "NoteDatabaseHelper";
    private static final String CREATE_NOTE_DATABASE = "create table notes ( "
            + "id integer primary key autoincrement, "
            + "title text, "
            + "content text, "
            + "date integer)";
    private static final String CREATE_TODO_LIST = "create table todolist ("
            + "id integer primary key autoincrement, "
            + "deadline integer, "
            + "content text, "
            + "status integer )";
    public NoteDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_NOTE_DATABASE);
        sqLiteDatabase.execSQL(CREATE_TODO_LIST);
        Log.i(TAG, "table is created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
