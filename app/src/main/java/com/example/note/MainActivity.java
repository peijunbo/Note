package com.example.note;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.example.note.database.NoteDatabaseManager;
import com.example.note.note.NotesFragment;
import com.example.note.todolist.TodolistFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String DATABASE_NAME = "NOTE.db";
    private int sortMethod = TodolistFragment.SORT_BY_ID;
    private ViewPager2 viewPager;
    private Fragment pageNote;
    private TodolistFragment pageTodolist;
    public NoteDatabaseManager databaseManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        setToolBar();

        databaseManager = new NoteDatabaseManager(this, DATABASE_NAME, null, 1);

        viewPager = findViewById(R.id.view_pager);
        List<Fragment> pageList = new ArrayList<Fragment>();
        pageTodolist = new TodolistFragment(databaseManager);
        pageNote = new NotesFragment(databaseManager);
        pageList.add(pageNote);
        pageList.add(pageTodolist);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), getLifecycle(), pageList);
        viewPager.setAdapter(adapter);
    }
    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        else {
            return true;
        }

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        ImageButton settingBtn;
        settingBtn = findViewById(R.id.setting_button);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.setting_menu, popupMenu.getMenu());
                popupMenu.getMenu().getItem(sortMethod).setChecked(true);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (sortMethod != TodolistFragment.SORT_BY_ID && id == R.id.option_create_time) {
                            sortMethod = TodolistFragment.SORT_BY_ID;
                        }
                        else if (sortMethod != TodolistFragment.SORT_BY_DEADLINE && id == R.id.option_deadline) {
                            sortMethod = TodolistFragment.SORT_BY_DEADLINE;
                        }
                        pageTodolist.sort(sortMethod);
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }
}