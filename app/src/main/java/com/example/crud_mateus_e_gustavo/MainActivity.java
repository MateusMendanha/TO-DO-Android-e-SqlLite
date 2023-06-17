package com.example.crud_mateus_e_gustavo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText taskEditText;
    private ListView taskListView;
    private ArrayAdapter<String> taskArrayAdapter;
    private List<String> taskList;
    private DatabaseHelper databaseHelper;
    private int selectedTaskIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskEditText = findViewById(R.id.taskEditText);
        taskListView = findViewById(R.id.taskListView);

        taskList = new ArrayList<>();
        taskArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        taskListView.setAdapter(taskArrayAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                taskEditText.setText(taskList.get(position));
                selectedTaskIndex = position;
            }
        });

        databaseHelper = new DatabaseHelper(this);
        loadTasksFromDatabase();
    }

    public void addTask(View view) {
        String description = taskEditText.getText().toString().trim();
        if (!description.isEmpty()) {
            long taskId = addTaskToDatabase(description);
            if (taskId != -1) {
                taskList.add(description);
                taskArrayAdapter.notifyDataSetChanged();
                taskEditText.setText("");
            } else {
                Toast.makeText(this, "Failed to add task.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a task description.", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateTask(View view) {
        String description = taskEditText.getText().toString().trim();
        if (!description.isEmpty() && selectedTaskIndex != -1) {
            String oldDescription = taskList.get(selectedTaskIndex);
            boolean success = updateTaskInDatabase(oldDescription, description);
            if (success) {
                taskList.set(selectedTaskIndex, description);
                taskArrayAdapter.notifyDataSetChanged();
                taskEditText.setText("");
                selectedTaskIndex = -1;
            } else {
                Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a task and enter a new description.", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteTask(View view) {
        if (selectedTaskIndex != -1) {
            String description = taskList.get(selectedTaskIndex);
            boolean success = deleteTaskFromDatabase(description);
            if (success) {
                taskList.remove(selectedTaskIndex);
                taskArrayAdapter.notifyDataSetChanged();
                taskEditText.setText("");
                selectedTaskIndex = -1;
            } else {
                Toast.makeText(this, "Failed to delete task.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select a task to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTasksFromDatabase() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        taskList.clear();

        while (cursor.moveToNext()) {
            String description = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION));
            taskList.add(description);
        }

        cursor.close();
        db.close();
        taskArrayAdapter.notifyDataSetChanged();
    }

    private long addTaskToDatabase(String description) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, description);

        long taskId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        db.close();
        return taskId;
    }

    private boolean updateTaskInDatabase(String oldDescription, String newDescription) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, newDescription);

        int rowsAffected = db.update(TaskContract.TaskEntry.TABLE_NAME, values,
                TaskContract.TaskEntry.COLUMN_DESCRIPTION + " = ?", new String[]{oldDescription});

        db.close();
        return rowsAffected > 0;
    }

    private boolean deleteTaskFromDatabase(String description) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int rowsAffected = db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry.COLUMN_DESCRIPTION + " = ?", new String[]{description});
        db.close();
        return rowsAffected > 0;
    }
}
