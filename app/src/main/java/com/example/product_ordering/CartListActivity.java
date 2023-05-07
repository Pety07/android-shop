package com.example.product_ordering;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CartListActivity extends AppCompatActivity {

    private static final String LOG_TAG = CartListActivity.class.getName();
    private FirebaseUser mUser;
    private String mCurrentUserEmail;

    private RecyclerView mRecyclerView;
    private List<Product> mCartList;
    private CartAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mUserRef;

    private ActionBar actionBar;
    private TextView mEmptyCart;
    private View mContinueButton;
    private int mCartNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }

        mCurrentUserEmail = mUser.getEmail();
        mCartList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.cartRecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new CartAdapter(this, (ArrayList<Product>) mCartList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mUserRef = mFirestore.collection("Users");

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mEmptyCart = findViewById(R.id.emptyCart);
        mContinueButton = findViewById(R.id.continueToOrder);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OrderingActivity.class);
                startActivity(intent);
            }
        });

        queryCart();
    }

    private void queryCart() {
        mCartList.clear();
        mCartNumber = 0;
        mUserRef.whereEqualTo("email", mCurrentUserEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        mCartList.addAll(user.getCartList());
                        mCartNumber = mCartList.size();
                    }
                    handleContinueButton();
                    mAdapter.notifyDataSetChanged();
                });
    }

    private void handleContinueButton() {
        if (mCartNumber > 0) {
            mEmptyCart.setVisibility(GONE);
            mContinueButton.setVisibility(VISIBLE);
        } else {
            mEmptyCart.setVisibility(VISIBLE);
            mContinueButton.setVisibility(GONE);
        }
    }

    private void setAndNotifyList(List<Product> new_list) {
        mCartList.clear();
        mCartList.addAll(new_list);
        mAdapter.notifyDataSetChanged();
    }

    public boolean addAmount(Product product) {
        if (product.getAmount() < product.getInStock()) {
            updateCart(product, 1);
            return true;
        }
        return false;
    }

    public boolean removeAmount(Product product) {
        if (product.getAmount() > 1) {
            updateCart(product, -1);
            return true;
        }
        updateCart(product, 0);
        mCartNumber--;
        handleContinueButton();
        return false;
    }

    public void updateCart(Product product, int operator) {
        mUserRef.whereEqualTo("email", mCurrentUserEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        List<Product> list = user.getCartList();
                        List<Product> new_list = new ArrayList<>();
                        for (Product item : list) {

                            if (operator == -1) {
                                if (product.getId().equals(item.getId())) {
                                    item.setAmount(item.getAmount() - 1);
                                    break;
                                }
                            } else if (operator == 1) {
                                if (product.getId().equals(item.getId())) {
                                    item.setAmount(item.getAmount() + 1);
                                    break;
                                }
                            } else {
                                if (!item.getId().equals(product.getId())) {
                                    new_list.add(item);
                                }
                            }
                        }
                        user.setCartList(operator == 0 ? new_list : list);
                        mUserRef.document(user._getId()).update("cartList", operator == 0 ? new_list : list).addOnFailureListener(failure -> {
                            Toast.makeText(this, "Sikertelen módosítás!", Toast.LENGTH_LONG).show();
                        });
                        setAndNotifyList(operator == 0 ? new_list : list);
                    }
                });
    }

}