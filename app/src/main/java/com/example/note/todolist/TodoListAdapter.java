package com.example.note.todolist;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.R;
import com.example.note.todolist.Todo;

import java.util.ArrayList;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private int[] types;
    private List<Todo> unfinishedTodoList;
    private List<Todo> finishedTodoList;
    private final int TYPE_TODO_UNFINISHED = 0;
    private final int TYPE_TODO_FINISHED = 1;
    private onItemClickListener onItemClickListener;
    private onStatusClickListener onStatusClickListener;
    private onItemLongClickListener onItemLongClickListener;
    private onItemTouchListener onItemTouchListener;
    private Context context;

    public TodoListAdapter(Context context, List<Todo> todoList) {
        this.context = context;
        types = new int[todoList.size()];
        finishedTodoList = new ArrayList<Todo>();
        unfinishedTodoList = new ArrayList<Todo>();
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).isFinished()) {
                types[i] = TYPE_TODO_FINISHED;
                finishedTodoList.add(todoList.get(i));
            }
            else {
                types[i] = TYPE_TODO_UNFINISHED;
                unfinishedTodoList.add(todoList.get(i));
            }
        }
    }

    //对子项点击事件的监听
    public interface onItemClickListener {
        public void onItemClick(UnfinishedTodoHolder holder);
    }
    public interface onItemLongClickListener {
        public void onItemLongClick(UnfinishedTodoHolder holder);
    }
    public interface onStatusClickListener {
        public void onStatusClick(UnfinishedTodoHolder holder);
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
        public void onItemTouch(MotionEvent motionEvent, UnfinishedTodoHolder holder);
    }
    public void setOnItemTouchListener(onItemTouchListener listener) {
        onItemTouchListener = listener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View todoItem;
        switch (viewType) {
            case TYPE_TODO_FINISHED:
                todoItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);
                holder = new FinishedTodoHolder(todoItem);
                break;
            case TYPE_TODO_UNFINISHED:
                todoItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);
                holder = new UnfinishedTodoHolder(todoItem);
                break;
            default:
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int index = getIndex(position, getItemViewType(position));
        String content;
        String deadline;
        switch (getItemViewType(position)) {
            case TYPE_TODO_FINISHED:
                content = finishedTodoList.get(index).getContent();
                deadline = finishedTodoList.get(index).getDeadline();
                ((FinishedTodoHolder) holder).id = finishedTodoList.get(index).getId();
                ((FinishedTodoHolder) holder).content.setText(content);
                ((FinishedTodoHolder) holder).isFinished.setText(R.string.tick);
                ((FinishedTodoHolder) holder).deadline.setText(deadline);
                ((FinishedTodoHolder) holder).content.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                ((FinishedTodoHolder) holder).deadline.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                int textColor = context.getColor(R.color.light_text);
                ((FinishedTodoHolder) holder).content.setTextColor(textColor);
                ((FinishedTodoHolder) holder).deadline.setTextColor(textColor);
                ((FinishedTodoHolder) holder).isFinished.setTextColor(textColor);
                break;

            case TYPE_TODO_UNFINISHED:
                content = unfinishedTodoList.get(index).getContent();
                deadline = unfinishedTodoList.get(index).getDeadline();
                ((UnfinishedTodoHolder) holder).id = unfinishedTodoList.get(index).getId();
                ((UnfinishedTodoHolder) holder).content.setText(content);
                ((UnfinishedTodoHolder) holder).deadline.setText(deadline);
                ((UnfinishedTodoHolder) holder).isFinished.setText(R.string.cross);
                break;

            default:
        }
        if (onItemTouchListener != null) {
            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    onItemTouchListener.onItemTouch(motionEvent, (UnfinishedTodoHolder) holder);
                    return true;
                }
            });
        }
        //监听点击事件
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick((UnfinishedTodoHolder) holder);
                }
            });
        }
        if (onStatusClickListener != null) {
            ((UnfinishedTodoHolder) holder).isFinished.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStatusClickListener.onStatusClick((UnfinishedTodoHolder) holder);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (unfinishedTodoList.size() + finishedTodoList.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return types[position];
    }

    public static class UnfinishedTodoHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public TextView isFinished;
        public TextView content;
        public TextView deadline;
        public int id;
        public UnfinishedTodoHolder(@NonNull View itemView) {
            super(itemView);
            isFinished = (TextView) itemView.findViewById(R.id.todo_isFinished);
            content = (TextView) itemView.findViewById(R.id.todo_content);
            deadline = (TextView) itemView.findViewById(R.id.todo_deadline);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.todo_item);
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
    public static class FinishedTodoHolder extends UnfinishedTodoHolder {
        public FinishedTodoHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private int getIndex(int position, int type) {
        int index = 0;
        for (int i = 0; i < position; i++) {
            if (types[i] == type) {
                index++;
            }
        }
        return index;
    }
}
