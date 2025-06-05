package com.example.lastapp;

import androidx.room.Entity;

@Entity(primaryKeys = {"noteId", "tagId"})
public class NoteTagCrossRef {
    public int noteId;
    public int tagId;
}
