package com.example.linkprojekt;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    Button btnAdd;
    EditText etNewSite;
    ArrayList<String> siteList;
    ArrayAdapter<String> adapter;
    SharedPreferences sharedPreferences;

    int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        etNewSite = findViewById(R.id.etNewSite);

        sharedPreferences = getSharedPreferences("amir", MODE_PRIVATE);

        Set<String> set = sharedPreferences.getStringSet("sites", new HashSet<>());
        siteList = new ArrayList<>(set);

        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.tvSiteName, siteList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
                }

                TextView tvName = convertView.findViewById(R.id.tvSiteName);
                TextView tvUrl = convertView.findViewById(R.id.tvSiteUrl);

                String item = getItem(position);
                if (item != null && item.contains("http")) {
                    String[] parts = item.split(" - ");
                    if (parts.length == 2) {
                        tvName.setText(parts[0].trim());
                        tvUrl.setText(parts[1].trim());
                    } else {
                        tvName.setText(item);
                        tvUrl.setText("");
                    }
                } else {
                    tvName.setText(item);
                    tvUrl.setText("");
                }

                return convertView;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String item = siteList.get(position);
            if (item.contains("http")) {
                String url = item.substring(item.indexOf("http"));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(this, "آدرس اشتباه است", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showEditDeleteDialog(position);
            return true;
        });

        btnAdd.setOnClickListener(v -> {
            String newSite = etNewSite.getText().toString().trim();
            if (!newSite.isEmpty()) {
                if (editPosition == -1) {
                    siteList.add(newSite);
                    saveData();
                } else {
                    siteList.set(editPosition, newSite);
                    editPosition = -1;
                    saveData();
                    btnAdd.setText("افزودن لینک");
                }
                etNewSite.setText("");
            } else {
                Toast.makeText(this, "آدرس را وارد کنید!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDeleteDialog(int position) {
        String[] options = {" ویرایش", " حذف"};
        new AlertDialog.Builder(this)
                .setTitle("یک گزینه را انتخاب کنید")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) startEditSite(position);
                    else if (which == 1) deleteSite(position);
                })
                .show();
    }

    private void startEditSite(int position) {
        etNewSite.setText(siteList.get(position));
        btnAdd.setText("ذخیره");
        editPosition = position;
    }

    private void deleteSite(int position) {
        siteList.remove(position);
        saveData();
        Toast.makeText(this, "آدرس حذف شد", Toast.LENGTH_SHORT).show();
    }

    private void saveData() {
        adapter.notifyDataSetChanged();
        Set<String> set = new HashSet<>(siteList);
        sharedPreferences.edit().putStringSet("sites", set).apply();
    }
}
