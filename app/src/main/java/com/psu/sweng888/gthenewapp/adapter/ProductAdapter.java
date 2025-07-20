package com.psu.sweng888.gthenewapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private List<Product> productListFull;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        try {
            holder.name.setText(product.getName() != null ? product.getName() : "(No Name)");
            holder.brand.setText(product.getBrand() != null ? product.getBrand() : "(No Brand)");
            holder.price.setText("$" + String.format("%.2f", product.getPrice()));
            holder.description.setText(product.getDescription() != null ? product.getDescription() : "(No Description)");
        } catch (Exception e) {
            android.util.Log.e("ProductAdapter", "Error binding product at position " + position + ": " + e.getMessage(), e);
        }
    }
    @Override
    public int getItemCount() { return productList.size(); }

    public void filter(String query) {
        productList.clear();
        if (query == null || query.trim().isEmpty()) {
            productList.addAll(productListFull);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Product product : productListFull) {
                if (product.getName().toLowerCase().contains(lowerQuery) ||
                    product.getBrand().toLowerCase().contains(lowerQuery) ||
                    product.getDescription().toLowerCase().contains(lowerQuery) ||
                    String.valueOf(product.getPrice()).contains(lowerQuery)) {
                    productList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, brand, price, description;
        public ProductViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.product_name);
            brand = itemView.findViewById(R.id.product_brand);
            price = itemView.findViewById(R.id.product_price);
            description = itemView.findViewById(R.id.product_description);
        }
    }
} 