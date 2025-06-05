package com.example.lastapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String content;

    public String imagePath;

    public long timestamp;

    public boolean isPinned = false;//置顶

    public boolean isFavorite = false; //收藏

    public String category; //分类
    public boolean isDeleted = false; // 回收
}