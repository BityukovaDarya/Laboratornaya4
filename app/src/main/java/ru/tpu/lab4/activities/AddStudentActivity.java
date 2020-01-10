package ru.tpu.lab4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import ru.tpu.lab4.Const;
import ru.tpu.lab4.R;
import ru.tpu.lab4.Student;

import static ru.tpu.lab4.Const.EXTRA_STUDENT;
import static ru.tpu.lab4.Const.REQUEST_CAMERA;

public class AddStudentActivity extends AppCompatActivity {

    private static final String PREFS_NAME      = "prefs";
    private static final String PREF_ID         = "id";
    private static final String PREF_FIRST_NAME = "first_name";
    private static final String PREF_LAST_NAME  = "last_name";
    private static final String PREF_PATRONYMIC = "patronymic";
    private static final String PREF_PHOTO_PATH = "photo_path";

    EditText etFirstName;
    EditText etLastName;
    EditText etPatronymic;
    TextInputLayout tilFirstName;
    TextInputLayout tilLastName;
    TextInputLayout tilPatronymic;
    ImageView photo;

    String photoPath;
    boolean skipSaveToPrefs = false;
    long studentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        bindViews();

        Student student = getIntent().getParcelableExtra(EXTRA_STUDENT);
        if (student != null) {
            etFirstName.setText(student.firstName);
            etLastName.setText(student.lastName);
            etPatronymic.setText(student.patronymic);
            photoPath = student.photoPath;
            studentId = student.id;
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            studentId = prefs.getLong(PREF_ID, -1);
            etFirstName.setText(prefs.getString(PREF_FIRST_NAME, ""));
            etLastName.setText(prefs.getString(PREF_LAST_NAME, ""));
            etPatronymic.setText(prefs.getString(PREF_PATRONYMIC, ""));
            photoPath = prefs.getString(PREF_PHOTO_PATH, null);
        }
        if (photoPath != null) {
            photo.setImageURI(Uri.parse(photoPath));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!skipSaveToPrefs) {
            saveInfoInPrefs();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        skipSaveToPrefs = true;
        clearPrefs();
    }

    private void bindViews() {
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etPatronymic = findViewById(R.id.et_patronymic);
        tilFirstName = findViewById(R.id.input_first_name);
        tilLastName = findViewById(R.id.input_last_name);
        tilPatronymic = findViewById(R.id.input_patronymic);
        photo = findViewById(R.id.iv_photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_student, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                if (validate()) {
                    Student student = new Student();
                    student.id = studentId;
                    student.firstName = etFirstName.getText().toString();
                    student.lastName = etLastName.getText().toString();
                    student.patronymic = etPatronymic.getText().toString();
                    student.photoPath = photoPath;

                    skipSaveToPrefs = true;
                    clearPrefs();

                    Intent intent = getIntent();
                    intent.putExtra(EXTRA_STUDENT, student);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.action_add_photo:
                try {
                    File tempFile = createTempFile();
                    photoPath = tempFile.getPath();
                    Uri photoUri = FileProvider.getUriForFile(this,
                            Const.FILE_PROVIDER_AUTHORITY,
                            tempFile);
                    Intent requestPhotoIntent = requestPhotoIntent(photoUri);
                    startActivityForResult(requestPhotoIntent, REQUEST_CAMERA);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.create_file_error),
                            Toast.LENGTH_SHORT).show();
                    photoPath = null;
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            try {
                Bitmap scaledPhoto = getScaledBitmap(photoPath, 1024, 1024);
                scaledPhoto = rotateBitmapIfNeeded(scaledPhoto, photoPath);
                saveBitmapToFile(scaledPhoto, photoPath);
                int smallSize = getSmallImageSize();
                Bitmap smallPhoto = getScaledBitmap(photoPath, smallSize, smallSize);
                String smallPhotoPath = photoPath.substring(0, photoPath.length() - 4) +
                        "_small" +
                        photoPath.substring(photoPath.length() - 4);
                saveBitmapToFile(smallPhoto, smallPhotoPath);

                photo.setImageURI(Uri.parse(photoPath));
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.get_photo_error),
                        Toast.LENGTH_SHORT).show();
                photoPath = null;
                photo.setImageURI(null);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private int getSmallImageSize() {
        //converts 48dp to px
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (48 * scale + 0.5f);
    }

    private boolean validate() {
        boolean valid = true;
        if (etFirstName.getText().toString().isEmpty()) {
            tilFirstName.setError(getResources().getString(R.string.empty));
            valid = false;
        }
        if (etLastName.getText().toString().isEmpty()) {
            tilLastName.setError(getResources().getString(R.string.empty));
            valid = false;
        }
        if (etPatronymic.getText().toString().isEmpty()) {
            tilPatronymic.setError(getResources().getString(R.string.empty));
            valid = false;
        }
        return valid;
    }

    private Intent requestPhotoIntent(Uri photoFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
        return intent;
    }

    private File createTempFile() throws IOException {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private Bitmap getScaledBitmap(String filePath, int maxWidth, int maxHeight)
            throws FileNotFoundException {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int w = bmOptions.outWidth;
        int h = bmOptions.outHeight;

        float scaleFactor = Math.max(w / maxWidth, h / maxHeight);
        scaleFactor = scaleFactor < 1 ? 1 : scaleFactor;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;
        return BitmapFactory.decodeFile(filePath, bmOptions);
    }

    private String saveBitmapToFile(Bitmap bitmap, String filePath) throws IOException {
        File file = new File(filePath);
        OutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            return file.getPath();
        } finally {
            if (fOut != null) {
                fOut.close();
            }
        }
    }

    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, String filePath) throws IOException {
        ExifInterface exif = new ExifInterface(filePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        int rotateBy = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotateBy = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotateBy = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotateBy = 270;
                break;
        }
        if (rotateBy == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateBy);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return newBitmap;
    }

    private void saveInfoInPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(PREF_ID, studentId)
                .putString(PREF_FIRST_NAME, etFirstName.getText().toString())
                .putString(PREF_LAST_NAME, etLastName.getText().toString())
                .putString(PREF_PATRONYMIC, etPatronymic.getText().toString())
                .putString(PREF_PHOTO_PATH, photoPath)
                .apply();
    }

    private void clearPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
