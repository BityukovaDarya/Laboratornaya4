package ru.tpu.lab4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.tpu.lab4.helpers.SQLiteHelper;

public class StudentsManager {

    private SQLiteHelper sqlHelper;
    private SQLiteDatabase db;

    public StudentsManager() {
        this.sqlHelper = new SQLiteHelper(App.getInstance());
    }

    public void open() {
        db = sqlHelper.getWritableDatabase();
    }

    public void close() {
        db.close();
    }

    public void addStudent(Student student) {
        db.insert(Student.TABLE_NAME, null, parseStudent(student));
    }

    public void updateStudent(Student student) {
        db.update(Student.TABLE_NAME, parseStudent(student), Student.ID + "=?",
                new String[]{String.valueOf(student.id)});
    }

    private ContentValues parseStudent(Student student) {
        ContentValues values = new ContentValues();
        values.put(Student.FIRST_NAME, student.firstName);
        values.put(Student.LAST_NAME, student.lastName);
        values.put(Student.PATRONYMIC, student.patronymic);
        values.put(Student.PHOTO_PATH, student.photoPath);
        return values;
    }

    private static final String[] ALL_COLUMNS = {
            Student.ID,
            Student.FIRST_NAME,
            Student.LAST_NAME,
            Student.PATRONYMIC,
            Student.PHOTO_PATH
    };

    public List<Student> getStudents() {
        Cursor cursor = db.query(Student.TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();
        List<Student> students = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            Student student = cursorToStudent(cursor);
            students.add(student);
            cursor.moveToNext();
        }
        cursor.close();
        Collections.sort(students);
        return students;
    }

    private Student cursorToStudent(Cursor cursor) {
        Student student = new Student();
        student.id = cursor.getLong(0);
        student.firstName = cursor.getString(1);
        student.lastName = cursor.getString(2);
        student.patronymic = cursor.getString(3);
        student.photoPath = cursor.getString(4);
        return student;
    }

    public void removeStudent(Student student) {
        db.delete(Student.TABLE_NAME, Student.ID + "=?", new String[]{String.valueOf(student.id)});
    }
}
