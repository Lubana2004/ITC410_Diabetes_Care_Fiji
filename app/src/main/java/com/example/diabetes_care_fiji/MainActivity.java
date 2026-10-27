package com.example.diabetes_care_fiji;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge;
    private Button button;

    private static final String PREFS_NAME = "DiabetesCarePrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_AGE = "user_age";
    private static final String KEY_PROFILE_COMPLETED = "profile_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check whether the user has already entered their details.
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean profileCompleted =
                preferences.getBoolean(KEY_PROFILE_COMPLETED, false);

        if (profileCompleted) {
            // Skip the Name and Age page.
            openHome();
            return;
        }

        // Show the Name and Age page for first-time users.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

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
                }
        );

        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> saveUserDetails());
    }

    private void saveUserDetails() {

        String name = editTextName.getText()
                .toString()
                .trim();

        String ageText = editTextAge.getText()
                .toString()
                .trim();

        // Validate the name.
        if (name.isEmpty()) {
            editTextName.setError("Please enter your name");
            editTextName.requestFocus();
            return;
        }

        // Validate the age.
        if (ageText.isEmpty()) {
            editTextAge.setError("Please enter your age");
            editTextAge.requestFocus();
            return;
        }

        int age;

        try {
            age = Integer.parseInt(ageText);

            if (age < 1 || age > 120) {
                editTextAge.setError("Please enter a valid age");
                editTextAge.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {
            editTextAge.setError("Please enter a valid age");
            editTextAge.requestFocus();
            return;
        }

        // Save the profile directly on the phone.
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        preferences.edit()
                .putString(KEY_NAME, name)
                .putInt(KEY_AGE, age)
                .putBoolean(KEY_PROFILE_COMPLETED, true)
                .apply();

        Toast.makeText(
                MainActivity.this,
                "Profile saved successfully!",
                Toast.LENGTH_SHORT
        ).show();

        // Open Home after saving the profile.
        openHome();
    }

    private void openHome() {

        Intent intent = new Intent(
                MainActivity.this,
                Home.class
        );

        startActivity(intent);

        // Prevent returning to the Name and Age page using Back.
        finish();
    }
}