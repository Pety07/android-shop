package com.example.product_ordering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<Product> mCartList = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;
    private boolean animateLeft = false;
    private String animateID = "";

    CartAdapter(Context context, ArrayList<Product> cartList) {
        this.mCartList = cartList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.cart_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        Product product = mCartList.get(position);
        holder.bindToItem(product);

        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_anim);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mCartList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mCartImage;
        private TextView mPriceText;
        private TextView mNameText;
        private TextView mAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mCartImage = itemView.findViewById(R.id.cartImage);
            mPriceText = itemView.findViewById(R.id.cartPrice);
            mNameText = itemView.findViewById(R.id.cartTitle);
            mAmount = itemView.findViewById(R.id.amount);
        }

        private void bindToItem(Product product) {
            Glide.with(mContext).load(product.getImageResource()).into(mCartImage);
            mPriceText.setText(setPrice(product.getAmount(), product.getPrice()));
            mNameText.setText(product.getName());
            mAmount.setText(String.valueOf(product.getAmount()));

            if (animateID.equals(product.getId())) {
                if (animateLeft) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_from_in_to_back_anim);
                    itemView.startAnimation(animation);
                } else {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_short_in_anim);
                    itemView.startAnimation(animation);
                }
                animateID = "";
            }

            itemView.findViewById(R.id.addAmount).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean wasSuccessful = ((CartListActivity) mContext).addAmount(product);
                    if (wasSuccessful) {
                        int amount = Integer.parseInt(mAmount.getText().toString()) + 1;
                        String newPrice = setPrice(amount, product.getPrice());
                        mPriceText.setText(newPrice);
                        mAmount.setText(String.valueOf(amount));
                        animateID = product.getId();
                        animateLeft = false;
                    }
                }
            });

            itemView.findViewById(R.id.removeAmount).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean wasSuccessful = ((CartListActivity) mContext).removeAmount(product);
                    if (wasSuccessful) {
                        int amount = Integer.parseInt(mAmount.getText().toString()) - 1;
                        mAmount.setText(String.valueOf(amount));
                        String newPrice = setPrice(amount, product.getPrice());
                        mPriceText.setText(newPrice);
                        mAmount.setText(String.valueOf(amount));
                        animateID = product.getId();
                        animateLeft = true;
                    } else {
                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_from_in_to_back_anim);
                        itemView.startAnimation(animation);
                    }
                }
            });
        }

        private String setPrice(int amount, String oldPrice) {
            String[] splittedPrice = oldPrice.split(" ");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < splittedPrice.length - 1; i++) {
                stringBuilder.append(splittedPrice[i]);
            }
            String price = String.valueOf(Integer.parseInt(stringBuilder.toString()) * amount);
            StringBuilder newPriceReversed = new StringBuilder(price);
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

    }

}
