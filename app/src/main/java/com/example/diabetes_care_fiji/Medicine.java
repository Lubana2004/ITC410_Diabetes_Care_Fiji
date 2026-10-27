package com.example.diabetes_care_fiji;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Medicine extends AppCompatActivity
        implements MedicineAdapter.MedicineActionListener {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;

    private EditText etMedicineName;
    private EditText etDosage;
    private EditText etFrequency;
    private EditText etTime;

    private RecyclerView rvMedicines;
    private TextView tvMedicineCount;
    private MaterialButton btnAddMedicine;
    private ImageButton btnBack;

    private MedicineAdapter medicineAdapter;
    private ArrayList<MedicineItem> medicineList;
    private MedicineDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // Status bar appearance.
        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowCompat.getInsetsController(
                    window,
                    window.getDecorView()
            ).setAppearanceLightStatusBars(true);
        }

        setContentView(R.layout.activity_medicine);

        // Handle system bar spacing.
        android.view.View root = findViewById(R.id.main);

        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    root,
                    (view, windowInsets) -> {

                        Insets systemBars = windowInsets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        );

                        view.setPadding(
                                view.getPaddingLeft(),
                                systemBars.top,
                                view.getPaddingRight(),
                                systemBars.bottom
                        );

                        return windowInsets;
                    }
            );

            ViewCompat.requestApplyInsets(root);
        }

        // Initialize form fields.
        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etTime = findViewById(R.id.etTime);

        // Initialize list and buttons.
        rvMedicines = findViewById(R.id.rvMedicines);
        tvMedicineCount = findViewById(R.id.tvMedicineCount);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnBack = findViewById(R.id.btnBack);

        // Initialize SQLite database.
        databaseHelper = new MedicineDatabaseHelper(this);

        medicineList = new ArrayList<>();

        medicineAdapter = new MedicineAdapter(
                medicineList,
                this
        );

        rvMedicines.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvMedicines.setAdapter(medicineAdapter);

        // Set up reminder time picker.
        etTime.setFocusable(false);
        etTime.setClickable(true);

        etTime.setOnClickListener(
                v -> showTimePicker(etTime)
        );

        // Button actions.
        btnAddMedicine.setOnClickListener(
                v -> addMedicine()
        );

        btnBack.setOnClickListener(
                v -> finish()
        );

        // Request notification permission if required.
        requestNotificationPermission();

        // Load saved medicines and schedule their reminders.
        loadMedicines();
    }

    // ---------------------------------------------------------
    // NOTIFICATION PERMISSION
    // ---------------------------------------------------------

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_REQUEST
                );
            }
        }
    }

    // ---------------------------------------------------------
    // EXACT ALARM PERMISSION
    // ---------------------------------------------------------

    private void requestExactAlarmPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(ALARM_SERVICE);

            if (alarmManager != null
                    && !alarmManager.canScheduleExactAlarms()) {

                new AlertDialog.Builder(this)
                        .setTitle("Allow Medicine Reminders")
                        .setMessage(
                                "To schedule medicine reminders at "
                                        + "your selected times, allow "
                                        + "Alarms and reminders in Settings."
                        )
                        .setPositiveButton(
                                "Open Settings",
                                (dialog, which) -> {

                                    Intent intent = new Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.parse(
                                                    "package:" + getPackageName()
                                            )
                                    );

                                    try {
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        Toast.makeText(
                                                this,
                                                "Please enable Alarms and reminders in Settings.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                        )
                        .setNegativeButton("Later", null)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Recheck exact-alarm access when returning from Settings.
        if (databaseHelper != null) {
            loadMedicines();
        }
    }

    // ---------------------------------------------------------
    // TIME PICKER
    // ---------------------------------------------------------

    private void showTimePicker(EditText target) {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {

                    Calendar selectedTime = Calendar.getInstance();

                    selectedTime.set(
                            Calendar.HOUR_OF_DAY,
                            selectedHour
                    );

                    selectedTime.set(
                            Calendar.MINUTE,
                            selectedMinute
                    );

                    SimpleDateFormat format =
                            new SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.getDefault()
                            );

                    target.setText(
                            format.format(selectedTime.getTime())
                    );
                },
                hour,
                minute,
                false
        );

        dialog.show();
    }

    // ---------------------------------------------------------
    // ADD MEDICINE
    // ---------------------------------------------------------

    private void addMedicine() {

        String name = etMedicineName.getText()
                .toString().trim();

        String dosage = etDosage.getText()
                .toString().trim();

        String frequency = etFrequency.getText()
                .toString().trim();

        String time = etTime.getText()
                .toString().trim();

        if (name.isEmpty()
                || dosage.isEmpty()
                || frequency.isEmpty()
                || time.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all medicine fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        boolean saved = databaseHelper.insertMedicine(
                name,
                dosage,
                frequency,
                time
        );

        if (saved) {

            // Reload medicines and schedule reminders.
            loadMedicines();

            etMedicineName.setText("");
            etDosage.setText("");
            etFrequency.setText("");
            etTime.setText("");

            Toast.makeText(
                    this,
                    "Medicine saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

            // Request exact-alarm access when the user first
            // saves a medicine, rather than at every screen opening.
            requestExactAlarmPermission();

        } else {

            Toast.makeText(
                    this,
                    "Could not save medicine",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ---------------------------------------------------------
    // LOAD MEDICINES AND SCHEDULE REMINDERS
    // ---------------------------------------------------------

    private void loadMedicines() {

        if (databaseHelper == null
                || medicineList == null
                || medicineAdapter == null) {
            return;
        }

        medicineList.clear();

        medicineList.addAll(
                databaseHelper.getAllMedicines()
        );

        medicineAdapter.notifyDataSetChanged();

        if (tvMedicineCount != null) {
            tvMedicineCount.setText(
                    "Medicines: " + medicineList.size()
            );
        }

        // Schedule daily reminders for saved medicines.
        for (MedicineItem medicine : medicineList) {

            MedicineReminderScheduler.scheduleDaily(
                    this,
                    medicine.getId(),
                    medicine.getName(),
                    medicine.getDosage(),
                    medicine.getTime()
            );
        }
    }

    // ---------------------------------------------------------
    // EDIT MEDICINE
    // ---------------------------------------------------------

    @Override
    public void onEdit(MedicineItem medicine) {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding = (int) (
                20 * getResources()
                        .getDisplayMetrics()
                        .density
        );

        layout.setPadding(
                padding,
                padding,
                padding,
                0
        );

        EditText nameInput = createInput(
                "Medicine name",
                medicine.getName()
        );

        EditText dosageInput = createInput(
                "Dosage",
                medicine.getDosage()
        );

        EditText frequencyInput = createInput(
                "Frequency",
                medicine.getFrequency()
        );

        EditText timeInput = createInput(
                "Reminder time",
                medicine.getTime()
        );

        timeInput.setFocusable(false);
        timeInput.setClickable(true);

        timeInput.setOnClickListener(
                v -> showTimePicker(timeInput)
        );

        layout.addView(nameInput);
        layout.addView(dosageInput);
        layout.addView(frequencyInput);
        layout.addView(timeInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Medicine")
                .setView(layout)
                .setPositiveButton("Save Changes", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(ignored -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String name = nameInput.getText()
                        .toString().trim();

                String dosage = dosageInput.getText()
                        .toString().trim();

                String frequency = frequencyInput.getText()
                        .toString().trim();

                String time = timeInput.getText()
                        .toString().trim();

                if (name.isEmpty()
                        || dosage.isEmpty()
                        || frequency.isEmpty()
                        || time.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Please fill in all medicine fields",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                boolean updated = databaseHelper.updateMedicine(
                        medicine.getId(),
                        name,
                        dosage,
                        frequency,
                        time
                );

                if (updated) {

                    // Reschedule using the updated information.
                    MedicineReminderScheduler.cancel(
                            this,
                            medicine.getId()
                    );

                    loadMedicines();

                    Toast.makeText(
                            this,
                            "Medicine updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();

                } else {

                    Toast.makeText(
                            this,
                            "Could not update medicine",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });

        dialog.show();
    }

    private EditText createInput(
            String hint,
            String value
    ) {

        EditText input = new EditText(this);

        input.setHint(hint);
        input.setText(value);
        input.setSingleLine(true);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT
        );

        return input;
    }

    // ---------------------------------------------------------
    // DELETE MEDICINE
    // ---------------------------------------------------------

    @Override
    public void onDelete(MedicineItem medicine) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage(
                        "Are you sure you want to delete "
                                + medicine.getName() + "?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            boolean deleted =
                                    databaseHelper.deleteMedicine(
                                            medicine.getId()
                                    );

                            if (deleted) {

                                // Cancel the scheduled reminder.
                                MedicineReminderScheduler.cancel(
                                        this,
                                        medicine.getId()
                                );

                                // Refresh the list.
                                loadMedicines();

                                Toast.makeText(
                                        this,
                                        "Medicine deleted successfully",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        this,
                                        "Could not delete medicine",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }
}
