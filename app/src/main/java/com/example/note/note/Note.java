package com.example.note.note;

import java.util.Calendar;

public class Note {
    private int id;
    private String title;
    private String content;
    private long date;
    public Note(String title, String content, long date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public Note(String title, String content, long date, int id) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public long getDate() {
        return date;
    }

    public void changeTitle(String title) {
        this.title = title;
    }
    public void changeContent(String content) {
        this.content = content;
    }
    public void changeDate(long date) {
        this.date = date;
    }

}
