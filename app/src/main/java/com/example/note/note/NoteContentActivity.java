package com.example.note.note;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.note.R;

public class NoteContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_content_main);
        Intent intent = getIntent();
        EditText noteContent = findViewById(R.id.note_content);
        EditText noteTitle = findViewById(R.id.note_title);
        noteContent.setText(intent.getStringExtra("content"));
        noteTitle.setText(intent.getStringExtra("title"));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(NoteContentActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }
}
