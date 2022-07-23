package com.example.note.note;

import android.app.Activity;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.note.R;
import com.example.note.database.NoteDatabaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotesFragment extends Fragment {
    private static final String TAG = "NotesFragment";
    private static final int REQUEST_READ_EXTERNAL_FILE = 1;
    private static final int NEW_NOTE_ID = -1;
    private static final int NULL_NOTE_ID = -2;
    private View root;
    private RecyclerView recyclerView;
    private NotesGridAdapter adapter;
    private NoteDatabaseManager databaseManager;
    private ActivityResultLauncher<Intent> openNoteLauncher;
    public NotesFragment(NoteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: 2022/7/23 删除note 
        openNoteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent intent = result.getData();
                Note note = new Note(intent.getStringExtra("title"),
                        intent.getStringExtra("content"),
                        intent.getLongExtra("date", 0),
                        intent.getIntExtra("id", NULL_NOTE_ID));
                Log.d(TAG, "note获取的为" + intent.getStringExtra("content"));
                if (note.getId() != NULL_NOTE_ID) {
                    adapter.changeItemById(intent.getBooleanExtra("isNew", false), note);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.notes_mainlayout, container, false);
        recyclerView = root.findViewById(R.id.notes);
        adapter = new NotesGridAdapter(requireContext(), databaseManager.getNotes());
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adapter.setOnItemClickListener(new NotesGridAdapter.onItemClickListener() {
            @Override
            public void onItemClick(NotesGridAdapter.NoteHolder holder) {
                Intent intent = new Intent(requireContext(), NoteContentActivity.class);
                intent.putExtra("title", holder.title.getText());
                intent.putExtra("content", holder.content.getText());
                intent.putExtra("date", holder.date.getText());
                intent.putExtra("id", holder.id);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), holder.itemView, "note_content");
                openNoteLauncher.launch(intent, optionsCompat);
            }
        });
        recyclerView.setAdapter(adapter);
        return root;
    }
    public void setAddNoteBtn(FloatingActionButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), NoteContentActivity.class);
                intent.putExtra("id", NEW_NOTE_ID);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "note_content");
                openNoteLauncher.launch(intent, optionsCompat);
            }
        });
    }

//    private void requestExternalImage() {
//        getImageLauncher.launch("image/*");
//    }
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

    private SpannableString getSpannableString(String str) {
        SpannableString result = new SpannableString(str);
        Pattern imgPattern = Pattern.compile("\\[image[0-9]*]");
        Matcher matcher = imgPattern.matcher(str);
        while (matcher.find()) {
            ImageSpan imageSpan = getImageSpan(str.substring(matcher.start() + 1, matcher.end() - 1));//+1-1去掉括号
            result.setSpan(imageSpan, matcher.start(), matcher.end(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return result;
    }
}
