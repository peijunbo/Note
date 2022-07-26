package com.example.note.note;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

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
    private ViewPager2 parent;
    private RecyclerView recyclerView;
    private NotesGridAdapter notesGridAdapter;
    private NoteDatabaseManager databaseManager;
    private ActivityResultLauncher<Intent> openNoteLauncher;
    //点击动画
    private AnimatorSet animatorSet;
    //删除
    private PopupWindow deleteNoteWindow;
    private ActionMode deleteActionMode;
    private ActionMode.Callback deleteCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // TODO: 2022/7/25 更换关闭图标
            requireActivity().getMenuInflater().inflate(R.menu.delete_select_menu, menu);
            parent.setUserInputEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            deleteNoteWindow.showAtLocation(root, Gravity.BOTTOM, 0, 0);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.select_all) {
                notesGridAdapter.selectAll();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deleteNoteWindow.dismiss();

            notesGridAdapter.endSelect();
            parent.setUserInputEnabled(true);
        }
    };

    public NotesFragment(NoteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    public NotesFragment(NoteDatabaseManager databaseManager, ViewPager2 viewPager2) {
        this.databaseManager = databaseManager;
        parent = viewPager2;
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
                    notesGridAdapter.changeItemById(intent.getBooleanExtra("isNew", false), note);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.notes_mainlayout, container, false);

        recyclerView = root.findViewById(R.id.notes);
        notesGridAdapter = new NotesGridAdapter(requireContext(), databaseManager.getNotes(), databaseManager);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        notesGridAdapter.setOnItemClickListener(new NotesGridAdapter.onItemClickListener() {
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
        recyclerView.setAdapter(notesGridAdapter);

        initDeleteWindow();

        return root;
    }

    private void initDeleteWindow() {
        View deleteNoteView = LayoutInflater.from(requireContext()).inflate(R.layout.delete_todo_layout, null, false);
        deleteNoteWindow = new PopupWindow(requireContext());
        deleteNoteWindow.setContentView(deleteNoteView);
        deleteNoteWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        deleteNoteWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        deleteNoteWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                deleteActionMode.finish();
            }
        });
        deleteNoteWindow.setAnimationStyle(R.style.PopupWindow);
        deleteNoteWindow.setBackgroundDrawable(AppCompatResources.getDrawable(getContext() , R.drawable.edit_todo_window));

        notesGridAdapter.setOnItemTouchListener(new NotesGridAdapter.onItemTouchListener() {
            @Override
            public void onItemTouch(MotionEvent motionEvent, NotesGridAdapter.NoteHolder holder) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        animatorSet = new AnimatorSet();
                        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 1, 0.95f);
                        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1, 0.95f);
                        shrinkX.setDuration(80);
                        shrinkY.setDuration(80);
                        ObjectAnimator shadow = ObjectAnimator.ofFloat(holder.itemView, "elevation", 0, 40);
                        shadow.setDuration(80);
                        ObjectAnimator expandX = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 0.95f, 1.05f);
                        ObjectAnimator expandY = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 0.95f, 1.05f);
                        expandX.setDuration(80);
                        expandY.setDuration(80);
                        animatorSet.play(expandX).with(expandY).with(shadow)
                                .after(320).after(shrinkY).after(shrinkX);
                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                holder.itemView.setBackgroundResource(R.drawable.dark_gray_bottom_rounded);
                            }

                            @Override
                            public void onAnimationStart(Animator animation)
                            {
                                holder.itemView.setBackgroundResource(R.drawable.gray_bottom_rounded);
                            }
                        });
                        animatorSet.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (animatorSet.isRunning()) {
                            //取消动画
                            animatorSet.cancel();
                            holder.itemView.setBackgroundResource(R.drawable.todo_item_background);
                            holder.itemView.setElevation(0);
                            holder.itemView.setScaleX(1);
                            holder.itemView.setScaleY(1);
                            //打开编辑界面
                            Intent intent = new Intent(requireContext(), NoteContentActivity.class);
                            intent.putExtra("title", holder.title.getText());
                            intent.putExtra("content", holder.content.getText());
                            intent.putExtra("date", holder.date.getText());
                            intent.putExtra("id", holder.id);
                            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), holder.itemView, "note_content");
                            openNoteLauncher.launch(intent, optionsCompat);
                        }
                        else {
                            //完成播放，打开删除界面
                            openDeleteNoteWindow(holder);
                            //播放结束动画
                            AnimatorSet animSet = new AnimatorSet();
                            ObjectAnimator resetX1 = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 1.05f, 1.01f);
                            ObjectAnimator resetY1 = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1.05f, 1.01f);
                            resetX1.setDuration(80);
                            resetY1.setDuration(80);
                            ObjectAnimator resetX2 = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 1.01f, 1.0f);
                            ObjectAnimator resetY2 = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 1.01f, 1.0f);
                            resetX2.setDuration(60);
                            resetY2.setDuration(60);
                            ObjectAnimator resetShadow1 = ObjectAnimator.ofFloat(holder.itemView, "elevation", 40, 8);
                            resetShadow1.setDuration(60);
                            ObjectAnimator resetShadow2 = ObjectAnimator.ofFloat(holder.itemView, "elevation", 8, 0);
                            resetShadow2.setDuration(60);
                            animSet.play(resetX2).with(resetY2).with(resetShadow2)
                                    .after(260).after(resetShadow1).after(resetX1).after(resetY1);
                            animSet.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    holder.itemView.setBackgroundResource(R.drawable.gray_bottom_rounded);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    holder.itemView.setBackgroundResource(R.drawable.todo_item_background);
                                }
                            });
                            animSet.start();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        //取消动画
                        animatorSet.cancel();
                        holder.itemView.setBackgroundResource(R.drawable.todo_item_background);
                        holder.itemView.setElevation(0);
                        holder.itemView.setScaleX(1);
                        holder.itemView.setScaleY(1);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    private void openDeleteNoteWindow(NotesGridAdapter.NoteHolder holder) {
        notesGridAdapter.startSelect(holder);
        deleteActionMode = requireActivity().startActionMode(deleteCallback);
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
