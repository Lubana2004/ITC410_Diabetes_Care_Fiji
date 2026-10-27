
package com.example.diabetes_care_fiji;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BloodSugarTracker extends AppCompatActivity {

    private EditText etBloodSugar, etTime, etDate, etNotes;
    private TextView tvReadingCount;
    private LinearLayout readingList;

    private JSONArray readings;

    private static final String PREFS = "blood_tracker_data";
    private static final String KEY_READINGS = "readings";

    private static final int TEAL = Color.rgb(0, 121, 107);
    private static final int DARK_TEAL = Color.rgb(0, 105, 92);
    private static final int RED = Color.rgb(198, 40, 40);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bloodsugartracker);

        View mainView = findViewById(R.id.main);

        ImageButton tvBack = findViewById(R.id.tvBack);

        tvBack.setOnClickListener(v -> finish());

        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            });
        }

        etBloodSugar = findViewById(R.id.etBloodSugar);
        etTime = findViewById(R.id.etTime);
        etDate = findViewById(R.id.etDate);
        etNotes = findViewById(R.id.etNotes);

        tvReadingCount = findViewById(R.id.tvReadingCount);
        readingList = findViewById(R.id.readingList);

        Button btnAddReading = findViewById(R.id.btnAddReading);

        etDate.setText(getToday());
        etDate.setOnClickListener(v -> showDatePicker());

        tvBack.setOnClickListener(v -> finish());
        btnAddReading.setOnClickListener(v -> addReading());

        loadReadings();
        displayReadings();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        try {
            String[] parts = etDate.getText().toString().split("/");

            if (parts.length == 3) {
                calendar.set(
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[0])
                );
            }
        } catch (Exception ignored) {
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    etDate.setText(String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            day,
                            month + 1,
                            year
                    ));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void addReading() {
        String glucoseText = etBloodSugar.getText().toString().trim();
        String timing = etTime.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(glucoseText)) {
            etBloodSugar.setError("Enter your blood sugar level");
            etBloodSugar.requestFocus();
            return;
        }

        if (!isValidGlucose(glucoseText)) {
            etBloodSugar.setError("Enter a valid measurement");
            etBloodSugar.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(timing)) {
            etTime.setError("Enter the meal timing");
            etTime.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Select a date");
            return;
        }

        try {
            JSONObject reading = new JSONObject();
            reading.put("glucose", glucoseText);
            reading.put("timing", timing);
            reading.put("date", date);
            reading.put("notes", notes);
            reading.put("recordedAt", System.currentTimeMillis());

            readings.put(reading);

            saveReadings();
            displayReadings();

            etBloodSugar.setText("");
            etTime.setText("");
            etNotes.setText("");
            etDate.setText(getToday());

            Toast.makeText(
                    this,
                    "Reading saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (JSONException e) {
            Toast.makeText(
                    this,
                    "Unable to save reading",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private boolean isValidGlucose(String value) {
        try {
            double glucose = Double.parseDouble(value);

            return !Double.isNaN(glucose)
                    && !Double.isInfinite(glucose)
                    && glucose > 0
                    && glucose <= 100;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void loadReadings() {
        String savedData = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_READINGS, "[]");

        try {
            readings = new JSONArray(savedData);
        } catch (JSONException e) {
            readings = new JSONArray();
        }
    }

    private void saveReadings() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_READINGS, readings.toString())
                .apply();
    }

    private void displayReadings() {
        if (readingList == null || tvReadingCount == null) {
            return;
        }

        readingList.removeAllViews();

        int total = readings.length();

        tvReadingCount.setText(
                total + (total == 1 ? " reading" : " readings")
        );

        for (int i = total - 1; i >= 0; i--) {
            JSONObject reading = readings.optJSONObject(i);

            if (reading == null) {
                continue;
            }

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            card.setBackgroundResource(R.drawable.bg_white_card);
            card.setElevation(dp(1));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            cardParams.bottomMargin = dp(12);
            card.setLayoutParams(cardParams);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            View accent = new View(this);
            accent.setBackgroundColor(
                    getAccentColor(reading.optString("glucose"))
            );

            row.addView(
                    accent,
                    new LinearLayout.LayoutParams(dp(4), dp(42))
            );

            LinearLayout details = new LinearLayout(this);
            details.setOrientation(LinearLayout.VERTICAL);
            details.setPadding(dp(12), 0, 0, 0);

            row.addView(
                    details,
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            TextView glucose = new TextView(this);
            glucose.setText(
                    reading.optString("glucose") + " mmol/L"
            );
            glucose.setTextColor(0xFF183B46);
            glucose.setTextSize(20);
            glucose.setTypeface(null, Typeface.BOLD);

            details.addView(glucose);

            TextView timing = new TextView(this);
            timing.setText(reading.optString("timing"));
            timing.setTextColor(0xFF71838C);
            timing.setTextSize(12);

            details.addView(timing);
            card.addView(row);

            TextView date = new TextView(this);
            date.setText(reading.optString("date"));
            date.setTextColor(0xFF71838C);
            date.setTextSize(12);
            date.setPadding(0, dp(12), 0, 0);

            card.addView(date);

            String notes = reading.optString("notes");

            if (!TextUtils.isEmpty(notes)) {
                TextView note = new TextView(this);
                note.setText("Notes: " + notes);
                note.setTextColor(0xFF405761);
                note.setTextSize(13);
                note.setPadding(0, dp(8), 0, 0);

                card.addView(note);
            }

            // Edit and Delete buttons
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            actions.setPadding(0, dp(12), 0, 0);

            Button editButton = new Button(this);
            editButton.setText("Edit");
            styleActionButton(editButton, true);

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            styleActionButton(deleteButton, false);

            LinearLayout.LayoutParams editParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            dp(44)
                    );

            editParams.leftMargin = dp(6);

            LinearLayout.LayoutParams deleteParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            dp(44)
                    );

            deleteParams.leftMargin = dp(6);

            actions.addView(editButton, editParams);
            actions.addView(deleteButton, deleteParams);
            card.addView(actions);

            final int readingIndex = i;

            editButton.setOnClickListener(
                    v -> editReading(readingIndex)
            );

            deleteButton.setOnClickListener(
                    v -> confirmDeleteReading(readingIndex)
            );

            readingList.addView(card);
        }
    }

    private void styleActionButton(Button button, boolean isEdit) {
        int backgroundColor = isEdit
                ? Color.rgb(224, 242, 241)
                : Color.rgb(253, 236, 236);

        int borderColor = isEdit
                ? TEAL
                : Color.rgb(217, 83, 79);

        int textColor = isEdit
                ? TEAL
                : RED;

        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(dp(10));
        background.setStroke(dp(1), borderColor);

        button.setBackground(background);
        button.setTextColor(textColor);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTypeface(null, Typeface.BOLD);

        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);

        button.setPadding(dp(18), 0, dp(18), 0);
        button.setStateListAnimator(null);
        button.setElevation(0);
    }

    private void editReading(int index) {
        if (index < 0 || index >= readings.length()) {
            return;
        }

        JSONObject reading = readings.optJSONObject(index);

        if (reading == null) {
            return;
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), dp(4));

        EditText glucoseInput = createDialogInput(
                "Blood sugar (mmol/L)",
                reading.optString("glucose")
        );

        EditText timingInput = createDialogInput(
                "Meal timing",
                reading.optString("timing")
        );

        EditText dateInput = createDialogInput(
                "Date (dd/MM/yyyy)",
                reading.optString("date")
        );

        EditText notesInput = createDialogInput(
                "Notes (optional)",
                reading.optString("notes")
        );

        dateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            try {
                String[] parts = dateInput.getText()
                        .toString().split("/");

                if (parts.length == 3) {
                    calendar.set(
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[0])
                    );
                }
            } catch (Exception ignored) {
            }

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) ->
                            dateInput.setText(String.format(
                                    Locale.getDefault(),
                                    "%02d/%02d/%04d",
                                    day,
                                    month + 1,
                                    year
                            )),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            picker.show();
        });

        form.addView(glucoseInput);
        form.addView(timingInput);
        form.addView(dateInput);
        form.addView(notesInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Blood Sugar Reading")
                .setView(form)
                .setPositiveButton("Save Changes", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            saveButton.setTextColor(TEAL);

            saveButton.setOnClickListener(v -> {
                String glucose = glucoseInput.getText()
                        .toString().trim();

                String timing = timingInput.getText()
                        .toString().trim();

                String date = dateInput.getText()
                        .toString().trim();

                String notes = notesInput.getText()
                        .toString().trim();

                if (!isValidGlucose(glucose)) {
                    glucoseInput.setError(
                            "Enter a valid measurement"
                    );
                    glucoseInput.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(timing)) {
                    timingInput.setError(
                            "Enter the meal timing"
                    );
                    timingInput.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(date)) {
                    dateInput.setError("Enter or select a date");
                    dateInput.requestFocus();
                    return;
                }

                try {
                    reading.put("glucose", glucose);
                    reading.put("timing", timing);
                    reading.put("date", date);
                    reading.put("notes", notes);

                    readings.put(index, reading);

                    saveReadings();
                    displayReadings();

                    Toast.makeText(
                            this,
                            "Reading updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();

                } catch (JSONException e) {
                    Toast.makeText(
                            this,
                            "Unable to update reading",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });

        dialog.show();
    }

    private EditText createDialogInput(
            String hint,
            String value
    ) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setTextColor(Color.rgb(24, 59, 70));
        input.setHintTextColor(Color.rgb(113, 131, 140));
        input.setTextSize(14);
        input.setSingleLine(false);
        input.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(8);
        input.setLayoutParams(params);

        return input;
    }

    private void confirmDeleteReading(int index) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reading")
                .setMessage(
                        "Are you sure you want to delete this reading? " +
                                "This action cannot be undone."
                )
                .setPositiveButton("Delete", (dialog, which) -> {
                    readings.remove(index);
                    saveReadings();
                    displayReadings();

                    Toast.makeText(
                            this,
                            "Reading deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getAccentColor(String value) {
        try {
            double glucose = Double.parseDouble(value);

            if (glucose < 4.0) {
                return 0xFFE6A23C;
            } else if (glucose <= 7.0) {
                return 0xFF28A69A;
            } else {
                return 0xFFE6A23C;
            }

        } catch (Exception e) {
            return 0xFF28A69A;
        }
    }

    private String getToday() {
        return new SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
        ).format(new Date());
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density
        );
    }
}
