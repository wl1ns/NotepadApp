package com.example.lastapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);
    @Update
    void update(Note note);
    @Delete
    void delete(Note note);
    @Insert
    void insertTag(Tag tag);
    @Query("SELECT * FROM Note ORDER BY isPinned DESC, isFavorite DESC, timestamp DESC")
    List<Note> getAllNotes();
    @Query("SELECT * FROM Note WHERE id = :id")
    Note getNoteById(int id);
    @Query("SELECT * FROM Note WHERE content LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    List<Note> searchNotes(String keyword);
    @Query("SELECT * FROM Note WHERE category = :category ORDER BY isPinned DESC, isFavorite DESC, timestamp DESC")
    List<Note> getNotesByCategory(String category);
    @Insert
    void insertNoteTagCrossRef(NoteTagCrossRef crossRef);

    @Transaction
    @Query("SELECT * FROM Note WHERE isDeleted = 0 ORDER BY isPinned DESC, isFavorite DESC, timestamp DESC")
    List<NoteWithTags> getNotesWithTags();

    @Transaction
    @Query("SELECT * FROM Note WHERE id = :noteId")
    NoteWithTags getNoteWithTags(int noteId);

    @Query("SELECT * FROM Tag")
    List<Tag> getAllTags();


    // 查询回收站笔记
    @Transaction
    @Query("SELECT * FROM Note WHERE isDeleted = 1 ORDER BY timestamp DESC")
    List<NoteWithTags> getDeletedNotesWithTags();
}