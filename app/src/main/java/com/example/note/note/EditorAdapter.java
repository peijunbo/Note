package com.example.note.note;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.R;

import java.util.List;

public class EditorAdapter extends RecyclerView.Adapter<EditorAdapter.EditorHolder> {

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        public EditText getEditText();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public EditorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_editor_item, parent, false);
        return new EditorHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EditorHolder holder, int position) {
        switch (position) {
            case 0:
                holder.icon.setImageResource(R.mipmap.bold_icon);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            EditText editText = onItemClickListener.getEditText();
                            editText.getEditableText().setSpan(new StyleSpan(Typeface.BOLD),
                                    editText.getSelectionStart(), editText.getSelectionEnd(),
                                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                });
                break;
            case 1:
                holder.icon.setImageResource(R.mipmap.italic_icon);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            EditText editText = onItemClickListener.getEditText();
                            editText.getEditableText().setSpan(new StyleSpan(Typeface.ITALIC),
                                    editText.getSelectionStart(), editText.getSelectionEnd(),
                                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                });
                break;
            case 2:
                holder.icon.setImageResource(R.mipmap.underline_icon);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            EditText editText = onItemClickListener.getEditText();
                            editText.getEditableText().setSpan(new UnderlineSpan(),
                                    editText.getSelectionStart(), editText.getSelectionEnd(),
                                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return 3;//固定长度
    }

    public static class EditorHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        public EditorHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.text_editor_icon);
        }
    }
}
