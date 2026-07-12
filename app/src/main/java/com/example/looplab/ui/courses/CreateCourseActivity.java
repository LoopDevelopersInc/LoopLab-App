package com.example.looplab.ui.courses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.looplab.R;
import com.example.looplab.data.CourseService;
import com.example.looplab.data.model.Models;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CreateCourseActivity extends AppCompatActivity {

    private TextInputEditText etCourseTitle, etCourseDescription, etLectureCount;
    private AutoCompleteTextView spinnerCategory, spinnerDifficulty;
    private MaterialButton btnCreateCourse, btnCancel;
    private CourseService courseService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_course);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        initializeViews();
        setupDropdowns();
        setupClickListeners();
        initializeServices();
    }

    private void initializeViews() {
        etCourseTitle = findViewById(R.id.etCourseTitle);
        etCourseDescription = findViewById(R.id.etCourseDescription);
        etLectureCount = findViewById(R.id.etLectureCount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnCreateCourse = findViewById(R.id.btnCreateCourse);
        btnCancel = findViewById(R.id.btnCancel);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // Category dropdown
        List<String> categories = new ArrayList<>();
        categories.add("Programming");
        categories.add("Design");
        categories.add("Business");
        categories.add("Marketing");
        categories.add("Data Science");
        categories.add("Mobile Development");
        categories.add("Web Development");
        categories.add("AI & Machine Learning");
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // Difficulty dropdown
        List<String> difficulties = new ArrayList<>();
        difficulties.add("Beginner");
        difficulties.add("Intermediate");
        difficulties.add("Advanced");
        
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, difficulties);
        spinnerDifficulty.setAdapter(difficultyAdapter);
    }

    private void setupClickListeners() {
        btnCreateCourse.setOnClickListener(v -> {
            if (validateInputs()) {
                createCourse();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void initializeServices() {
        courseService = new CourseService();
    }

    private boolean validateInputs() {
        String title = etCourseTitle.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();
        String lectureCountStr = etLectureCount.getText().toString().trim();

        if (title.isEmpty()) {
            etCourseTitle.setError("Course title is required");
            return false;
        }

        if (description.isEmpty()) {
            etCourseDescription.setError("Course description is required");
            return false;
        }

        if (lectureCountStr.isEmpty()) {
            etLectureCount.setError("Number of lectures is required");
            return false;
        }

        String category = spinnerCategory.getText().toString().trim();
        if (category.isEmpty()) {
            spinnerCategory.setError("Category is required");
            return false;
        }

        String difficulty = spinnerDifficulty.getText().toString().trim();
        if (difficulty.isEmpty()) {
            spinnerDifficulty.setError("Difficulty level is required");
            return false;
        }

        try {
            int lectureCount = Integer.parseInt(lectureCountStr);
            if (lectureCount <= 0) {
                etLectureCount.setError("Number of lectures must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etLectureCount.setError("Please enter a valid number");
            return false;
        }

        return true;
    }

    private void createCourse() {
        String title = etCourseTitle.getText().toString().trim();
        String description = etCourseDescription.getText().toString().trim();
        int lectureCount = Integer.parseInt(etLectureCount.getText().toString().trim());
        String category = spinnerCategory.getText().toString();
        String difficulty = spinnerDifficulty.getText().toString();

        // Create course object
        Models.Course course = new Models.Course();
        course.id = java.util.UUID.randomUUID().toString();
        course.title = title;
        course.description = description;
        course.instructorId = currentUserId;
        course.instructorName = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Instructor";
        course.lectureCount = lectureCount;
        course.enrolledCount = 0;
        course.category = category;
        course.difficulty = difficulty;
        course.isPublished = false;
        course.rating = 0.0;
        course.createdAt = System.currentTimeMillis();

        // Show loading state
        btnCreateCourse.setEnabled(false);
        btnCreateCourse.setText("Creating Course...");

        // Save course to Firebase
        courseService.createCourse(course, new CourseService.CourseCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateCourseActivity.this, "Course created successfully!", Toast.LENGTH_SHORT).show();
                
                // Show dialog to add lectures
                showAddLecturesDialog(course.id);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CreateCourseActivity.this, "Error creating course: " + error, Toast.LENGTH_SHORT).show();
                btnCreateCourse.setEnabled(true);
                btnCreateCourse.setText("Create Course");
            }
        });
    }
    
    private void showAddLecturesDialog(String courseId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Course Created Successfully!")
                .setMessage("Would you like to add video lectures to your course now?")
                .setPositiveButton("Add Lectures", (dialog, which) -> {
                    // Navigate to lecture creation activity
                    Intent intent = new Intent(this, CreateLectureActivity.class);
                    intent.putExtra("course_id", courseId);
                    startActivity(intent);
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Later", (dialog, which) -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
