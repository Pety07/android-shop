package com.example.product_ordering;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProductListActivity.class.getName();
    private FirebaseUser mUser;
    private String mCurrentUserEmail;

    private RecyclerView mRecyclerView;
    private List<Product> mProductList;
    private ProductAdapter mAdapter;
    private List<Product> mCartList;

    private FirebaseFirestore mFirestore;
    private CollectionReference mUserRef;
    private CollectionReference mProductRef;

    private MyNotificationManager mNotificationManager;
    private ActionBar actionBar;
    private TextView mCartNumTextView;
    private int mCartListNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }

        mCurrentUserEmail = mUser.getEmail();
        mRecyclerView = findViewById(R.id.productRecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mProductList = new ArrayList<>();
        mCartList = new ArrayList<>();
        mAdapter = new ProductAdapter(this, (ArrayList<Product>) mProductList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mUserRef = mFirestore.collection("Users");
        mProductRef = mFirestore.collection("Products");

        mNotificationManager = new MyNotificationManager(this);
        actionBar = getSupportActionBar();  // A VISSZA GOMB MIATT
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getCart();
    }

    private void getCart() {
        mCartList.clear();
        mCartListNum = 0;
        mUserRef.whereEqualTo("email", mCurrentUserEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        mCartList.addAll(user.getCartList());
                        mCartListNum = mCartList.size();
                        queryProducts();
                    }
                    mCartNumTextView.setText(String.valueOf(mCartListNum));
                });
    }

    private void queryProducts() {
        mProductList.clear();
        mProductRef.orderBy("name").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        for (Product item : mCartList) {
                            if (product.getId().equals(item.getId())) {
                                product.setInCart(true);
                                break;
                            }
                        }
                        mProductList.add(product);
                    }
                    mAdapter.notifyDataSetChanged();
                });
    }

//    private void uploadProducts() {
//        String[] productNameList = getResources().getStringArray(R.array.product_name);
//        String[] productBrandList = getResources().getStringArray(R.array.product_brand);
//        String[] productPriceList = getResources().getStringArray(R.array.product_price);
//        String[] productDescriptionList = getResources().getStringArray(R.array.product_description);
//        TypedArray productImageResource = getResources().obtainTypedArray(R.array.product_images);
//
//        for (int i = 0; i < productNameList.length; i++) {
//            mProductRef.add(
//                    new Product(
//                            "",
//                            productNameList[i],
//                            productBrandList[i],
//                            productPriceList[i],
//                            productDescriptionList[i],
//                            productImageResource.getResourceId(i, 0),
//                            false,
//                            0,
//                            50)
//            );
//        }
//        mProductRef.get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        Product product = document.toObject(Product.class);
//                        product.setId(document.getId());
//
//                        mProductRef.document(product.getId()).update("id", product.getId());
//                    }
//                });
//
//        productImageResource.recycle();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                Log.d(LOG_TAG, searchText);
                mAdapter.getFilter().filter(searchText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem cartButton = menu.findItem(R.id.cart);
        FrameLayout frameLayout = (FrameLayout) cartButton.getActionView();
        mCartNumTextView = frameLayout.findViewById(R.id.in_cart_number);

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(cartButton);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(LOG_TAG, "Kijelentkezés");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.cart:
                Log.d(LOG_TAG, "Cart");
                Intent intent = new Intent(this, CartListActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addToCart(Product product) {
        mUserRef.whereEqualTo("email", mCurrentUserEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        product.setAmount(1);
                        product.setInCart(true);
                        List<Product> list = user.getCartList();
                        list.add(product);
                        mUserRef.document(user._getId()).update("cartList", list);
                        setAndNotifyList(list, product);
                    }
                });

        mNotificationManager.send(product.getName() + "-t sikeresen beleraktad a kosaradba!");
        mCartListNum++;
        mCartNumTextView.setText(String.valueOf(mCartListNum));
    }

    private void removeFromCart(Product product) {
        mUserRef.whereEqualTo("email", mCurrentUserEmail).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        List<Product> list = user.getCartList();
                        List<Product> new_list = new ArrayList<>();
                        product.setInCart(false);
                        for (Product item : list) {
                            if (!item.getId().equals(product.getId())) {
                                new_list.add(item);
                            }
                        }
                        mUserRef.document(user._getId()).update("cartList", new_list);
                        setAndNotifyList(new_list, product);
                    }
                });
        mCartListNum--;
        mCartNumTextView.setText(String.valueOf(mCartListNum));
    }

    private void setAndNotifyList(List<Product> new_list, Product product) {
        mCartList.clear();
        mCartList.addAll(new_list);
        for (Product item : mProductList) {
            if (item.getId().equals(product.getId())) {
                item.setInCart(product.isInCart());
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public boolean updateInCartNumber(Product product) {
        boolean in_cart = false;
        for (Product item : mCartList) {
            if (item.getId().equals(product.getId())) {
                in_cart = true;
                break;
            }
        }
        if (!in_cart) {
            addToCart(product);
            return true;
        }
        removeFromCart(product);
        return false;
    }
}