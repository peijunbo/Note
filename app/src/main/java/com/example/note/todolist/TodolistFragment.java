package com.example.note.todolist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.viewpager2.widget.ViewPager2;

import com.example.note.R;
import com.example.note.database.NoteDatabaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TodolistFragment extends Fragment {
    private View root;
    public static final int SORT_BY_ID = 0;
    public static final int SORT_BY_DEADLINE = 1;
    private int sortMethod = SORT_BY_ID;
    private NoteDatabaseManager databaseManager;
    private ViewPager2 parent;
    private TextView todoDate;
    private TextView todoTime;
    private RecyclerView recyclerView;
    private TodoListAdapter todoListAdapter;
    private TodoSortListCallback todoSortListCallback;
    private EditText editTodoText;
    private Button editTodoDoneBtn;
    private Button editTodoCancelBtn;
    private Button deleteTodoBtn;
    private View editTodoView;
    private View deleteTodoView;
    private PopupWindow editTodoWindow;
    private PopupWindow deleteTodoWindow;
    //多选删除
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
            deleteTodoWindow.showAtLocation(root, Gravity.BOTTOM, 0, 0);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.select_all) {
                todoListAdapter.selectAll();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            deleteTodoWindow.dismiss();
            todoListAdapter.endSelect();
            parent.setUserInputEnabled(true);
        }
    };
    //改变Note状态的监听
    private TodoListAdapter.onStatusClickListener onStatusClickListener;
    //按下监听实现动态效果
    private TodoListAdapter.onItemTouchListener onItemTouchListener;
    private AnimatorSet animatorSet;
    public void setSortMethod(int sortMethod) {
        this.sortMethod = sortMethod;
        initRecyclerView();
    }
    public TodolistFragment(NoteDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    public TodolistFragment(NoteDatabaseManager databaseManager, ViewPager2 viewPager2) {
        this.databaseManager = databaseManager;
        parent = viewPager2;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.todolist_mainlayout, container, false);

        initListeners();
        initViews();
        initButtons();
        initRecyclerView();
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.setting_menu, menu);
        menu.findItem(R.id.option_create_time).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (sortMethod != TodolistFragment.SORT_BY_ID && id == R.id.option_create_time) {
            sortMethod = TodolistFragment.SORT_BY_ID;
            menuItem.setChecked(true);
            setSortMethod(sortMethod);
        }
        else if (sortMethod != TodolistFragment.SORT_BY_DEADLINE && id == R.id.option_deadline) {
            sortMethod = TodolistFragment.SORT_BY_DEADLINE;
            menuItem.setChecked(true);
            setSortMethod(sortMethod);
        }
        return false;
    }

    private void initViews() {
        //编辑界面
        editTodoView = LayoutInflater.from(getContext()).inflate(R.layout.edit_todo_layout, null, false);
        editTodoWindow = new PopupWindow(getContext());
        editTodoWindow.setContentView(editTodoView);
        editTodoWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        editTodoWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        editTodoWindow.setFocusable(true);
        editTodoWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        editTodoWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        editTodoWindow.setBackgroundDrawable(AppCompatResources.getDrawable(getContext() , R.drawable.edit_todo_window));

        //删除界面
        deleteTodoView = LayoutInflater.from(getContext()).inflate(R.layout.delete_todo_layout, null, false);
        deleteTodoWindow = new PopupWindow(getContext());
        deleteTodoWindow.setContentView(deleteTodoView);
        deleteTodoWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        deleteTodoWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        deleteTodoWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                deleteActionMode.finish();
            }
        });
        deleteTodoWindow.setAnimationStyle(R.style.PopupWindow);
        deleteTodoWindow.setBackgroundDrawable(AppCompatResources.getDrawable(getContext() , R.drawable.edit_todo_window));

        //日期时间设置
        todoDate = (TextView) editTodoView.findViewById(R.id.todo_edit_date);
        todoDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDate(todoDate);
            }
        });
        todoTime = (TextView) editTodoView.findViewById(R.id.todo_edit_time);
        todoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickTime(todoTime);
            }
        });
        //绑定输入框
        editTodoText = (EditText) editTodoView.findViewById(R.id.todo_edit);
        editTodoText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    editTodoText.setSelection(editTodoText.getText().length());
                    InputMethodManager manager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                else {
                    openSoftInput(editTodoText);
                }
            }
        });
        editTodoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkInput();
            }
        });
        //待办列表
        recyclerView = (RecyclerView) root.findViewById(R.id.todolist);
    }

    private void initButtons() {
        //完成待办编辑
        editTodoDoneBtn = (Button) editTodoView.findViewById(R.id.todo_edit_done);

        //删除待办
        deleteTodoBtn = (Button) deleteTodoView.findViewById(R.id.delete_todo_item);
        deleteTodoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoListAdapter.deleteSelectedTodo();
                //后续界面变化
                deleteActionMode.finish();
            }
        });
        //取消待办输入
        editTodoCancelBtn = (Button) editTodoView.findViewById(R.id.todo_edit_cancel);
        editTodoCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeEditTodoWindow();
            }
        });
    }

    public void setAddTodoBtn(FloatingActionButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEditNoteBtn(null);
                resetEditLayout();
                openEditNoteWindow();
            }
        });
    }

    private void initListeners() {

        onStatusClickListener = new TodoListAdapter.onStatusClickListener() {
            @Override
            public void onStatusClick(TodoListAdapter.TodoViewHolder holder) {
                Todo todo = holder.getSingleNote();
                todo.changeStatus();
                todoListAdapter.updateTodo(todo);
//                updateRecyclerView();
            }
        };
        animatorSet = new AnimatorSet();

        //按下动态效果
        onItemTouchListener = new TodoListAdapter.onItemTouchListener() {
            @Override
            public void onItemTouch(MotionEvent motionEvent, TodoListAdapter.TodoViewHolder holder) {
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
                            //编辑框内容设置
                            editTodoText.setText(holder.getContentView().getText());
                            String[] deadline = holder.getDeadlineView().getText().toString().split(" ");
                            todoDate.setText(deadline[0]);
                            todoTime.setText(deadline[1]);
                            //新建监听修改Note
                            setEditNoteBtn(holder);
                            //显示编辑界面
                            openEditNoteWindow();
                        }
                        else {
                            //完成播放，打开删除界面
//                            setDeleteNoteBtn(holder);

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
        };
    }

    //待办列表
    private void initRecyclerView() {
        todoListAdapter = new TodoListAdapter(requireContext(), databaseManager);
        todoSortListCallback = new TodoSortListCallback(todoListAdapter);
        todoSortListCallback.setSortMethod(sortMethod);
        SortedList<Todo> sortedList = new SortedList<>(Todo.class, todoSortListCallback);
        sortedList.addAll(databaseManager.getTodoList());
        todoListAdapter.setTodoList(sortedList);
        todoListAdapter.setonStatusClickListener(onStatusClickListener);
        todoListAdapter.setOnItemTouchListener(onItemTouchListener);
        recyclerView.setAdapter(todoListAdapter);
    }



    //针对不同情况修改编辑界面按钮事件
    private void setEditNoteBtn(TodoListAdapter.TodoViewHolder holder) {
        if (holder != null) {
            editTodoDoneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String deadline = todoDate.getText().toString() + " " + todoTime.getText().toString();
                    String content = editTodoText.getText().toString();
                    Todo todo = holder.getSingleNote();
                    todo.changeContent(content);
                    todo.changeDeadline(deadline);
                    todoListAdapter.updateTodo(todo);
                    //后续界面变化
                    closeEditTodoWindow();
//                    updateRecyclerView();
                }
            });
        }
        else {
            editTodoDoneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String deadline = todoDate.getText() + " " + todoTime.getText();
                    String content = editTodoText.getText().toString();
                    Todo todo = new Todo(content, deadline, false);
                    todo.id =   databaseManager.addTodo(todo);
                    todoListAdapter.addTodo(todo);
                    //后续界面变化
                    closeEditTodoWindow();
//                    updateRecyclerView();
                }
            });
        }
    }


    //针对不同情况修改删除界面按钮
//    private void setDeleteNoteBtn(TodoListAdapter.TodoViewHolder holder) {
//        deleteTodoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                databaseManager.deleteTodoById(holder.getId());
//                todoListAdapter.deleteTodoById(holder.getId());
//                //后续界面变化
//                closeDeleteNoteWindow(holder);
////                updateRecyclerView();
//            }
//        });
//    }
    public void checkInput() {
        String date = todoDate.getText().toString();
        String time = todoTime.getText().toString();
        String content = editTodoText.getText().toString();
        if (date.equals("日期") || time.equals("时间") || content.equals("")) {
            editTodoDoneBtn.setClickable(false);
            editTodoDoneBtn.setTextColor(Color.LTGRAY);
        }
        else {
            editTodoDoneBtn.setClickable(true);
            editTodoDoneBtn.setTextColor(Color.BLACK);
        }
    }

    private void resetEditLayout() {
        todoDate.setText("日期");
        todoTime.setText("时间");
        editTodoText.setText("");
    }

    private void closeEditTodoWindow() {
        editTodoWindow.dismiss();
        resetEditLayout();
        editTodoText.clearFocus();
    }


//    private void closeDeleteNoteWindow(TodoListAdapter.TodoViewHolder holder) {
//        holder.itemView.setBackgroundResource(R.drawable.todo_item_background);
//        deleteActionMode.finish();
//        todoListAdapter.deleteSelectedTodo();
//        todoListAdapter.endSelect();
//    }

    private void openEditNoteWindow() {

        checkInput();
        editTodoWindow.showAtLocation(root, Gravity.BOTTOM, 0, 0);
        editTodoText.requestFocus();
    }

    private void openDeleteNoteWindow(TodoListAdapter.TodoViewHolder holder) {
        deleteActionMode = requireActivity().startActionMode(deleteCallback);
        todoListAdapter.startSelect(holder);
    }

    private void openSoftInput(View view) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
            }
        }, 200);

    }
    private void closeSoftInput(View view) {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private void pickDate(TextView textView) {
        //为与时间选择保持一致，关闭软键盘
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                textView.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", i, i1, i2));
                checkInput();
            }
        }, year, month, day).show();
    }

    private void pickTime(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                textView.setText(String.format(Locale.CHINA,"%02d:%02d", i, i1));
                checkInput();
            }
        }, hour, minute, true).show();
    }

}
