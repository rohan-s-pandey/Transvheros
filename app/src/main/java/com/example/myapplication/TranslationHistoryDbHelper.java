package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// TranslationHistoryDbHelper.java
public class TranslationHistoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TranslationHistory.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "translation_history"; // Add this line

    public TranslationHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the translation history table
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "source_text TEXT," +
                "translated_text TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
        // For simplicity, you can drop and recreate the table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Define methods to insert and query translation history
    public long insertTranslation(String sourceText, String translatedText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("source_text", sourceText);
        values.put("translated_text", translatedText);
        long newRowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    public Cursor getAllTranslations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, "_id DESC");
    }

    public void clearTranslationHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
