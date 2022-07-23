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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
    private ActivityResultLauncher<String> getImageLauncher;
    private PopupWindow editOptions;
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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setTitle("");
        setContentView(R.layout.note_content_main);

        databaseManager = new NoteDatabaseManager(this, DATABASE_NAME, null, 1);
        init();
        initSoftInputListener();
        setGetImageLauncher();

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
        else if (id == R.id.text_options) {
            item.setIcon(R.drawable.text_icon);
            View view = findViewById(R.id.text_options);
            AnimatorSet scaleSet = new AnimatorSet();
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator scale = ObjectAnimator.ofFloat(view, "scaleX", 1, 1);
            scale.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    item.setIcon(R.drawable.close_icon);
                }
            });
            ObjectAnimator scale2 = ObjectAnimator.ofFloat(view, "scaleX", 1, 1);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0, 540);
            scale.setDuration(350);
            scale2.setDuration(150);
            rotation.setDuration(500);
            scaleSet.play(scale).after(scale2);
            animatorSet.play(rotation).with(scaleSet);
            animatorSet.start();
        }
        else if (id == R.id.set_bold) {
            StyleSpan span = new StyleSpan(Typeface.BOLD);
            noteContent.getEditableText().setSpan(span, noteContent.getSelectionStart(), noteContent.getSelectionEnd(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else if (id == R.id.set_underline) {
            UnderlineSpan span = new UnderlineSpan();
            noteContent.getEditableText().setSpan(span, noteContent.getSelectionStart(), noteContent.getSelectionEnd(),SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, getResult());
        super.onBackPressed();
    }


//    private void setGetImageLauncher() {
//        getImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
//                new ActivityResultCallback<Uri>() {
//            @Override
//            public void onActivityResult(Uri result) {
//                if (result != null) {
//                    InputStream inputStream = null;
//                    FileOutputStream fileOutputStream = null;
//                    String imageName = "image" + System.currentTimeMillis();
//                    try {
//                        inputStream = NoteContentActivity.this
//                                .getContentResolver()
//                                .openInputStream(result);
//
//                        fileOutputStream = NoteContentActivity.this
//                                .openFileOutput(imageName, Context.MODE_PRIVATE);
//                        byte[] buffer = new byte[4096];
//                        int redCount;
//                        while ((redCount = inputStream.read(buffer)) >= 0) {
//                            fileOutputStream.write(buffer, 0, redCount);
//                        }
//                    } catch (FileNotFoundException e) {
//                        Log.e(TAG, "ImageNotFound", e);
//                    } catch (IOException e) {
//                        Log.e(TAG, "ImageIO", e);
//                    } finally {
//                        try {
//                            if (inputStream != null) {
//                                inputStream.close();
//                            }
//                            if (fileOutputStream != null) {
//                                fileOutputStream.flush();
//                                fileOutputStream.getFD().sync();
//                                fileOutputStream.close();
//                            }
//                        } catch (IOException e) {
//                            Log.e(TAG, "closeError", e);
//                        }
//                    }
//                    Editable editable =  noteContent.getEditableText();
//                    editable.insert(noteContent.getSelectionStart(), getSpannableString("[" + imageName + "]"));
//
//                }
//            }
//        });
//    }
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

        return new ImageSpan(this, uri, ImageSpan.ALIGN_BASELINE);
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
        noteTitle = findViewById(R.id.note_title);

        if (id != -2 && id != NEW_NOTE_ID) {
            // TODO: 2022/7/23 用fromhtml处理文本
            Spanned spannableString = Html.fromHtml(intent.getStringExtra("content"), Html.FROM_HTML_MODE_COMPACT, new Html.ImageGetter() {
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

            noteTitle.setText(intent.getStringExtra("title"));
            date = Utils.getTimeStampFromString(intent.getStringExtra("date"));
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void initPopupWindow() {
        View editOptionsView = LayoutInflater.from(this).inflate(R.layout.edit_note_popup, findViewById(R.id.root_view), false);
        editOptions = new PopupWindow();
        editOptions.setContentView(editOptionsView);
    }

    private void initSoftInputListener() {
        View root = getWindow().getDecorView();
        root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                Rect rect = new Rect();
                root.getWindowVisibleDisplayFrame(rect);
                int visibleHeight = rect.height();
                int screenHeight = root.getHeight();
                if (visibleHeight < screenHeight * 3 / 4) {
                    //键盘打开
                }
                else {
                    //键盘收起
                    saveNote();

                }
            }
        });
    }


    private Intent getResult() {
        Intent intent = new Intent();
        // TODO: 2022/7/23 将结果用html返回来保存格式
        intent.putExtra("title", noteTitle.getText().toString());
        intent.putExtra("content", Html.toHtml(noteContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE));
        intent.putExtra("date", date);
        intent.putExtra("id", id);
        intent.putExtra("isNew", isNew);
        return intent;
    }

    private void saveNote() {
//        Note note = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), date);
        Note note = new Note(noteTitle.getText().toString(), Html.toHtml(noteContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE), date);
        if (id != -1) {//是已经存在的Note
            databaseManager.updateNoteById(id, note);
        }
        else {
            id = databaseManager.addNote(note);
            isNew = true;
        }
    }





}
