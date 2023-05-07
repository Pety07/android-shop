package com.example.product_ordering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> implements Filterable {

    private List<Product> mProductListCurr = new ArrayList<>();
    private List<Product> mProductListWhole = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;
    private String animateID = "";

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Product> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                results.count = mProductListWhole.size();
                results.values = mProductListWhole;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Product product : mProductListWhole) {
                    if (product.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(product);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mProductListCurr = (ArrayList) filterResults.values;
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() { return productFilter; }

    ProductAdapter(Context context, ArrayList<Product> productList) {
        this.mProductListCurr = productList;
        this.mProductListWhole = productList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.product_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
        Product product = mProductListCurr.get(position);
        holder.bindProduct(product);

        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_anim);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }

    }

    @Override
    public int getItemCount() {
        return mProductListCurr.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mNameText;
        private TextView mBrandText;
        private ImageView mProductImage;
        private TextView mDescriptionText;
        private TextView mPriceText;
        private TextView mAddOrRemoveText;
        private ImageView mAddOrRemoveImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mNameText = itemView.findViewById(R.id.productTitle);
            mBrandText = itemView.findViewById(R.id.prductBrand);
            mProductImage = itemView.findViewById(R.id.productImage);
            mDescriptionText = itemView.findViewById(R.id.prductDescription);
            mPriceText = itemView.findViewById(R.id.prductPrice);
            mAddOrRemoveText = itemView.findViewById(R.id.addOrRemove);
            mAddOrRemoveImage = itemView.findViewById(R.id.addOrRemoveImage);
        }

        private void bindProduct(Product product) {
            mNameText.setText(product.getName());
            mBrandText.setText(product.getBrand());
            Glide.with(mContext).load(product.getImageResource()).into(mProductImage);
            mDescriptionText.setText(product.getDescription());
            mPriceText.setText(product.getPrice());

            if (product.isInCart()) {
                if (animateID.equals(product.getId())) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_anim);
                    itemView.startAnimation(animation);
                    animateID = "";
                }
                setPicToRemove();
            } else {
                if (animateID.equals(product.getId())) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_back_anim);
                    itemView.startAnimation(animation);
                    animateID = "";
                }
                setPicToAdd();
            }

            itemView.findViewById(R.id.addToCart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean success = ((ProductListActivity) mContext).updateInCartNumber(product);
                    if (success) {
                        animateID = product.getId();
                    } else {
                        animateID = product.getId();
                    }
                }
            });
        }

        private void setPicToAdd() {
            mAddOrRemoveText.setText("Hozzáadás a kosárhoz");
            mAddOrRemoveImage.setImageResource(R.drawable.add_to_cart_icon);
        }

        private void setPicToRemove() {
            mAddOrRemoveText.setText("Eltávolítás a kosárból");
            mAddOrRemoveImage.setImageResource(R.drawable.remove_cart_icon);
        }
    }
}
