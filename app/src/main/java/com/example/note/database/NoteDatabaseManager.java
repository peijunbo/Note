package com.example.note.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.note.Utils;
import com.example.note.note.Note;
import com.example.note.todolist.Todo;

import java.util.ArrayList;
import java.util.List;

public class NoteDatabaseManager {
    private NoteDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static final String TABLE_NAME = "notes";
    public NoteDatabaseManager(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        dbHelper = new NoteDatabaseHelper(context, name, factory, version);
        database = dbHelper.getWritableDatabase();
    }

    public void addNote(Note note) {
        database.insert(TABLE_NAME, null, getValuesFromNote(note));
    }

    public void deleteNoteById(int id) {
        database.delete(TABLE_NAME, "id = ?", new String[]{"" + id});
    }

    public void updateNoteById(int id, Note note) {
        database.update(TABLE_NAME, getValuesFromNote(note), "id = ?", new String[]{"" + id});
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<Note>();
        Cursor cursor;

        //创建时间排序
        cursor = database.rawQuery("select * from notes order by id asc", null);
        //读取
        String title;
        String content;
        long date;
        int id;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                date = 1;
                notes.add(new Note(title, content, date, id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    private ContentValues getValuesFromNote(Note note) {
        ContentValues values = new ContentValues();
        values.put("title", note.getTitle());
        values.put("content", note.getContent());
        values.put("date", note.getDate());
        return values;
    }

}
