package com.example.note.todolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.adapters.AdapterViewBindingAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.viewpager2.widget.ViewPager2;

import com.example.note.PagesFragment;
import com.example.note.R;
import com.example.note.database.NoteDatabaseManager;
import com.example.note.databinding.TodoItemBinding;
import com.example.note.todolist.Todo;

import java.util.ArrayList;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoViewHolder>{
    SortedList<Todo> todoList;
    private List<Boolean> booleanList;
    private ObservableBoolean mSwitch;
    private onItemClickListener onItemClickListener;
    private onStatusClickListener onStatusClickListener;
    private onItemLongClickListener onItemLongClickListener;
    private onItemTouchListener onItemTouchListener;
    private Context context;
    private NoteDatabaseManager databaseManager;
    private onItemTouchListener backupListener;
    public TodoListAdapter(Context context, NoteDatabaseManager databaseManager) {
        this.context = context;
        booleanList = new ArrayList<>();
        mSwitch = new ObservableBoolean(false);
        this.databaseManager = databaseManager;
    }
    public void startSelect(TodoViewHolder holder) {
        backupListener = onItemTouchListener;
        onItemTouchListener = null;
        for (int i = 0; i < booleanList.size(); i++) {
            booleanList.set(i, false);
        }
        booleanList.set(holder.getAdapterPosition(), true);
        holder.checkBox.setChecked(true);
        mSwitch.set(true);
    }
    public void startSelect() {
        backupListener = onItemTouchListener;
        onItemTouchListener = null;
        for (int i = 0; i < booleanList.size(); i++) {
            booleanList.set(i, false);
        }
        mSwitch.set(true);
    }
    public void endSelect() {
        for (int i = 0; i < booleanList.size(); i++) {
            booleanList.set(i, false);
        }
        mSwitch.set(false);
        notifyDataSetChanged();
        onItemTouchListener = backupListener;
    }

    public void setTodoList(SortedList<Todo> todoList) {
        this.todoList = todoList;
        for (int i = 0; i < todoList.size(); i++) {
            booleanList.add(false);
        }
    }
    //对子项点击事件的监听
    public interface onItemClickListener {
        public void onItemClick(TodoViewHolder holder);
    }
    public interface onItemLongClickListener {
        public void onItemLongClick(TodoViewHolder holder);
    }
    public interface onStatusClickListener {
        public void onStatusClick(TodoViewHolder holder);
    }
    public void setonItemClickListener(onItemClickListener listener) {
        onItemClickListener = listener;
    }
    public void setonStatusClickListener(onStatusClickListener listener) {
        onStatusClickListener = listener;
    }
    public void setOnItemLongClickListener(onItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }
    //按下事件监听
    public interface onItemTouchListener {
        public void onItemTouch(MotionEvent motionEvent, TodoViewHolder holder);
    }
    public void setOnItemTouchListener(onItemTouchListener listener) {
        onItemTouchListener = listener;
    }
    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View todoItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);;
        return new TodoViewHolder(todoItem);
    }


    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);

        holder.mBinding.setVisible(mSwitch);

        holder.id = todo.getId();
        holder.content.setText(todo.getContent());
        holder.deadline.setText(todo.getDeadline());
        holder.checkBox.setChecked(booleanList.get(position));

        if (todo.isFinished()) {
            holder.isFinished.setText(R.string.tick);
            holder.content.setPaintFlags(holder.content.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.deadline.setPaintFlags(holder.deadline.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            int textColor = context.getColor(R.color.light_text);
            holder.content.setTextColor(textColor);
            holder.deadline.setTextColor(textColor);
            holder.isFinished.setTextColor(textColor);
        }
        else {
            holder.isFinished.setText(R.string.cross);
            holder.content.setPaintFlags(holder.content.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.deadline.setPaintFlags(holder.deadline.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            int textColor = context.getColor(R.color.black);
            holder.content.setTextColor(textColor);
            holder.deadline.setTextColor(textColor);
            holder.isFinished.setTextColor(textColor);
        }
        //监听选中
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleanList.set(holder.getAdapterPosition(), isChecked);
            }
        });

        //监听按下事件
        if (onItemTouchListener != null) {
            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (onItemTouchListener != null) {
                        onItemTouchListener.onItemTouch(motionEvent, holder);
                    }
                    return true;
                }
            });
        }
        //监听点击事件
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder);
                }
            });
        }
        if (onStatusClickListener != null) {
            holder.isFinished.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStatusClickListener.onStatusClick(holder);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public TextView isFinished;
        public TextView content;
        public TextView deadline;
        public TodoItemBinding mBinding;
        public CheckBox checkBox;
        public int id;
        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            isFinished = itemView.findViewById(R.id.todo_isFinished);
            content = itemView.findViewById(R.id.todo_content);
            deadline = itemView.findViewById(R.id.todo_deadline);
            linearLayout = itemView.findViewById(R.id.todo_item);
            mBinding = DataBindingUtil.bind(itemView);
            checkBox = itemView.findViewById(R.id.delete_todo_check);
        }

        public TextView getContentView() {
            return content;
        }
        public TextView getDeadlineView() {
            return  deadline;
        }
        public TextView getIsFinishedView() {
            return isFinished;
        }
        public String getContent() {return content.getText().toString();}
        public String getDeadline() {return deadline.getText().toString();}

        public int getId() {
            return id;
        }
        public boolean finished() {
            return isFinished.getText().toString().equals("\u2714");
        }
        public Todo getSingleNote() {
            return new Todo(getContent(), getDeadline(), finished(), id);
        }
    }

    public void deleteTodoById(int id) {
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getId() == id) {
                todoList.removeItemAt(i);
                databaseManager.deleteTodoById(id);
                return;
            }
        }
    }
    public void deleteSelectedTodo() {
        for (int i = 0; i < todoList.size(); i++) {
            if (booleanList.get(i)) {
                databaseManager.deleteTodoById(todoList.get(i).getId());
                todoList.removeItemAt(i);
                booleanList.remove(i);
                i--;
            }
        }
    }


    public void selectAll() {
        boolean isSelectAll = true;
        for (int i = 0; i < booleanList.size(); i++) {
            if (!booleanList.get(i)) {
                booleanList.set(i, true);
                isSelectAll = false;
            }
        }
        if (isSelectAll) {
            for (int i = 0; i < booleanList.size(); i++) {
                booleanList.set(i, false);
            }
        }
        notifyDataSetChanged();
    }


    public void updateTodo(Todo todo) {
        int id = todo.getId();
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getId() == id) {
                todoList.updateItemAt(i, todo);
                databaseManager.updateTodoById(todo.getId(), todo);
                return;
            }
        }
    }
    public void addTodo(Todo todo) {
        booleanList.add(false);
        todoList.add(todo);
    }
}
