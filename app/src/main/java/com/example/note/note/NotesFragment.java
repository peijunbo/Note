package com.example.note.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.note.MainActivity;
import com.example.note.R;
import com.example.note.database.NoteDatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotesFragment extends Fragment {
    private static final String TAG = "NotesFragment";
    public static final String DATABASE_NAME = "NOTE.db";
    public static final int REQUEST_READ_EXTERNAL_FILE = 1;
    private View root;
    private EditText editNoteText;
    private Button selectImageBtn;
    private Button saveButton;
    private Button updateButton;
    private Button openButton;
    private NoteDatabaseManager databaseManager;
    private ActivityResultLauncher<String> getImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            //将选择的图片添加到edittext后
            if (result != null) {
                //转存图片
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                String imageName = "image" + System.currentTimeMillis();
                try {
                    inputStream = requireContext().getContentResolver().openInputStream(result);
                    Log.d(TAG, imageName);
                    fileOutputStream = requireContext().openFileOutput(imageName, Context.MODE_PRIVATE);
                    byte[] buffer = new byte[4096];
                    int redCount;
                    while ((redCount = inputStream.read(buffer)) >= 0) {
                        fileOutputStream.write(buffer, 0, redCount);
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFoundException");
                } catch (IOException e) {
                    Log.e(TAG, "IOException");
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
                        e.printStackTrace();
                    }
                }

                //添加到编辑框
                SpannableString spannableString = new SpannableString("[" + imageName + "]");
                spannableString.setSpan(getImageSpan(imageName), 0, spannableString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                editNoteText.getEditableText().insert(editNoteText.getSelectionStart(), spannableString);
            }
        }
    });

    public NotesFragment(NoteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private ImageSpan getImageSpan(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxWidth = 400;
        int maxHeight = 400;
        Bitmap result;
        if (width > maxWidth) {
            float scaleW = (float) maxWidth / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleW, scaleW);
            result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        else if (height > maxHeight) {
            float scaleH = (float) maxHeight / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleH, scaleH);
            result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        else {
            result = bitmap;
        }
        return new ImageSpan(requireContext(), result, ImageSpan.ALIGN_BASELINE);
    }
    private ImageSpan getImageSpan(String imageName) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = requireContext().openFileInput(imageName);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "imageNotFound");
            return null;
        }
        if (fileInputStream == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxWidth = 400;
        int maxHeight = 400;
        Bitmap result;
        if (width > maxWidth) {
            float scaleW = (float) maxWidth / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleW, scaleW);
            result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        else if (height > maxHeight) {
            float scaleH = (float) maxHeight / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleH, scaleH);
            result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        else {
            result = bitmap;
        }
        return new ImageSpan(requireContext(), result, ImageSpan.ALIGN_BASELINE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.note_mainlayout, container, false);
        editNoteText = root.findViewById(R.id.edit_note_text);

        selectImageBtn = root.findViewById(R.id.select_image);
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestExternalImage();
            }
        });
        saveButton = root.findViewById(R.id.save);
        updateButton = root.findViewById(R.id.load);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = new Note("标题", editNoteText.getText().toString(), 1);
                databaseManager.addNote(note);
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = databaseManager.getNotes().get(0);
                editNoteText.setText(getSpannableString(note.getContent()));
            }
        });
        openButton = root.findViewById(R.id.open);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(NotesFragment.this).navigate(R.id.open_note_content_fragment);
            }
        });
        return root;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void requestExternalImage() {
        getImageLauncher.launch("image/*");
    }

    private SpannableString getSpannableString(String str) {
        SpannableString result = new SpannableString(str);
        Pattern imPattern = Pattern.compile("\\[image[0-9]*]");
        Matcher matcher = imPattern.matcher(str);
        while (matcher.find()) {
            ImageSpan imageSpan = getImageSpan(str.substring(matcher.start() + 1, matcher.end() - 1));//+1-1去掉括号
            result.setSpan(imageSpan, matcher.start(), matcher.end(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return result;
    }
}
