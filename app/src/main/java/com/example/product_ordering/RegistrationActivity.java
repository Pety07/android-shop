package com.example.product_ordering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = RegistrationActivity.class.getName();

    private EditText mFullName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPassAgain;
    private EditText mTelNumber;
    private Spinner spinner;
    private EditText mAddress;
    private RadioGroup mRadioGroupButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore mFirestore;
    private CollectionReference mUserListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.emailEdittext);
        mPassword = findViewById(R.id.passwordEdittext);
        mPassAgain = findViewById(R.id.passAgainEdittext);
        mTelNumber = findViewById(R.id.telNumber);
        spinner = findViewById(R.id.countrySpinner);
        mAddress = findViewById(R.id.addressEdittext);
        mRadioGroupButton = findViewById(R.id.radioButton);
        mRadioGroupButton.check(R.id.man);
        spinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries,
                R.layout.custom_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        firebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mUserListRef = mFirestore.collection("Users");
    }

    public void registration(View view) {
        String email = mEmail.getText().toString().toLowerCase();
        String password = mPassword.getText().toString();
        if (
                email.isEmpty()
                || password.isEmpty()
                || mFullName.getText().toString().isEmpty()
                || mTelNumber.getText().toString().isEmpty()
                || mAddress.getText().toString().isEmpty()
        ) {
            Log.e(LOG_TAG, "Minden mezőt kötelező kitölteni!");
            Toast.makeText(this, "Minden mezőt kötelező kitölteni!", Toast.LENGTH_LONG).show();
            return;
        }
        else if (!mPassword.getText().toString().equals(mPassAgain.getText().toString())) {
            Log.e(LOG_TAG, "A két jelszó nem egyezik!");
            Toast.makeText(this, "A két jelszó nem egyezik!", Toast.LENGTH_LONG).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Sikeres regisztráció");
                            insertUser();
                            jumpToProductList();
                        } else {
                            Log.d(LOG_TAG, "Nem sikerült a regisztráció");
                            Toast.makeText(RegistrationActivity.this,
                                    "Nem sikerült a regisztráció: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void cancel(View view) {
        finish();
    }

    private void insertUser() {
        int radioButtonId = mRadioGroupButton.getCheckedRadioButtonId();
        RadioButton radioButton = mRadioGroupButton.findViewById(radioButtonId);
        String which_sex = (String) radioButton.getText();
        mUserListRef.add(new User(
                mFullName.getText().toString(),
                mEmail.getText().toString().toLowerCase(),
                mPassword.getText().toString(),
                mTelNumber.getText().toString(),
                spinner.getSelectedItem().toString(),
                mAddress.getText().toString(),
                which_sex,
                new ArrayList<>()
        ));
    }

    private void jumpToProductList() {
        Intent intent = new Intent(this, ProductListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String chosen = parent.getItemAtPosition(position).toString();
        Log.i(LOG_TAG, chosen);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}