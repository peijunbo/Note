package com.example.note.note;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.R;
import com.example.note.Utils;
import com.example.note.database.NoteDatabaseManager;
import com.example.note.databinding.NoteItemGridBinding;

import java.util.ArrayList;
import java.util.List;

public class NotesGridAdapter extends RecyclerView.Adapter<NotesGridAdapter.NoteHolder> {
    private List<Note> notes;
    private List<Boolean> booleanList;
    private Context context;
    private onItemClickListener itemClickListener;
    private onItemTouchListener itemTouchListener;
    private onItemTouchListener backupListener;
    private ObservableBoolean mSwitch;
    private NoteDatabaseManager databaseManager;
    public NotesGridAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
        booleanList = new ArrayList<>();
        mSwitch = new ObservableBoolean(false);
        for (int i = 0; i < notes.size(); i++) {
            booleanList.add(false);
        }
    }
    public NotesGridAdapter(Context context, List<Note> notes, NoteDatabaseManager databaseManager) {
        this.context = context;
        this.notes = notes;
        this.databaseManager = databaseManager;
        booleanList = new ArrayList<>();
        mSwitch = new ObservableBoolean(false);
        for (int i = 0; i < notes.size(); i++) {
            booleanList.add(false);
        }
    }

    public interface onItemClickListener {
        public void onItemClick(NoteHolder holder);
    }
    public void setOnItemClickListener(onItemClickListener listener) {
        itemClickListener = listener;
    }

    public interface onItemTouchListener {
        public void  onItemTouch(MotionEvent motionEvent, NoteHolder holder);
    }
    public void setOnItemTouchListener(onItemTouchListener listener) {
        itemTouchListener = listener;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.note_item_grid, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note note = notes.get(position);

        holder.mBinding.setVisible(mSwitch);

        holder.checkBox.setChecked(booleanList.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleanList.set(holder.getAdapterPosition(), isChecked);
            }
        });

        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.date.setText(Utils.getTimeStringFromStamp(note.getDate()));
        holder.id = notes.get(position).getId();


        // TODO: 2022/7/26 改为onTouchListener
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                itemClickListener.onItemClick(holder);
//            }
//        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (itemTouchListener != null) {
                    itemTouchListener.onItemTouch(event, holder);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public TextView date;
        public CheckBox checkBox;
        public int id;
        public NoteItemGridBinding mBinding;
        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.preview_note_title);
            content = itemView.findViewById(R.id.preview_note_content);
            date = itemView.findViewById(R.id.preview_note_date);
            checkBox = itemView.findViewById(R.id.delete_note_check);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    public void startSelect(NoteHolder holder) {
        backupListener = itemTouchListener;
        itemTouchListener = null;

        booleanList.set(holder.getAdapterPosition(), true);
        holder.checkBox.setChecked(true);

        mSwitch.set(true);
    }

    public void endSelect() {
        deleteSelectedNote();

        for (int i = 0; i < booleanList.size(); i++) {
            booleanList.set(i, false);
        }
        mSwitch.set(false);
        notifyDataSetChanged();
        itemTouchListener = backupListener;
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
    public void changeItemById(boolean isNew, Note note) {
        if (!isNew) {
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).getId() == note.getId()) {
                    notes.set(i, note);
                    notifyItemChanged(i);
                    return;
                }
            }
        }
        else {
            notes.add(0, note);
            booleanList.add(false);
            notifyItemInserted(0);
        }
    }

    private void deleteSelectedNote() {
        for (int i = 0; i < notes.size(); i++) {
            if (booleanList.get(i)) {
                databaseManager.deleteNoteById(notes.get(i).getId());
                notes.remove(i);
                booleanList.remove(i);
                notifyItemRemoved(i);
                i--;
            }
        }
    }

}
