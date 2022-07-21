package com.example.note;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.note.database.NoteDatabaseManager;
import com.example.note.note.NotesFragment;
import com.example.note.todolist.TodolistFragment;

import java.util.ArrayList;
import java.util.List;

public class PagesFragment extends Fragment {
    private static final String TAG = "PagesFragment";
    public static final String DATABASE_NAME = "NOTE.db";
    private View root;
    private ViewPager2 viewPager;
    private Fragment pageNote;
    private TodolistFragment pageTodolist;
    public NoteDatabaseManager databaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.pages_fragment, container, false);
        databaseManager = new NoteDatabaseManager(requireActivity(), DATABASE_NAME, null, 1);
        viewPager = root.findViewById(R.id.view_pager);
        List<Fragment> pageList = new ArrayList<>();
        pageTodolist = new TodolistFragment(databaseManager);
        pageNote = new NotesFragment(databaseManager);
        pageList.add(pageNote);
        pageList.add(pageTodolist);
        MyPagerAdapter adapter = new MyPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle(), pageList);
        viewPager.setAdapter(adapter);
        return root;
    }
}
