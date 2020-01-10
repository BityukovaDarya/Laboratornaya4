package ru.tpu.lab4;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.tpu.lab4.helpers.ItemTouchHelperAdapter;

public class RvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static final int TYPE_NUMBER = 0;
    private static final int TYPE_STUDENT = 1;

    private List<Student> students = new ArrayList<>();
    private onLongClickListener onLongClickListener;

    public interface onLongClickListener {
        void onLongClick(int index);
    }

    public RvAdapter(onLongClickListener listener) {
        super();
        this.onLongClickListener = listener;
    }

    @Override
    public void onItemDismiss(int position) {
        if (getItemViewType(position) == TYPE_STUDENT) {
            Student student = students.get((position + 1) / 2 - 1);
            students.remove(student);
            App.getInstance().getStudentsManager().removeStudent(student);
            notifyItemRemoved(position);
            notifyItemRemoved(position - 1);

            //обновляем номера
            for (int i = 0; i <= getItemCount(); i += 2) {
                notifyItemChanged(i);
            }
        } else {
            //возвращаем только что смахнутый номер
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NUMBER:
                return new NumberHolder(parent);
            case TYPE_STUDENT:
                StudentHolder studentHolder = new StudentHolder(parent);
                studentHolder.tvStudent.setOnLongClickListener((v) -> {
                    onLongClickListener.onLongClick((studentHolder.getAdapterPosition() + 1) / 2 - 1);
                    return true;
                });
                return studentHolder;
        }
        throw new IllegalArgumentException("Weird viewType = " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_NUMBER:
                NumberHolder numberHolder = (NumberHolder) holder;
                int studentNumber = (position + 1) / 2 + 1;
                numberHolder.tvNumber.setText(holder.itemView.getResources()
                        .getString(R.string.number, studentNumber));
                break;
            case TYPE_STUDENT:
                StudentHolder studentHolder = (StudentHolder) holder;
                Student student = students.get(position / 2);
                studentHolder.tvStudent.setText(student.lastName + " " + student.firstName + " "
                        + student.patronymic);
                if (!TextUtils.isEmpty(student.photoPath)) {
                    studentHolder.ivPhoto.setVisibility(View.VISIBLE);
                    studentHolder.ivPhoto.setImageURI(Uri.parse(
                            student.photoPath.substring(0, student.photoPath.length() - 4) +
                            "_small" +
                            student.photoPath.substring(student.photoPath.length() - 4)));
                } else {
                    studentHolder.ivPhoto.setVisibility(View.GONE);
                    studentHolder.ivPhoto.setImageURI(null);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return students.size() * 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? TYPE_NUMBER : TYPE_STUDENT;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public class NumberHolder extends RecyclerView.ViewHolder {

        final TextView tvNumber;

        NumberHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_number, parent, false));
            tvNumber = itemView.findViewById(R.id.tv_number);
        }
    }

    public class StudentHolder extends RecyclerView.ViewHolder {

        final TextView tvStudent;
        ImageView ivPhoto;

        public StudentHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_student, parent, false));
            tvStudent = itemView.findViewById(R.id.item_tv_student);
            ivPhoto = itemView.findViewById(R.id.item_iv_photo);
        }
    }
}
