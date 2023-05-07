package com.example.product_ordering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int RC_SIGN_IN = 123;

    private EditText mEmail;
    private EditText mPassword;
    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mFirebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG, "Bejelentkezve mint: " + account.getId());
                firabaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(LOG_TAG, "Google bejelentkezés nem sikerült!");
            }
        }
    }

    private void firabaseAuthWithGoogle(String id) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(id, null);
        mFirebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres belépés credential-lel!");
                    jumpToProductList();
                } else {
                    Log.d(LOG_TAG, "Nem sikerült a belépés credential-lel!");
                    Toast.makeText(MainActivity.this, "Nem sikerült a belépés: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void login(View view) {
        String email = mEmail.getText().toString().toLowerCase();
        String password = mPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Log.e(LOG_TAG, "Kérem adja meg a felhasználó nevet és a jelszót is!");
            Toast.makeText(this, "Kérem adja meg a felhasználó nevet és a jelszót is!", Toast.LENGTH_LONG).show();
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres belépés " + email + "val!");
                    jumpToProductList();
                } else {
                    Log.d(LOG_TAG, "Nem sikerült a belépés");
                    Toast.makeText(MainActivity.this, "Nem sikerült a belépés: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void loginAsGuest(View view) {
        mFirebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres Anonymous belépés!");
                    jumpToProductList();
                } else {
                    Log.d(LOG_TAG, "Nem sikerült a belépés");
                    Toast.makeText(MainActivity.this, "Nem sikerült a belépés: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void loginWithGoogle(View view) {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    public void registration(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void jumpToProductList() {
        Intent intent = new Intent(this, ProductListActivity.class);
        startActivity(intent);
    }
}