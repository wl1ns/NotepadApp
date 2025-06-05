package com.example.lastapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<NoteWithTags> noteList;

    private EditText etSearch;
    private LinearLayout historyContainer;

    private Spinner spFilterCategory;
    private String[] categories = {"全部", "未分类", "工作", "生活", "学习"};

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etSearch = findViewById(R.id.etSearch);
        historyContainer = findViewById(R.id.historyContainer);

        Button btnRecycleBin = findViewById(R.id.btnRecycleBin);
        btnRecycleBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecycleBinActivity.class));
            }
        });

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NoteEditActivity.class));
            }
        });

        // 搜索监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNotes(s.toString());
                if (!s.toString().isEmpty()) {
                    saveSearchHistory(MainActivity.this, s.toString());
                    showSearchHistory();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        showSearchHistory();
        spFilterCategory = findViewById(R.id.spFilterCategory);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCategory.setAdapter(spinnerAdapter);

        spFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = categories[position];
                if ("全部".equals(selected)) {
                    loadNotes();
                } else {
                    loadNotesByCategory(selected);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadNotes();
//夜间模式
        findViewById(R.id.btnNight).setOnClickListener(v -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        });

        findViewById(R.id.btnDay).setOnClickListener(v -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    // 刷新并显示所有笔记（含标签）
    void loadNotes() {
        new Thread(() -> {
            noteList = NoteDatabase.getInstance(this).noteDao().getNotesWithTags();
            runOnUiThread(() -> {
                adapter = new NoteAdapter(this, noteList);
                adapter.setKeyword(""); // 不高亮
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    // 删除
    public void deleteNote(Note note) {
        new Thread(() -> {
            note.isDeleted = true;
            NoteDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(this::loadNotes);
        }).start();
    }

    // 搜索（含标签）
    private void searchNotes(String keyword) {
        new Thread(() -> {
            List<NoteWithTags> searchList = NoteDatabase.getInstance(this).noteDao().getNotesWithTags();
            // 过滤内容包含关键字的笔记
            List<NoteWithTags> filtered = new java.util.ArrayList<>();
            for (NoteWithTags nwt : searchList) {
                if (nwt.note.content != null && nwt.note.content.contains(keyword)) {
                    filtered.add(nwt);
                }
            }
            runOnUiThread(() -> {
                adapter = new NoteAdapter(this, filtered);
                adapter.setKeyword(keyword);//高亮关键词
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }

    // 分类筛选（含标签）
    private void loadNotesByCategory(String category) {
        new Thread(() -> {
            List<NoteWithTags> allList = NoteDatabase.getInstance(this).noteDao().getNotesWithTags();
            List<NoteWithTags> filtered = new java.util.ArrayList<>();
            for (NoteWithTags nwt : allList) {
                if (category.equals(nwt.note.category)) {
                    filtered.add(nwt);
                }
            }
            runOnUiThread(() -> {
                adapter = new NoteAdapter(this, filtered);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
    // 保存搜索历史
    public void saveSearchHistory(Context context, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        SharedPreferences sp = context.getSharedPreferences("search_history", MODE_PRIVATE);
        Set<String> history = new LinkedHashSet<>(sp.getStringSet("history", new LinkedHashSet<>()));
        history.remove(keyword); // 去重
        history.add(keyword);    // 新加到最后
        if (history.size() > 10) { // 最多保存10条
            Iterator<String> it = history.iterator();
            it.next();
            it.remove();
        }
        sp.edit().putStringSet("history", history).apply();
    }

    // 获取搜索历史
    public List<String> getSearchHistory(Context context) {
        SharedPreferences sp = context.getSharedPreferences("search_history", MODE_PRIVATE);
        Set<String> history = sp.getStringSet("history", new LinkedHashSet<>());
        return new ArrayList<>(history);
    }

    // 展示搜索历史
    private void showSearchHistory() {
        historyContainer.removeAllViews();
        List<String> history = getSearchHistory(this);
        for (String keyword : history) {
            TextView tv = new TextView(this);
            tv.setText(keyword);
            tv.setPadding(24, 8, 24, 8);
            tv.setBackgroundResource(R.drawable.bg_tag); // 可选圆角背景
            tv.setTextColor(getResources().getColor(android.R.color.white));
            tv.setOnClickListener(v -> etSearch.setText(keyword));
            historyContainer.addView(tv);
        }
    }

}