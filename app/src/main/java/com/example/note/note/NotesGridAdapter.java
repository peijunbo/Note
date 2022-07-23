package com.example.note.note;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.R;
import com.example.note.Utils;

import java.util.List;

public class NotesGridAdapter extends RecyclerView.Adapter<NotesGridAdapter.NoteHolder> {
    private List<Note> notes;
    private Context context;
    private onItemClickListener onItemClickListener;
    public NotesGridAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    public interface onItemClickListener {
        public void onItemClick(NoteHolder holder);
    }
    public void setOnItemClickListener(onItemClickListener listener) {
        onItemClickListener = listener;
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
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.date.setText(Utils.getTimeStringFromStamp(note.getDate()));
        holder.id = notes.get(position).getId();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    private void deleteItem(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }

    public static class NoteHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public TextView date;
        public int id;
        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.preview_note_title);
            content = itemView.findViewById(R.id.preview_note_content);
            date = itemView.findViewById(R.id.preview_note_date);
        }
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
            notifyItemInserted(0);
        }
    }

}
