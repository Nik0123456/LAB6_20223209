package com.example.l6_20223209.services;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class FirebaseAuthService {
    
    private static FirebaseAuthService instance;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private FirebaseAuthService() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthService getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthService();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void initGoogleSignIn(Context context, String webClientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public Task<AuthResult> signInWithEmail(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> registerWithEmail(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        return mAuth.signInWithCredential(credential);
    }

    public void signOut(Context context) {
        mAuth.signOut();
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }
}
