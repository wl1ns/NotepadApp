package com.example.lastapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Tag {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}