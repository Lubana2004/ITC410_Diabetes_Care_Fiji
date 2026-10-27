
package com.example.diabetes_care_fiji;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.widget.TextView;

import java.util.Locale;

public class bmiCalculator extends AppCompatActivity {

    private TextInputEditText etWeight, etHeight;

    private MaterialButton btnCalculateBMI, btnResetBMI;

    private CardView cardBMIResult;

    private TextView tvBMIValue, tvBMICategory, tvBMIAdvice;

    private ImageButton btnBack;

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
        setContentView(R.layout.activity_bmicalculator);

        // Connect XML views
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);

        btnCalculateBMI = findViewById(R.id.btnCalculateBMI);
        btnResetBMI = findViewById(R.id.btnResetBMI);

        cardBMIResult = findViewById(R.id.cardBMIResult);

        tvBMIValue = findViewById(R.id.tvBMIValue);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        tvBMIAdvice = findViewById(R.id.tvBMIAdvice);

        btnBack = findViewById(R.id.btnBack);

        // Calculate BMI
        btnCalculateBMI.setOnClickListener(v -> calculateBMI());

        // Clear inputs and results
        btnResetBMI.setOnClickListener(v -> clearFields());

        // Return to previous screen
        btnBack.setOnClickListener(v -> finish());
    }

    private void calculateBMI() {

        String weightInput = etWeight.getText() != null
                ? etWeight.getText().toString().trim() : "";

        String heightInput = etHeight.getText() != null
                ? etHeight.getText().toString().trim() : "";

        // Check for empty fields
        if (weightInput.isEmpty()) {
            etWeight.setError("Please enter your weight");
            etWeight.requestFocus();
            return;
        }

        if (heightInput.isEmpty()) {
            etHeight.setError("Please enter your height");
            etHeight.requestFocus();
            return;
        }

        double weight;
        double heightCm;

        try {
            weight = Double.parseDouble(weightInput);
            heightCm = Double.parseDouble(heightInput);
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Please enter valid numbers",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Validate values
        if (weight <= 0 || weight > 500) {
            etWeight.setError("Enter a valid weight in kg");
            etWeight.requestFocus();
            return;
        }

        if (heightCm <= 0 || heightCm > 300) {
            etHeight.setError("Enter a valid height in cm");
            etHeight.requestFocus();
            return;
        }

        // Convert centimetres to metres
        double heightMetres = heightCm / 100.0;

        // BMI = weight in kilograms / height in metres squared
        double bmi = weight / (heightMetres * heightMetres);

        // Display BMI value
        tvBMIValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

        // Determine category and advice
        String category;
        String advice;
        int categoryColor;

        if (bmi < 18.5) {

            category = "Underweight";
            categoryColor = 0xFF1976D2;

            advice = "Your BMI is below the usual adult healthy-weight range. " +
                    "Consider discussing your nutrition and weight with a healthcare professional.";

        } else if (bmi < 25.0) {

            category = "Normal weight";
            categoryColor = 0xFF2E7D32;

            advice = "Your BMI is within the usual adult healthy-weight range. " +
                    "Continue balanced meals, regular physical activity, and recommended diabetes check-ups.";

        } else if (bmi < 30.0) {

            category = "Overweight";
            categoryColor = 0xFFED6C02;

            advice = "Your BMI is above the usual adult healthy-weight range. " +
                    "Balanced meals and regular physical activity may help. " +
                    "Ask a healthcare professional about goals suitable for you.";

        } else {

            category = "Obesity range";
            categoryColor = 0xFFC62828;

            advice = "Your BMI is in the adult obesity range. " +
                    "BMI alone cannot diagnose a health condition. " +
                    "A healthcare professional can help assess your overall health and discuss suitable next steps.";
        }

        // Update result views
        tvBMICategory.setText(category);
        tvBMICategory.setTextColor(categoryColor);
        tvBMIAdvice.setText(advice);

        // Show result card
        cardBMIResult.setVisibility(View.VISIBLE);

        // Move the result into view
        cardBMIResult.requestFocus();

        Toast.makeText(
                this,
                "BMI calculated successfully",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void clearFields() {

        etWeight.setText("");
        etHeight.setText("");

        etWeight.setError(null);
        etHeight.setError(null);

        cardBMIResult.setVisibility(View.GONE);

        etWeight.requestFocus();
    }
}
