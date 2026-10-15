package algonquin.cst2335.androidlabs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<TodoItem> todoList = new ArrayList<>();
    private MyListAdapter myAdapter;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize SQLite Database
        MyOpener dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase();

        // 2. Query all rows and print cursor information to Logcat
        Cursor cursor = db.query(false, MyOpener.TABLE_NAME, null, null, null, null, null, null, null);
        printCursor(cursor);

        // 3. Load existing saved todos into the list
        if (cursor != null) {
            int idColIdx = cursor.getColumnIndex(MyOpener.COL_ID);
            int todoColIdx = cursor.getColumnIndex(MyOpener.COL_TODO);
            int urgentColIdx = cursor.getColumnIndex(MyOpener.COL_IS_URGENT);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColIdx);
                String text = cursor.getString(todoColIdx);
                boolean isUrgent = cursor.getInt(urgentColIdx) == 1;
                todoList.add(new TodoItem(id, text, isUrgent));
            }
            cursor.close();
        }

        // 4. UI Views
        ListView listView = findViewById(R.id.listViewTodo);
        EditText editText = findViewById(R.id.editTextTodo);
        SwitchCompat urgentSwitch = findViewById(R.id.switchUrgent);
        Button addButton = findViewById(R.id.btnAdd);

        myAdapter = new MyListAdapter();
        listView.setAdapter(myAdapter);

        // 5. Add button: Insert into SQLite and refresh list
        addButton.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (!text.isEmpty()) {
                boolean isUrgent = urgentSwitch.isChecked();

                ContentValues cValues = new ContentValues();
                cValues.put(MyOpener.COL_TODO, text);
                cValues.put(MyOpener.COL_IS_URGENT, isUrgent ? 1 : 0);

                long newId = db.insert(MyOpener.TABLE_NAME, null, cValues);

                todoList.add(new TodoItem(newId, text, isUrgent));
                myAdapter.notifyDataSetChanged();

                editText.setText("");
                urgentSwitch.setChecked(false);
            }
        });

        // 6. Long click: Confirm deletion and remove from SQLite & list
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            TodoItem itemToRemove = todoList.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.delete_title)
                    .setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_ID + " = ?", new String[]{String.valueOf(itemToRemove.getId())});
                        todoList.remove(position);
                        myAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

            return true;
        });
    }

    private void printCursor(Cursor c) {
        String TAG = "Lab5_Cursor";

        Log.i(TAG, "Database Version: " + db.getVersion());

        if (c == null) return;

        int colCount = c.getColumnCount();
        Log.i(TAG, "Number of columns: " + colCount);

        String[] colNames = c.getColumnNames();
        StringBuilder namesStr = new StringBuilder("Columns: ");
        for (String col : colNames) {
            namesStr.append(col).append(" | ");
        }
        Log.i(TAG, namesStr.toString());

        Log.i(TAG, "Row count: " + c.getCount());

        c.moveToPosition(-1);
        while (c.moveToNext()) {
            StringBuilder rowStr = new StringBuilder("Row: ");
            for (int i = 0; i < colCount; i++) {
                rowStr.append(colNames[i]).append(" = ").append(c.getString(i)).append(" ; ");
            }
            Log.i(TAG, rowStr.toString());
        }
    }

    private class MyListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return todoList.size();
        }

        @Override
        public Object getItem(int position) {
            return todoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return todoList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.row_todo, parent, false);
            }

            TextView textView = view.findViewById(R.id.todo_text);
            TodoItem item = (TodoItem) getItem(position);
            textView.setText(item.getText());

            if (item.isUrgent()) {
                textView.setBackgroundColor(Color.RED);
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setBackgroundColor(Color.WHITE);
                textView.setTextColor(Color.BLACK);
            }

            return view;
        }
    }
}