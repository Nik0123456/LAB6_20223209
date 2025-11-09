package com.example.l6_20223209.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.l6_20223209.R;
import com.example.l6_20223209.services.FirebaseAuthService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister, btnGoogleSignIn;
    private ProgressBar progressBar;
    private TextView tvSwitchToRegister;
    private boolean isLoginMode = true;

    private FirebaseAuthService authService;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = FirebaseAuthService.getInstance();
        
        // Verificar si ya hay usuario autenticado
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
            return;
        }

        initializeViews();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);
        tvSwitchToRegister = findViewById(R.id.tvSwitchToRegister);
    }

    private void setupGoogleSignIn() {
        // Inicializar Google Sign-In con el Web Client ID
        String webClientId = getString(R.string.default_web_client_id);
        authService.initGoogleSignIn(this, webClientId);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    }
                });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if (isLoginMode) {
                loginUser();
            } else {
                registerUser();
            }
        });

        btnRegister.setOnClickListener(v -> toggleMode());
        
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        tvSwitchToRegister.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            btnLogin.setText("Iniciar Sesión");
            btnRegister.setVisibility(View.VISIBLE);
            tvSwitchToRegister.setText("¿No tienes cuenta? Regístrate");
        } else {
            btnLogin.setText("Registrarse");
            btnRegister.setVisibility(View.GONE);
            tvSwitchToRegister.setText("¿Ya tienes cuenta? Inicia sesión");
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        showProgress(true);

        authService.signInWithEmail(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        showProgress(true);

        authService.registerWithEmail(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = authService.getGoogleSignInClient().getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(this, "Error en Google Sign-In: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showProgress(true);

        authService.firebaseAuthWithGoogle(account)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingrese su correo electrónico");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese su contraseña");
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnRegister.setEnabled(!show);
        btnGoogleSignIn.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
