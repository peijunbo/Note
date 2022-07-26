package com.example.note.note;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.MainActivity;
import com.example.note.R;
import com.example.note.Utils;
import com.example.note.database.NoteDatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteContentActivity extends AppCompatActivity {
    private static final String TAG = "NoteContentActivity";
    private static final int REQUEST_CODE_EXTERNAL_IMAGE = 1;
    private static final String DATABASE_NAME = "NOTE.db";
    private static final int NEW_NOTE_ID = -1;
    private NoteDatabaseManager databaseManager;
    private EditText noteContent;
    private EditText noteTitle;
    private Toolbar toolbar;
    private int id;
    private long date;
    private String content;
    private String title;
    private boolean isNew = false;
    //编辑功能弹窗
    private View editorMenu;
    private View.OnClickListener listener1;
    private View.OnClickListener listener2;
    //获取外部图片
    private ActivityResultLauncher<String> getImageLauncher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.white));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setTitle("");
        setContentView(R.layout.note_content_main);

        databaseManager = new NoteDatabaseManager(this, DATABASE_NAME, null, 1);
        init();
        setGetImageLauncher();
        initEditorMenu();
        initSoftInputListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_content_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            setSupportActionBar(null);
            Intent result = getResult();
            setResult(RESULT_OK, result);
            ActivityCompat.finishAfterTransition(NoteContentActivity.this);
        }
        else if (id == R.id.save_note) {
            saveNote();
        }
        else if (id == R.id.add_image) {
            getImageLauncher.launch("image/*");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveNote();
        setResult(RESULT_OK, getResult());
        super.onBackPressed();
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

                        //打开bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        //缩小bitmap

                        int maxWidth = getWindow().getDecorView().getWidth() - 100;
                        int maxHeight = getWindow().getDecorView().getHeight() / 3;

                        //调整大小
                        if (bitmap.getWidth() > maxWidth) {
                            float scaleW = (float) maxWidth / bitmap.getWidth();
                            Matrix matrix = new Matrix();
                            matrix.postScale(scaleW, scaleW);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        }
                        if (bitmap.getHeight() > maxHeight) {
                            float scaleH = (float) maxHeight / bitmap.getHeight();
                            Matrix matrix = new Matrix();
                            matrix.postScale(scaleH, scaleH);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        }

                        fileOutputStream = NoteContentActivity.this
                                .openFileOutput(imageName, Context.MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "ImageNotFound", e);
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

                    Editable editable =  noteContent.getEditableText();
                    editable.insert(noteContent.getSelectionStart(), getSpannableString("\n[" + imageName + "]\n"));

                }
            }
        });
    }

    private ImageSpan getImageSpan(String imageName) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(imageName);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "imageNotFound");
            return null;
        }
        if (fileInputStream == null) {
            return null;
        }

        //读取bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);

        int maxWidth = getWindow().getDecorView().getWidth() - 100;
        int maxHeight = getWindow().getDecorView().getHeight() / 3;

        //调整大小
        if (bitmap.getWidth() > maxWidth) {
            float scaleW = (float) maxWidth / bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(scaleW, scaleW);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        if (bitmap.getHeight() > maxHeight) {
            float scaleH = (float) maxHeight / bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(scaleH, scaleH);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        Uri uri = Uri.fromFile(getFileStreamPath(imageName));

        return new ImageSpan(this, uri, ImageSpan.ALIGN_BOTTOM);
    }

    private SpannableString getSpannableString(String str) {
        // TODO: 2022/7/23 修改函数
        SpannableString result = new SpannableString(str);
        Pattern imgPattern = Pattern.compile("\\[image[0-9]*]");
        Matcher matcher = imgPattern.matcher(str);
        while (matcher.find()) {
            ImageSpan imageSpan = getImageSpan(str.substring(matcher.start() + 1, matcher.end() - 1));//+1-1去掉括号
            result.setSpan(imageSpan, matcher.start(), matcher.end(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return result;
    }


    private void init() {
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -2);
        noteContent = findViewById(R.id.note_content);
        noteContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editorMenu.setVisibility(View.VISIBLE);
                }
                else {
                    editorMenu.setVisibility(View.GONE);
                    saveNote();
                }
            }
        });
        noteTitle = findViewById(R.id.note_title);
        if (id != NEW_NOTE_ID) {
            content = intent.getStringExtra("content");
            Spanned spannableString = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    Uri uri = Uri.parse(source);
                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
                    BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                    drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    return drawable;
                }
            }, null);
            noteContent.setText(spannableString);

            title = intent.getStringExtra("title");
            noteTitle.setText(title);
            date = Utils.getTimeStampFromString(intent.getStringExtra("date"));
        }
        View space = findViewById(R.id.note_scroll_bottom_space);
        space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.requestFocus();
            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void initEditorMenu() {
        //弹窗设置
        editorMenu = findViewById(R.id.note_editor);
        //编辑选项设置
        ImageView textStyle = findViewById(R.id.image8);
        View insertOptions = findViewById(R.id.insert_options);
        View textEditor = findViewById(R.id.text_editor);
        RecyclerView recyclerView = findViewById(R.id.editor_options);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        EditorAdapter adapter = new EditorAdapter();
        adapter.setOnItemClickListener(new EditorAdapter.OnItemClickListener() {
            @Override
            public EditText getEditText() {
                return noteContent;
            }
        });
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        //打开
        //位移动画
        ObjectAnimator o1 = ObjectAnimator.ofFloat(insertOptions, "translationX", 0, -1000);
        ObjectAnimator o2 = ObjectAnimator.ofFloat(textEditor, "translationX", 1000, 0);
        o1.setDuration(500);
        o2.setDuration(500);
        o1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                insertOptions.setVisibility(View.GONE);
                textStyle.setOnClickListener(listener2);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                textEditor.setVisibility(View.VISIBLE);
                textStyle.setOnClickListener(null);
            }
        });
        //修改文字计时
        ObjectAnimator s1 = ObjectAnimator.ofFloat(textStyle, "scaleX", 1, 1);
        s1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textStyle.setImageResource(R.mipmap.close_icon);
            }
        });
        s1.setDuration(150);

        //旋转动画
        ObjectAnimator r1 = ObjectAnimator.ofFloat(textStyle, "rotation", 0, 540);
        r1.setDuration(500);

        AnimatorSet openTextEditor = new AnimatorSet();
        openTextEditor.play(o1).with(o2).with(s1).with(r1);

        //关闭动画
        ObjectAnimator c1 = ObjectAnimator.ofFloat(insertOptions, "translationX", -1000, 0);
        ObjectAnimator c2 = ObjectAnimator.ofFloat(textEditor, "translationX", 0, 1000);
        c1.setDuration(500);
        c2.setDuration(500);
        c1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textEditor.setVisibility(View.GONE);
                textStyle.setOnClickListener(listener1);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                insertOptions.setVisibility(View.VISIBLE);
                textStyle.setOnClickListener(null);
            }
        });

        //修改文字计时
        ObjectAnimator s2 = ObjectAnimator.ofFloat(textStyle, "scaleX", 1, 1);
        s2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textStyle.setImageResource(R.mipmap.text_icon);
            }
        });
        s2.setDuration(350);

        //旋转动画
        ObjectAnimator r2 = ObjectAnimator.ofFloat(textStyle, "rotation", 540, 0);
        r2.setDuration(500);
        AnimatorSet closeTextEditor = new AnimatorSet();
        closeTextEditor.play(c1).with(c2).with(s2).with(r2);

        listener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTextEditor.start();
            }
        };
        listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTextEditor.start();
            }
        };
        textStyle.setOnClickListener(listener1);
    }
    private void initSoftInputListener() {

        ScrollView view = findViewById(R.id.note_scroll);
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom - top < (oldBottom - oldTop) * 2 / 3) {
                    //键盘打开
                }
                else if (bottom - top > (oldBottom - oldTop) * 3 / 2) {
                    //键盘关闭
                    noteContent.clearFocus();
                }
            }
        });
    }


    private Intent getResult() {
        Intent intent = new Intent();
        intent.putExtra("title", noteTitle.getText().toString());
        intent.putExtra("content", Html.toHtml(noteContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE));
        intent.putExtra("date", date);
        intent.putExtra("id", id);
        intent.putExtra("isNew", isNew);
        return intent;
    }

    private void saveNote() {
        String newContent = Html.toHtml(noteContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        String newTitle = noteTitle.getText().toString();
        if (!newContent.equals(content) || ! newTitle.equals(title) || newContent.isEmpty() || newTitle.isEmpty()) {
            Note note = new Note(noteTitle.getText().toString(), Html.toHtml(noteContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE), date);
            if (id != NEW_NOTE_ID) {//是已经存在的Note
                databaseManager.updateNoteById(id, note);
            } else {
                id = databaseManager.addNote(note);
                isNew = true;
            }
        }
    }
}
