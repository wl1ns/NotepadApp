package com.example.lastapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteEditActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private EditText etContent;
    private Spinner spCategory;
    private String[] categories = {"未分类", "工作", "生活", "学习"};

    private RecyclerView rvImages;
    private ImageAdapter imageAdapter;
    private List<String> imagePaths = new ArrayList<>();
    private Uri photoUri;
    private String currentPhotoPath;

    private int noteId = -1;
    private Note editingNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        etContent = findViewById(R.id.etContent);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);

        // 分类标签
        spCategory = findViewById(R.id.spCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // 图片RecyclerView
        rvImages = findViewById(R.id.rvImages);
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(this, imagePaths);
        rvImages.setAdapter(imageAdapter);

        // 判断是否为编辑
        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            new Thread(() -> {
                editingNote = NoteDatabase.getInstance(this).noteDao().getNoteById(noteId);
                runOnUiThread(() -> {
                    if (editingNote != null) {
                        etContent.setText(editingNote.content);
                        // 多图支持：用分号分割
                        if (editingNote.imagePath != null && !editingNote.imagePath.isEmpty()) {
                            imagePaths.clear();
                            imagePaths.addAll(Arrays.asList(editingNote.imagePath.split(";")));
                            imageAdapter.notifyDataSetChanged();
                        }
                        // 设置分类
                        int idx = 0;
                        for (int i = 0; i < categories.length; i++) {
                            if (categories[i].equals(editingNote.category)) {
                                idx = i;
                                break;
                            }
                        }
                        spCategory.setSelection(idx);
                    }
                });
            }).start();
        }

        // 选择图片
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        // 拍照
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(NoteEditActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NoteEditActivity.this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                takePhoto();
            }
        });

        // 保存
        btnSave.setOnClickListener(v -> saveNote());
    }

    // 权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "请授予相机权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePhoto() {
        try {
            File photoFile = new File(getFilesDir(), System.currentTimeMillis() + "_photo.jpg");
            currentPhotoPath = photoFile.getAbsolutePath();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法启动相机", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                File file = new File(getFilesDir(), System.currentTimeMillis() + ".jpg");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.close();
                imagePaths.add(file.getAbsolutePath());
                imageAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (currentPhotoPath != null) {
                imagePaths.add(currentPhotoPath);
                imageAdapter.notifyDataSetChanged();
            }
        }
    }

    private void saveNote() {
        String content = etContent.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        // 多图：用分号拼接
        String imagePathStr = imagePaths.isEmpty() ? null : String.join(";", imagePaths);

        if (content.isEmpty() && (imagePathStr == null || imagePathStr.isEmpty())) {
            finish();
            return;
        }
        new Thread(() -> {
            if (noteId != -1 && editingNote != null) {
                // 编辑模式，更新
                editingNote.content = content;
                editingNote.imagePath = imagePathStr;
                editingNote.timestamp = System.currentTimeMillis();
                editingNote.category = category;
                NoteDatabase.getInstance(this).noteDao().update(editingNote);
            } else {
                // 新增模式
                Note note = new Note();
                note.content = content;
                note.imagePath = imagePathStr;
                note.timestamp = System.currentTimeMillis();
                note.category = category;
                NoteDatabase.getInstance(this).noteDao().insert(note);
            }
            runOnUiThread(this::finish);
        }).start();
    }
}