package com.example.note;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.note.database.NoteDatabaseManager;
import com.example.note.note.NotesFragment;
import com.example.note.todolist.TodolistFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PagesFragment extends Fragment {
    private static final String TAG = "PagesFragment";
    public static final String DATABASE_NAME = "NOTE.db";
    private View root;
    private ViewPager2 viewPager;
    private NotesFragment pageNote;
    private TodolistFragment pageTodolist;
    private FloatingActionButton button;
    public NoteDatabaseManager databaseManager;
    private SpannableString noteTitle;
    private ForegroundColorSpan colorSpan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.pages_fragment, container, false);
        databaseManager = new NoteDatabaseManager(requireActivity(), DATABASE_NAME, null, 1);
        viewPager = root.findViewById(R.id.view_pager);
        button = root.findViewById(R.id.floating_button);

        List<Fragment> pageList = new ArrayList<>();
        pageTodolist = new TodolistFragment(databaseManager, viewPager);
        pageNote = new NotesFragment(databaseManager, viewPager);
        pageList.add(pageNote);
        pageList.add(pageTodolist);
        MyPagerAdapter adapter = new MyPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle(), pageList);
        viewPager.setAdapter(adapter);

        noteTitle = new SpannableString("笔记 | 待办");
        colorSpan = new ForegroundColorSpan(requireActivity().getColor(R.color.dark_gray));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    pageNote.setAddNoteBtn(button);
                    noteTitle.setSpan(colorSpan, 5, 7, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
                    TextView title = requireActivity().findViewById(R.id.title);
                    title.setText(noteTitle);
                    Log.d(TAG, Html.toHtml(noteTitle, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE));
                }
                else {
                    pageTodolist.setAddTodoBtn(button);
                    noteTitle.setSpan(colorSpan, 0, 2, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
                    TextView title = requireActivity().findViewById(R.id.title);
                    title.setText(noteTitle);
                }
            }
        });
        return root;
    }
}
