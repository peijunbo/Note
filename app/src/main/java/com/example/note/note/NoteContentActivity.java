package com.example.note.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.note.R;
import com.example.note.Utils;
import com.example.note.database.NoteDatabaseManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class NoteContentActivity extends AppCompatActivity {
    private static final String TAG = "NoteContentActivity";
    private static final int REQUEST_CODE_EXTERNAL_IMAGE = 1;
    private static final String DATABASE_NAME = "NOTE.db";
    private static final int NEW_NOTE_ID = -1;
    private NoteDatabaseManager databaseManager;
    private ActivityResultLauncher<String> getImageLauncher;
    private EditText noteContent;
    private EditText noteTitle;
    private Toolbar toolbar;
    private int id;
    private long date;
    private boolean isNew = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.white));
        setTitle("");
        setContentView(R.layout.note_content_main);
        init();

        databaseManager = new NoteDatabaseManager(this, DATABASE_NAME, null, 1);
        setGetImageLauncher();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_content_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setSupportActionBar(null);
            Intent result = getResult();
            setResult(RESULT_OK, result);
            ActivityCompat.finishAfterTransition(NoteContentActivity.this);
        }
        else if (item.getItemId() == R.id.save_note) {
            saveNote();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveNote();
        Intent result = getResult();
        setResult(RESULT_OK, result);
    }

    private void setGetImageLauncher() {
        getImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    InputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;
                    String imageName = "image" + System.currentTimeMillis();
                    try {
                        inputStream = NoteContentActivity.this
                                .getContentResolver()
                                .openInputStream(result);
                        fileOutputStream = NoteContentActivity.this
                                .openFileOutput(imageName, Context.MODE_PRIVATE);
                        byte[] buffer = new byte[4096];
                        int redCount;
                        while ((redCount = inputStream.read(buffer)) >= 0) {
                            fileOutputStream.write(buffer, 0, redCount);
                        }
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "ImageNotFound", e);
                    } catch (IOException e) {
                        Log.e(TAG, "ImageIO", e);
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.flush();
                                fileOutputStream.getFD().sync();
                                fileOutputStream.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "closeError", e);
                        }
                    }
                    // TODO: 2022/7/22 图片获取及imageSpan生成
                }
            }
        });
    }

    private void init() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -2);
        noteContent = findViewById(R.id.note_content);
        noteTitle = findViewById(R.id.note_title);

        if (id != -2 && id != NEW_NOTE_ID) {
            noteContent.setText(intent.getStringExtra("content"));
            noteTitle.setText(intent.getStringExtra("title"));
            date = Utils.getTimeStampFromString(intent.getStringExtra("date"));
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private Intent getResult() {
        Intent intent = new Intent();
        intent.putExtra("title", noteTitle.getText().toString());
        intent.putExtra("content", noteContent.getText().toString());
        intent.putExtra("date", date);
        intent.putExtra("id", id);
        intent.putExtra("isNew", isNew);
        return intent;
    }

    private void saveNote() {
        Note note = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), date);
        if (id != -1) {//是已经存在的Note
            databaseManager.updateNoteById(id, note);
        }
        else {
            id = databaseManager.addNote(note);
            isNew = true;
        }

    }
}
