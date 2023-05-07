package com.example.product_ordering;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = OrderingActivity.class.getName();
    private FirebaseUser mUser;
    private String mCurrentUserEmail;
    private String mCurrentUserID;

    private EditText mFullName;
    private EditText mEmail;
    private EditText mTelNumber;
    private EditText mAddress;
    private EditText mDescription;
    private Spinner spinner;
    private String deliveryFee;
    private boolean mIsAirPlaneOn;

    private TextView mShippingFee;
    private TextView mTotalProductPrice;
    private TextView mTotalPrice;

    private FirebaseFirestore mFirestore;
    private CollectionReference mProductListRef;
    private CollectionReference mUserRef;
    private CollectionReference mOrderRef;
    private List<Product> mCartList;

    private MyNotificationManager mNotificationManager;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }

        mCurrentUserEmail = mUser.getEmail();
        mFullName = findViewById(R.id.detailsFullName);
        mEmail = findViewById(R.id.detailsEmail);
        mTelNumber = findViewById(R.id.detailsTelNum);
        mAddress = findViewById(R.id.detailsAddress);
        mDescription = findViewById(R.id.detailsDescription);
        spinner = findViewById(R.id.paymentSpinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods,
                R.layout.custom_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mIsAirPlaneOn = false;

        mShippingFee = findViewById(R.id.shippingFeeValue);
        mTotalProductPrice = findViewById(R.id.totalProductPriceValue);
        mTotalPrice = findViewById(R.id.totalPriceValue);

        findViewById(R.id.verifyOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });

        mFirestore = FirebaseFirestore.getInstance();
        mProductListRef = mFirestore.collection("Products");
        mUserRef = mFirestore.collection("Users");
        mOrderRef = mFirestore.collection("Orders");
        mCartList = new ArrayList<>();

        mNotificationManager = new MyNotificationManager(this);
        actionBar = getSupportActionBar();  // A VISSZA GOMB MIATT
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(airPlaneModeChecker, intentFilter);

        setPrices();
    }

    BroadcastReceiver airPlaneModeChecker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mIsAirPlaneOn = !mIsAirPlaneOn;
            }
        }
    };

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void placeOrder() {
        if (
                mFullName.getText().toString().isEmpty()
                        || mEmail.getText().toString().isEmpty()
                        || mTelNumber.getText().toString().isEmpty()
                        || mAddress.getText().toString().isEmpty()
        ) {
            Toast.makeText(this, "Kérem töltsön ki minden mezőt!", Toast.LENGTH_LONG).show();
            return;
        } else if (!isNetworkConnected() || mIsAirPlaneOn) {
            Toast.makeText(this, "A rendeléshez szükséges internet kapcsolat!", Toast.LENGTH_LONG).show();
            return;
        }

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        mOrderRef.add(new Order(
                mFullName.getText().toString(),
                mEmail.getText().toString(),
                mTelNumber.getText().toString(),
                mAddress.getText().toString(),
                mDescription.getText().toString(),
                formatter.format(date),
                mCartList,
                mTotalPrice.getText().toString(),
                deliveryFee
        ));

        mProductListRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());

                        for (Product item : mCartList) {
                            if (product.getId().equals(item.getId())) {
                                int in_stock = product.getInStock() - item.getAmount();
                                mProductListRef.document(product.getId()).update("inStock", in_stock);
                            }
                        }

                        mUserRef.document(mCurrentUserID).update("cartList", new ArrayList<Product>());
                    }
                    mNotificationManager.send("Kedves " + mFullName.getText().toString() + "!\nKöszönjük a rendelésed!");
                    Intent intent = new Intent(this, ProductListActivity.class);
                    startActivity(intent);
                });
    }

    private void setPrices() {
        mCartList.clear();
        mUserRef.whereEqualTo("email", mCurrentUserEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        mCurrentUserID = document.getId();
                        mCartList.addAll(user.getCartList());
                    }
                    int totalPriceValue = 0;
                    for (Product item : mCartList) {
                        int amount = item.getAmount();
                        int price = getValueOfPrice(item.getPrice());
                        totalPriceValue += price * amount;
                    }
                    String totalProductPrice = makeStringFromPrice(String.valueOf(totalPriceValue));
                    mTotalProductPrice.setText(totalProductPrice);
                    countPricePlusShipping();
                });
    }

    private int getValueOfPrice(String price) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] splittedPrice = price.split(" ");
        for (int i = 0; i < splittedPrice.length - 1; i++) {
            stringBuilder.append(splittedPrice[i]);
        }
        return Integer.parseInt(stringBuilder.toString());
    }

    private String makeStringFromPrice(String price) {
        String priceString = String.valueOf(price);
        StringBuilder newPriceReversed = new StringBuilder(priceString);
        newPriceReversed = newPriceReversed.reverse();
        int j = 0;
        StringBuilder newPrice = new StringBuilder();
        for (Character num : newPriceReversed.toString().toCharArray()) {
            if (j % 3 == 0) {
                newPrice.append(" ");
            }
            newPrice.append(num);
            j++;
        }
        newPrice = newPrice.reverse();
        newPrice.append(" Ft");
        return newPrice.toString();
    }

    private void countPricePlusShipping() {
        int totalProductPrice = getValueOfPrice(mTotalProductPrice.getText().toString());
        totalProductPrice += getValueOfPrice(mShippingFee.getText().toString());
        String totalPrice = makeStringFromPrice(String.valueOf(totalProductPrice));
        mTotalPrice.setText(totalPrice);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String chosen = parent.getItemAtPosition(position).toString();
        if (chosen.equals("Utánvétel ( + 500 Ft)")) {
            mShippingFee.setText("500 Ft");
            deliveryFee = "500 Ft";
        } else if (chosen.equals("Banki utalás (Ingyenes)")) {
            mShippingFee.setText("0 Ft");
            deliveryFee = "0 Ft";
        } else {
            mShippingFee.setText("1000 Ft");
            deliveryFee = "1000 Ft";
        }
        countPricePlusShipping();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void fillDetails(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            mUserRef.whereEqualTo("email", mUser.getEmail()).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            User user = document.toObject(User.class);
                            user.setId(document.getId());
                            mFullName.setText(user.getFullName());
                            mEmail.setText(user.getEmail());
                            mTelNumber.setText(user.getPhoneNumber());
                            StringBuilder stringBuilder = new StringBuilder(user.getCountry());
                            stringBuilder.append(", ").append(user.getAddress());
                            mAddress.setText(stringBuilder.toString());
                        }
                    });
        } else {
            mFullName.setText("");
            mEmail.setText("");
            mTelNumber.setText("");
            mAddress.setText("");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(airPlaneModeChecker);
    }
}