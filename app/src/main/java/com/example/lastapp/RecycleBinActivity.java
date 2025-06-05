package com.example.lastapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecycleBinActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<NoteWithTags> deletedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadDeletedNotes();
    }

    private void loadDeletedNotes() {
        new Thread(() -> {
            deletedList = NoteDatabase.getInstance(this).noteDao().getDeletedNotesWithTags();
            runOnUiThread(() -> {
                adapter = new NoteAdapter(this, deletedList, true); // true表示回收站模式
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    // 恢复
    public void restoreNote(Note note) {
        new Thread(() -> {
            note.isDeleted = false;
            NoteDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(this::loadDeletedNotes);
        }).start();
    }

    // 彻底删除
    public void realDeleteNote(Note note) {
        new Thread(() -> {
            NoteDatabase.getInstance(this).noteDao().delete(note);
            runOnUiThread(this::loadDeletedNotes);
        }).start();
    }
}