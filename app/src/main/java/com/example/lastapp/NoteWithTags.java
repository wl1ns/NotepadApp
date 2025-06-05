package com.example.lastapp;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;
import java.util.List;

public class NoteWithTags {
    @Embedded public Note note;
    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = NoteTagCrossRef.class,
                    parentColumn = "noteId",
                    entityColumn = "tagId"
            )
    )
    public List<Tag> tags;
}
