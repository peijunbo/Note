package com.example.note.note;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.note.R;
import com.example.note.database.NoteDatabaseManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotesFragment extends Fragment {
    private static final String TAG = "NotesFragment";
    public static final String DATABASE_NAME = "NOTE.db";
    public static final int REQUEST_READ_EXTERNAL_FILE = 1;
    private View root;
    private RecyclerView recyclerView;
    private NoteDatabaseManager databaseManager;
//    private ActivityResultLauncher<String> getImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
//        @Override
//        public void onActivityResult(Uri result) {
//            //将选择的图片添加到edittext后
//            if (result != null) {
//                //转存图片
//                InputStream inputStream = null;
//                FileOutputStream fileOutputStream = null;
//                String imageName = "image" + System.currentTimeMillis();
//                try {
//                    inputStream = requireContext().getContentResolver().openInputStream(result);
//                    Log.d(TAG, imageName);
//                    fileOutputStream = requireContext().openFileOutput(imageName, Context.MODE_PRIVATE);
//                    byte[] buffer = new byte[4096];
//                    int redCount;
//                    while ((redCount = inputStream.read(buffer)) >= 0) {
//                        fileOutputStream.write(buffer, 0, redCount);
//                    }
//                } catch (FileNotFoundException e) {
//                    Log.e(TAG, "FileNotFoundException");
//                } catch (IOException e) {
//                    Log.e(TAG, "IOException");
//                } finally {
//                    try {
//                        if (inputStream != null) {
//                            inputStream.close();
//                        }
//                        if (fileOutputStream != null) {
//                            fileOutputStream.flush();
//                            fileOutputStream.getFD().sync();
//                            fileOutputStream.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                //添加到编辑框
//                SpannableString spannableString = new SpannableString("[" + imageName + "]");
//                spannableString.setSpan(getImageSpan(imageName), 0, spannableString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
//                editNoteText.getEditableText().insert(editNoteText.getSelectionStart(), spannableString);
//            }
//        }
//    });

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.notes_mainlayout, container, false);

        //设置recyclerView内容
        List<Note> notes = new ArrayList<>();
        for (int i = 1; i < 40; i++) {
            if (i % 10 == 0) {
                notes.add(new Note("标题" + i, "超长内容\n超长内容\n超长内容\n超长内容\n啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦\n啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦" + i, i));
            }
            notes.add(new Note("标题" + i, "内容" + i, i));
        }
        recyclerView = root.findViewById(R.id.notes);
        NotesGridAdapter notesGridAdapter = new NotesGridAdapter(requireContext(), notes);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(notesGridAdapter);
        return root;
    }


//    private void requestExternalImage() {
//        getImageLauncher.launch("image/*");
//    }

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
