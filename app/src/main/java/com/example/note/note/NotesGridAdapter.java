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

    public NotesGridAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NoteContentActivity.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context, view, "note_content").toBundle();
                context.startActivity(intent, bundle);
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
        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.preview_note_title);
            content = itemView.findViewById(R.id.preview_note_content);
            date = itemView.findViewById(R.id.preview_note_date);
        }
    }
}
