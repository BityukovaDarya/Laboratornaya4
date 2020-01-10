package ru.tpu.lab4.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.tpu.lab4.Student;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_FILE_NAME = "students.db";
    private static final int VERSION = 1;

    private static final String SQL_CREATE_TABLE_STUDENTS = "CREATE TABLE IF NOT EXISTS " +
            Student.TABLE_NAME + " ( " +
            Student.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Student.FIRST_NAME + " TEXT NOT NULL, " +
            Student.LAST_NAME + " TEXT NOT NULL, " +
            Student.PATRONYMIC + " TEXT NOT NULL, " +
            Student.PHOTO_PATH + " TEXT " + " );";

    public SQLiteHelper(Context context) {
        super(context, DB_FILE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_STUDENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Student.TABLE_NAME);
        onCreate(db);
    }
}
