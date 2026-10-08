package algonquin.cst2335.androidlabs;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<TodoItem> todoList;
    private TodoAdapter adapter;
    private EditText editTextTodo;
    private SwitchCompat switchUrgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTodo = findViewById(R.id.editTextTodo);
        switchUrgent = findViewById(R.id.switchUrgent);
        Button btnAdd = findViewById(R.id.btnAdd);
        ListView listViewTodo = findViewById(R.id.listViewTodo);

        todoList = new ArrayList<>();
        adapter = new TodoAdapter();
        listViewTodo.setAdapter(adapter);

        // Add Button Listener
        btnAdd.setOnClickListener(v -> {
            String text = editTextTodo.getText().toString().trim();
            if (!text.isEmpty()) {
                boolean isUrgent = switchUrgent.isChecked();
                todoList.add(new TodoItem(text, isUrgent));

                editTextTodo.setText("");
                switchUrgent.setChecked(false);
                adapter.notifyDataSetChanged();
            }
        });

        // Click / Long-click row to delete via AlertDialog
        listViewTodo.setOnItemClickListener((parent, view, position, id) -> showDeleteDialog(position));
        listViewTodo.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setMessage(getString(R.string.dialog_message, position))
                .setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
                    todoList.remove(position);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    // BaseAdapter implementation
    private class TodoAdapter extends BaseAdapter {

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
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_todo, parent, false);
            }

            TextView todoText = view.findViewById(R.id.todoText);
            TodoItem item = (TodoItem) getItem(position);

            todoText.setText(item.getText());

            if (item.isUrgent()) {
                view.setBackgroundColor(Color.RED);
                todoText.setTextColor(Color.WHITE);
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
                todoText.setTextColor(Color.BLACK);
            }

            return view;
        }
    }
}