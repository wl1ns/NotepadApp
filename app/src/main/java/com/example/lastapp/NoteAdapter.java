package com.example.lastapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private List<NoteWithTags> noteList;
    private Context context;
    private String keyword = "";
    private boolean isRecycleBin = false;

    public NoteAdapter(Context context, List<NoteWithTags> noteList, boolean isRecycleBin) {
        this.context = context;
        this.noteList = noteList;
        this.isRecycleBin = isRecycleBin;
    }

    // 兼容主界面构造方法
    public NoteAdapter(Context context, List<NoteWithTags> noteList) {
        this(context, noteList, false);
    }

    // 设置高亮关键词
    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NoteWithTags noteWithTags = noteList.get(position);
        Note note = noteWithTags.note;

        // 内容显示
        holder.tvContent.setText(note.content);

        // 图片显示（多图只显示第一张）
        if (note.imagePath != null && !note.imagePath.isEmpty()) {
            String[] images = note.imagePath.split(";");
            if (images.length > 0 && !images[0].isEmpty()) {
                holder.ivImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(new java.io.File(images[0])).into(holder.ivImage);
            } else {
                holder.ivImage.setVisibility(View.GONE);
            }
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteEditActivity.class);
            intent.putExtra("note_id", note.id);
            context.startActivity(intent);
        });

        // 格式化时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String timeStr = sdf.format(new Date(note.timestamp));
        holder.tvTime.setText(timeStr);

        // 高亮显示搜索关键字
        if (!keyword.isEmpty() && note.content != null && note.content.contains(keyword)) {
            SpannableString ss = new SpannableString(note.content);
            int start = note.content.indexOf(keyword);
            while (start >= 0) {
                int end = start + keyword.length();
                ss.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = note.content.indexOf(keyword, end);
            }
            holder.tvContent.setText(ss);
        } else {
            holder.tvContent.setText(note.content);
        }

        // 置顶图标
        holder.ivPin.setImageResource(note.isPinned ? R.drawable.ic_pin_on : R.drawable.ic_pin_off);
        holder.ivPin.setOnClickListener(v -> {
            note.isPinned = !note.isPinned;
            NoteDatabase.getInstance(context).noteDao().update(note);
            if (context instanceof MainActivity) {
                ((MainActivity) context).loadNotes();
            }
        });

        // 收藏图标
        holder.ivFavorite.setImageResource(note.isFavorite ? R.drawable.ic_star : R.drawable.ic_star_border);
        holder.ivFavorite.setOnClickListener(v -> {
            note.isFavorite = !note.isFavorite;
            NoteDatabase.getInstance(context).noteDao().update(note);
            if (context instanceof MainActivity) {
                ((MainActivity) context).loadNotes();
            }
        });

        // 标签显示
        holder.tagContainer.removeAllViews();
        if (noteWithTags.tags != null) {
            for (Tag tag : noteWithTags.tags) {
                TextView tagView = new TextView(context);
                tagView.setText(tag.name);
                tagView.setTextSize(12);
                tagView.setTextColor(Color.WHITE);
                tagView.setBackgroundResource(R.drawable.bg_tag);
                tagView.setPadding(16, 4, 16, 4);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(8, 0, 0, 0);
                tagView.setLayoutParams(lp);
                holder.tagContainer.addView(tagView);
            }
        }

        // 长按事件：主界面为“删除”，回收站为“恢复/彻底删除”
        holder.itemView.setOnLongClickListener(v -> {
            if (isRecycleBin) {
                new AlertDialog.Builder(context)
                        .setTitle("操作")
                        .setItems(new String[]{"恢复", "彻底删除"}, (dialog, which) -> {
                            if (which == 0 && context instanceof RecycleBinActivity) {
                                ((RecycleBinActivity) context).restoreNote(note);
                            } else if (which == 1 && context instanceof RecycleBinActivity) {
                                ((RecycleBinActivity) context).realDeleteNote(note);
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(context)
                        .setTitle("删除")
                        .setMessage("确定要删除这条笔记吗？")
                        .setPositiveButton("删除", (dialog, which) -> {
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).deleteNote(note);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        ImageView ivImage;
        TextView tvTime;
        ImageView ivPin;
        ImageView ivFavorite;
        LinearLayout tagContainer;
        public ViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivPin = itemView.findViewById(R.id.ivPin);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tagContainer = itemView.findViewById(R.id.tagContainer);
        }
    }
}