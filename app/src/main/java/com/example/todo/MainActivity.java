package com.example.todo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener {

    private static final String TAG = "Track Items";
    private SharedPreferences prefs;
    final ArrayList<String> listItems = new ArrayList<String>();
    final ArrayList<String> tempItems = new ArrayList<String>();
    private RecyclerView mainRecyclerView;
    private MainRecyclerAdapter mainRecyclerAdapter;
    private  ArrayList<MainModel> mainModelArrayList;
    private Button redoBtn;
    private Button addBtn;
    private Button settingBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainRecyclerView = findViewById(R.id.my_recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mainRecyclerView.setLayoutManager(linearLayoutManager);

        redoBtn = findViewById(R.id.restore_btn);
        addBtn = findViewById(R.id.add_btn);
        settingBtn = findViewById(R.id.setting_btn);
        redoBtn.setBackgroundResource(R.color.colorPrimary);
        addBtn.setBackgroundResource(R.color.colorPrimary);
        settingBtn.setBackgroundResource(R.color.colorPrimary);

        // Saved values
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String csvList = prefs.getString("myProgress", null);
        if(csvList != null && !csvList.isEmpty()){
            String[] items = csvList.split(",");
            if(items.length >= 1){
                for(int i=0; i < items.length; i++){
                    listItems.add(items[i]);
                }
            }
        }

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(listItems, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(listItems, i, i - 1);
                    }
                }
                startAdapter();
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT || direction == ItemTouchHelper.LEFT ){
                    tempItems.add(listItems.get(position));
                    listItems.remove(listItems.get(position));
                    startAdapter();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mainRecyclerView);

        startAdapter();
    }

    private void startAdapter(){
        mainModelArrayList = prepareList();
        mainRecyclerAdapter = new MainRecyclerAdapter(this, mainModelArrayList);
        mainRecyclerAdapter.setOnRecyclerViewItemClickListener(this);
        mainRecyclerView.setAdapter(mainRecyclerAdapter);
        Log.d("test", "Start Adapter....");
    }

    private ArrayList<MainModel> prepareList() {
        ArrayList<MainModel> mainModelList = new ArrayList<>();
        for (int i = 0; i < listItems.size(); i++) {
            MainModel mainModel = new MainModel();
            mainModel.setOfferName(listItems.get(i));
            mainModelList.add(mainModel);
        }
        return mainModelList;
    }

    @Override
    public void onItemClick(final int position, View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_edit_view, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.editItem);

        dialogBuilder.setTitle("Edit Task");
        dialogBuilder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String insertedValue = edt.getText().toString();

                if(insertedValue.length() != 0){
                    listItems.set(position, edt.getText().toString());
                    startAdapter();
                }else{
                    Toast.makeText(MainActivity.this, "Enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    public void restoreItem(View v){
        if(tempItems != null && !tempItems.isEmpty()) {
            listItems.add(tempItems.get(0));
            tempItems.remove(tempItems.get(0));
            startAdapter();
        }
    }


    public void addItem(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_add_view, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.newItem);

        dialogBuilder.setTitle("New Task");
        dialogBuilder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String insertedValue = edt.getText().toString();

                if(insertedValue.length() != 0){
                    listItems.add(insertedValue);
                    startAdapter();
                }else{
                    Toast.makeText(MainActivity.this, "Enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void settingButtonClicked(View v) {
        startActivityForResult(new Intent(this, UserSettingActivity.class), 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "INSIDE: onDestroy");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Text_Name", null);

        SharedPreferences.Editor editPrefs = prefs.edit();
        StringBuilder csvList = new StringBuilder();
        if(listItems != null && !listItems.isEmpty()) {
            for (String s : listItems) {
                csvList.append(s);
                csvList.append(",");
            }
            editPrefs.putString("myProgress", csvList.toString());
            editPrefs.commit();
            editor.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "INSIDE: onPause");
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Text_Name", null);

        SharedPreferences.Editor editPrefs = prefs.edit();
        StringBuilder csvList = new StringBuilder();
        if(listItems != null && !listItems.isEmpty()) {
            for (String s : listItems) {
                csvList.append(s);
                csvList.append(",");
            }
            editPrefs.putString("myProgress", csvList.toString());
            editPrefs.commit();
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "INSIDE: onResume");
        try{
            String csvList = prefs.getString("Text_Name", null);
            if(csvList != null && !csvList.isEmpty()){
                String[] items = csvList.split(",");
                if(items.length >= 1){

                    for(int i=0; i < items.length; i++){
                        listItems.add(items[i]);
                    }
                    startAdapter();
                }
            }
        }catch (Exception e){
            Log.d(TAG, "INSIDE: No extra string");
        }
    }
}
