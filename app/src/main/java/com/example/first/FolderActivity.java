package com.example.first;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.first.base.AppInfo;

import java.util.List;

public class FolderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<AppInfo> appInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        initData();
        initView();
    }

    private void initView() {
        recyclerView = findViewById(R.id.folderRv);
    }

    private void initData() {
    }

    public List<AppInfo> getAppInfo() {
        appInfoList = (List<AppInfo>) getIntent().getSerializableExtra("appList");
        return appInfoList;
    }
}