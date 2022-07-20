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
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_TODOLIST = "todolist";
    public NoteDatabaseManager(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        dbHelper = new NoteDatabaseHelper(context, name, factory, version);
        database = dbHelper.getWritableDatabase();
    }

    public void addNote(Note note) {
        database.insert(TABLE_NOTES, null, getValuesFromNote(note));
    }

    public void deleteNoteById(int id) {
        database.delete(TABLE_NOTES, "id = ?", new String[]{"" + id});
    }

    public void updateNoteById(int id, Note note) {
        database.update(TABLE_NOTES, getValuesFromNote(note), "id = ?", new String[]{"" + id});
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


    public void addTodo(Todo todo) {
        database.insert(TABLE_TODOLIST, null, getValueFromTodo(todo));
    }

    public void deleteTodoById(int id) {
        database.delete(TABLE_TODOLIST, "id = ?", new String[]{"" + id});
    }

    public void updateTodoById(int id, Todo todo) {
        database.update(TABLE_TODOLIST, getValueFromTodo(todo), "id = ?", new String[]{"" + id});
    }


    public List<Todo> getTodoList(int sortMethod) {
        List<Todo> todoList = new ArrayList<Todo>();
        Cursor cursor;
        switch (sortMethod) {
            case 0:
                //创建时间排序
                cursor = database.rawQuery("select * from todolist order by status asc, id desc", null);
                break;
            case 1:
                //截止时间排序
                cursor = database.rawQuery("select * from todolist order by status asc, deadline asc", null);
                break;
            default:
                cursor = database.rawQuery("select * from todolist order by id asc", null);
                break;
        }
        //读取
        String deadline;
        long deadline_num;
        String content;
        int id;
        boolean isFinished;
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                deadline_num = cursor.getLong(cursor.getColumnIndexOrThrow("deadline"));
                deadline = Utils.getTimeStringFromStamp(deadline_num);
                content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                isFinished = cursor.getInt(cursor.getColumnIndexOrThrow("status")) > 0;
                todoList.add(new Todo(content, deadline, isFinished, id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return todoList;
    }

    private ContentValues getValueFromTodo(Todo todo) {
        ContentValues values = new ContentValues();
        values.put("content", todo.getContent());
        values.put("deadline", Utils.getTimeStampFromString(todo.getDeadline()));
        values.put("status", todo.isFinished());
        return values;
    }

}
