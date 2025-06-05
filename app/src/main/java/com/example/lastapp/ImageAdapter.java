package com.example.lastapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<String> imagePaths;
    private Context context;

    public ImageAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(new File(imagePaths.get(position))).into(holder.ivPic);
        holder.ivDelete.setOnClickListener(v -> {
            imagePaths.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPic, ivDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPic = itemView.findViewById(R.id.ivPic);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
