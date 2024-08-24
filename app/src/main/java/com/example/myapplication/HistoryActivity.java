package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

// HistoryActivity.java
public class HistoryActivity extends AppCompatActivity {

    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;

    private EditText inputEditText;
    private TextView translationTextView;
    private Button translateButton;
    private String currentInput = "";
    private String lastTranslatedText = "";
    private TranslationHistoryDbHelper dbHelper;
    private ListView historyListView;
    private CursorAdapter cursorAdapter; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new TranslationHistoryDbHelper(this);
        historyListView = findViewById(R.id.historyListView);

        Cursor historyCursor = dbHelper.getAllTranslations();

        String[] fromColumns = {"source_text", "translated_text"};
        int[] toViews = {R.id.historySourceText, R.id.historyTranslatedText};

        cursorAdapter = new SimpleCursorAdapter(this, // Use the member variable
                R.layout.list_item_history, historyCursor, fromColumns, toViews, 0);

        historyListView.setAdapter(cursorAdapter);
        registerForContextMenu(historyListView); // Register ListView for context menu
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.historyListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Cursor cursor = (Cursor) historyListView.getAdapter().getItem(info.position);
            if (cursor != null) {
                String translatedText = cursor.getString(cursor.getColumnIndexOrThrow("translated_text"));
                menu.add(Menu.NONE, 1, Menu.NONE, "Copy Translation");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Cursor cursor = (Cursor) historyListView.getAdapter().getItem(info.position);
            if (cursor != null) {
                String translatedText = cursor.getString(cursor.getColumnIndexOrThrow("translated_text"));

                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    ClipData clipData = ClipData.newPlainText("Translated Text", translatedText);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(this, "Translation copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }



    private void copyToClipboard(String text) {
        // Implement the clipboard copying logic here
    }

    public void onClearHistoryClick(View view) {
        // Clear the translation history
        dbHelper.clearTranslationHistory();
        updateHistoryList();
    }

    private void updateHistoryList() {
        Cursor updatedHistoryCursor = dbHelper.getAllTranslations();
        cursorAdapter.changeCursor(updatedHistoryCursor);
    }
}
