package com.example.note.todolist;

public class Todo {
    private int id;
    private String content;
    private String deadline;
    private boolean isFinished;
    public Todo(String content, String deadline, boolean isFinished) {
        this.content = content;
        if (deadline != null) {
            this.deadline = deadline;
        }
        else {
            this.deadline = "NULL";
        }
        this.isFinished = isFinished;
    }
    public Todo(String content, String deadline, boolean isFinished, int id) {
        this.content = content;
        if (deadline != null) {
            this.deadline = deadline;
        }
        else {
            this.deadline = "NULL";
        }
        this.isFinished = isFinished;
        this.id = id;
    }

    public boolean isFinished() {
        return isFinished;
    }
    public String getContent() {
        return content;
    }
    public String getDeadline() {
        return deadline;
    }
    public int getId() {return id;}
    public void changeContent(String content) {this.content = content;}
    public void changeDeadline(String deadline) {this.deadline = deadline;}
    public void changeStatus() {isFinished = !isFinished;}
}
