package com.example.diabetes_care_fiji;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MedicineDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DiabetesCare.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MEDICINES = "medicines";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DOSAGE = "dosage";
    private static final String COL_FREQUENCY = "frequency";
    private static final String COL_TIME = "time";

    public MedicineDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MEDICINES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_DOSAGE + " TEXT NOT NULL, " +
                COL_FREQUENCY + " TEXT NOT NULL, " +
                COL_TIME + " TEXT NOT NULL)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        // Preserve existing medicines during upgrades.
    }

    public boolean insertMedicine(String name, String dosage,
                                  String frequency, String time) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DOSAGE, dosage);
        values.put(COL_FREQUENCY, frequency);
        values.put(COL_TIME, time);

        return db.insert(TABLE_MEDICINES, null, values) != -1;
    }

    public ArrayList<MedicineItem> getAllMedicines() {
        ArrayList<MedicineItem> medicines = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_MEDICINES,
                new String[]{
                        COL_ID,
                        COL_NAME,
                        COL_DOSAGE,
                        COL_FREQUENCY,
                        COL_TIME
                },
                null, null, null, null,
                COL_ID + " DESC")) {

            while (cursor.moveToNext()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_ID));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME));

                String dosage = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DOSAGE));

                String frequency = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_FREQUENCY));

                String time = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_TIME));

                medicines.add(new MedicineItem(
                        id, name, dosage, frequency, time));
            }
        }

        return medicines;
    }

    public boolean updateMedicine(long id, String name, String dosage,
                                  String frequency, String time) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DOSAGE, dosage);
        values.put(COL_FREQUENCY, frequency);
        values.put(COL_TIME, time);

        return db.update(
                TABLE_MEDICINES,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public boolean deleteMedicine(long id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_MEDICINES,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }
}