package ru.tpu.lab4.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.tpu.lab4.App;
import ru.tpu.lab4.R;
import ru.tpu.lab4.RvAdapter;
import ru.tpu.lab4.Student;
import ru.tpu.lab4.StudentsManager;
import ru.tpu.lab4.helpers.SimpleItemTouchHelperCallback;

import static ru.tpu.lab4.Const.CREATING;
import static ru.tpu.lab4.Const.EDITING;
import static ru.tpu.lab4.Const.EXTRA_STUDENT;

public class MainActivity extends AppCompatActivity implements RvAdapter.onLongClickListener {

    private RecyclerView rv;
    StudentsManager studentsManager;
    private RvAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = findViewById(R.id.rv);
        FloatingActionButton fab = findViewById(R.id.fab);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        studentsManager = App.getInstance().getStudentsManager();
        studentsManager.open();
        rv.setAdapter(rvAdapter = new RvAdapter(this));
        rvAdapter.setStudents(studentsManager.getStudents());

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(rvAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rv);

        fab.setOnClickListener((v) ->
                startActivityForResult(new Intent(getBaseContext(), AddStudentActivity.class), CREATING));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        studentsManager.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Student student = data.getParcelableExtra(EXTRA_STUDENT);
            switch (requestCode) {
                case CREATING:
                    studentsManager.addStudent(student);
                    rvAdapter.setStudents(studentsManager.getStudents());
                    rvAdapter.notifyDataSetChanged();
                    rv.scrollToPosition(rvAdapter.getItemCount() - 1);
                    break;
                case EDITING:
                    studentsManager.updateStudent(student);
                    rvAdapter.setStudents(studentsManager.getStudents());
                    rvAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLongClick(int index) {
        Student student = studentsManager.getStudents().get(index);
        Intent intent = new Intent(getBaseContext(), AddStudentActivity.class);
        intent.putExtra(EXTRA_STUDENT, student);
        startActivityForResult(intent, EDITING);
    }
}
