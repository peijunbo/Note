package com.example.note.todolist;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.example.note.Utils;

public class TodoSortListCallback extends SortedListAdapterCallback<Todo> {
    public static final int SORT_BY_ID = 0;
    public static final int SORT_BY_DEADLINE = 1;
    private int sortMethod = SORT_BY_ID;
    public TodoSortListCallback(RecyclerView.Adapter adapter) {
        super(adapter);
    }
    public void setSortMethod(int i) {
        switch (i) {
            case SORT_BY_ID:
                sortMethod = SORT_BY_ID;
                break;
            case SORT_BY_DEADLINE:
                sortMethod = SORT_BY_DEADLINE;
                break;
            default:
        }
    }
    @Override
    public int compare(Todo o1, Todo o2) {
        if (o1.isFinished() == o2.isFinished()) {
            switch (sortMethod) {
                case SORT_BY_ID:
                    return Integer.compare(o2.getId(), o1.getId());
                case SORT_BY_DEADLINE:
                    return Long.compare(Utils.getTimeStampFromString(o1.getDeadline()), Utils.getTimeStampFromString(o2.getDeadline()));
                default:
                    return 1;
            }
        }
        else {
            return Boolean.compare(o1.isFinished(), o2.isFinished());
        }
    }

    @Override
    public boolean areContentsTheSame(Todo oldItem, Todo newItem) {
        return oldItem == newItem;
    }

    @Override
    public boolean areItemsTheSame(Todo item1, Todo item2) {
        return item1.getId() == item2.getId();
    }
}
