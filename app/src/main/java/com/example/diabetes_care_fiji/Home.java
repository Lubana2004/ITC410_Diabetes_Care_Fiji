package com.example.diabetes_care_fiji;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class Home extends AppCompatActivity {

    private TextView textView3;

    private static final String PREFS_NAME = "DiabetesCarePrefs";
    private static final String KEY_NAME = "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

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

        // Retrieve the saved name from phone storage.
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String name = preferences.getString(KEY_NAME, "");

        // Display the personalized welcome message.
        textView3 = findViewById(R.id.textView3);

        if (name != null && !name.trim().isEmpty()) {
            textView3.setText("WELCOME BACK, " + name.trim() + "!");
        } else {
            textView3.setText("WELCOME BACK");
        }

        // Find dashboard cards.
        CardView cardMedicine = findViewById(R.id.cardMedicine);
        CardView cardFoodDiet = findViewById(R.id.cardFoodDiet);
        CardView cardExercise = findViewById(R.id.cardExercise);
        CardView cardTracker = findViewById(R.id.cardTracker);
        CardView cardEducation = findViewById(R.id.cardEducation);
        CardView cardBMI = findViewById(R.id.cardBMI);



        // Set dashboard navigation.
        cardMedicine.setOnClickListener(v ->
                openPage(Medicine.class));

        cardFoodDiet.setOnClickListener(v ->
                openPage(FOOD_DIET.class));

        cardExercise.setOnClickListener(v ->
                openPage(Exercise.class));

        cardTracker.setOnClickListener(v ->
                openPage(BloodSugarTracker.class));

        cardEducation.setOnClickListener(v ->
                openPage(Education.class));

        cardBMI.setOnClickListener(v ->
                openPage(bmiCalculator.class));
    }

    private void openPage(Class<?> page) {
        Intent intent = new Intent(Home.this, page);
        startActivity(intent);
    }
}