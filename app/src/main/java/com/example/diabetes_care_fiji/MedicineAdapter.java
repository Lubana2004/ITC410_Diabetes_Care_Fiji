package com.example.diabetes_care_fiji;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MedicineAdapter
        extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    public interface MedicineActionListener {
        void onEdit(MedicineItem medicine);
        void onDelete(MedicineItem medicine);
    }

    private final ArrayList<MedicineItem> medicines;
    private final MedicineActionListener listener;

    public MedicineAdapter(ArrayList<MedicineItem> medicines,
                           MedicineActionListener listener) {
        this.medicines = medicines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);

        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MedicineViewHolder holder, int position) {

        MedicineItem medicine = medicines.get(position);

        holder.tvName.setText(medicine.getName());
        holder.tvDosage.setText("Dosage: " + medicine.getDosage());
        holder.tvFrequency.setText(
                "Frequency: " + medicine.getFrequency());
        holder.tvTime.setText("Reminder: " + medicine.getTime());

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(medicine));

        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(medicine));
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvDosage, tvFrequency, tvTime;
        MaterialButton btnEdit, btnDelete;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(
                    R.id.tvItemMedicineName);
            tvDosage = itemView.findViewById(
                    R.id.tvItemDosage);
            tvFrequency = itemView.findViewById(
                    R.id.tvItemFrequency);
            tvTime = itemView.findViewById(
                    R.id.tvItemTime);

            btnEdit = itemView.findViewById(
                    R.id.btnEditMedicine);
            btnDelete = itemView.findViewById(
                    R.id.btnDeleteMedicine);
        }
    }
}