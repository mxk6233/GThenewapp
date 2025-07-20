package com.psu.sweng888.gthenewapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.adapter.ProductAdapter;
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.data.Product;
import java.util.List;
import androidx.appcompat.widget.SearchView;
import android.app.AlertDialog;
import com.psu.sweng888.gthenewapp.util.RecyclerItemClickListener;
import com.psu.sweng888.gthenewapp.fragments.EditProductFragment;

public class ProductsListFragment extends Fragment {
    private static final String TAG = "ProductsListFragment";
    private ProductAdapter productAdapter;
    private RecyclerView mRecyclerView;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dbHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        // Set up SearchView
        SearchView searchView = view.findViewById(R.id.product_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (productAdapter != null) productAdapter.filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (productAdapter != null) productAdapter.filter(newText);
                return true;
            }
        });
        loadProducts();
        return view;
    }
    private void loadProducts() {
        firebaseDatabaseManager.getAllProducts(task -> {
            List<Product> products = task.getResult();
            if (products == null || products.isEmpty()) {
                if (dbHelper.isProductsEmpty()) dbHelper.populateProductsDatabase();
                products = dbHelper.getAllProducts();
                Toast.makeText(getActivity(), "Loaded from local database", Toast.LENGTH_SHORT).show();
            }
            final List<Product> finalProducts = products;
            getActivity().runOnUiThread(() -> {
                productAdapter = new ProductAdapter(finalProducts);
                mRecyclerView.setAdapter(productAdapter);
                // Add long-press for edit/delete
                mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {}
                    @Override
                    public void onLongItemClick(View view, int position) {
                        showEditDeleteDialog(finalProducts.get(position));
                    }
                }));
            });
        });
    }
    private void showEditDeleteDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(product.getName())
            .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                if (which == 0) {
                    // Edit
                    EditProductFragment editFragment = EditProductFragment.newInstance(product);
                    getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
                } else if (which == 1) {
                    dbHelper.clearAllProducts(); // For demo, replace with deleteProduct(product) for real app
                    firebaseDatabaseManager.addProduct(product, task -> loadProducts());
                    Toast.makeText(getActivity(), "Product deleted", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dbHelper = new BookDatabaseHelper(context);
    }
} 