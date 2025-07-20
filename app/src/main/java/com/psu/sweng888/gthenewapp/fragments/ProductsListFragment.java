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
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import java.util.ArrayList;

public class ProductsListFragment extends Fragment {
    private static final String TAG = "ProductsListFragment";
    private ProductAdapter productAdapter;
    private RecyclerView mRecyclerView;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;
    private ArrayAdapter<String> autoAdapter; // Store as a field

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        try {
            mRecyclerView = view.findViewById(R.id.recyclerView);
            if (mRecyclerView == null) {
                Log.e(TAG, "RecyclerView is null!");
                Toast.makeText(getActivity(), "RecyclerView is null!", Toast.LENGTH_LONG).show();
                return view;
            }
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            dbHelper = new BookDatabaseHelper(getActivity());
            firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
            if (dbHelper.isProductsEmpty()) {
                dbHelper.clearAllProducts();
                dbHelper.populateProductsDatabase();
                Log.d(TAG, "Products database repopulated");
                Toast.makeText(getActivity(), "Products database repopulated", Toast.LENGTH_SHORT).show();
            }
            AutoCompleteTextView searchAuto = view.findViewById(R.id.product_search_autocomplete);
            autoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
            searchAuto.setAdapter(autoAdapter);
            searchAuto.setThreshold(1);
            searchAuto.setOnItemClickListener((parent, v, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                if (productAdapter != null) productAdapter.filter(selected);
            });
            searchAuto.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (productAdapter != null) productAdapter.filter(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
            Button refreshButton = view.findViewById(R.id.refresh_button);
            if (refreshButton != null) {
                refreshButton.setOnClickListener(v -> refreshProducts());
                refreshButton.setOnLongClickListener(v -> {
                    resetDatabase();
                    return true;
                });
            }
            Button loadDummyButton = view.findViewById(R.id.load_data_button);
            if (loadDummyButton != null) {
                loadDummyButton.setOnClickListener(v -> testRecyclerView());
            }
            Button syncFirebaseButton = view.findViewById(R.id.sync_firebase_button);
            if (syncFirebaseButton != null) {
                syncFirebaseButton.setOnClickListener(v -> {
                    if (firebaseDatabaseManager.isUserAuthenticated()) {
                        firebaseDatabaseManager.getAllProducts(task -> {
                            List<Product> products = task.getResult();
                            if (products != null && !products.isEmpty()) {
                                getActivity().runOnUiThread(() -> {
                                    productAdapter = new ProductAdapter(products);
                                    mRecyclerView.setAdapter(productAdapter);
                                    productAdapter.notifyDataSetChanged();
                                    Toast.makeText(getActivity(), "Synced " + products.size() + " products from Firebase", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "No products found in Firebase", Toast.LENGTH_SHORT).show());
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Not authenticated with Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Button pushFirebaseButton = view.findViewById(R.id.push_firebase_button);
            if (pushFirebaseButton != null) {
                pushFirebaseButton.setOnClickListener(v -> {
                    if (firebaseDatabaseManager.isUserAuthenticated()) {
                        List<Product> products = dbHelper.getAllProducts();
                        if (products != null && !products.isEmpty()) {
                            int[] completed = {0};
                            for (Product product : products) {
                                firebaseDatabaseManager.addProduct(product, task -> {
                                    completed[0]++;
                                    if (completed[0] == products.size()) {
                                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Pushed " + products.size() + " products to Firebase", Toast.LENGTH_SHORT).show());
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getActivity(), "No products in SQLite to push", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Not authenticated with Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            loadProducts(autoAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Exception in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }
    private void loadProducts(ArrayAdapter<String> autoAdapter) {
        Log.d(TAG, "Loading products from SQLite only (ignore Firebase)");
        List<Product> products = dbHelper.getAllProducts();
        final List<Product> finalProducts = products;
        getActivity().runOnUiThread(() -> {
            try {
                productAdapter = new ProductAdapter(finalProducts);
                mRecyclerView.setAdapter(productAdapter);
                autoAdapter.clear();
                for (Product p : finalProducts) autoAdapter.add(p.getName() + " — " + p.getBrand() + " — $" + String.format("%.2f", p.getPrice()) + " — " + p.getDescription());
                autoAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + finalProducts.size() + " products");
                Toast.makeText(getActivity(), "Loaded " + finalProducts.size() + " products", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Exception updating UI with products: " + e.getMessage(), e);
                Toast.makeText(getActivity(), "Error updating product list", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void refreshProducts() {
        Log.d(TAG, "Refreshing products list (SQLite only)...");
        loadProducts(autoAdapter);
    }
    private void resetDatabase() {
        dbHelper.clearAllProducts();
        dbHelper.populateProductsDatabase();
        Toast.makeText(getActivity(), "Product database reset.", Toast.LENGTH_SHORT).show();
        loadProducts(autoAdapter);
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
                    // Delete
                    dbHelper.deleteProduct(product);
                    refreshProducts();
                    Toast.makeText(getActivity(), "Product deleted", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void testRecyclerView() {
        Log.d(TAG, "Testing RecyclerView with dummy product data...");
        List<Product> dummyProducts = new ArrayList<>();
        dummyProducts.add(new Product("iPhone 15 Pro Max", "Apple", 1199.99, "Flagship Apple smartphone with A17 chip, 6.7-inch display, triple camera system."));
        dummyProducts.add(new Product("Galaxy S23 Ultra", "Samsung", 1099.99, "Samsung's top-tier phone with S Pen, 200MP camera, 6.8-inch AMOLED display."));
        dummyProducts.add(new Product("MacBook Pro 16-inch", "Apple", 2499.99, "Apple laptop with M2 Pro chip, 16-inch Retina display, 1TB SSD."));
        dummyProducts.add(new Product("Surface Pro 9", "Microsoft", 1399.99, "Microsoft 2-in-1 laptop/tablet, 13-inch touchscreen, Intel Evo platform."));
        dummyProducts.add(new Product("Sony WH-1000XM5", "Sony", 399.99, "Industry-leading noise-canceling wireless headphones, 30-hour battery life."));
        dummyProducts.add(new Product("Kindle Paperwhite", "Amazon", 139.99, "E-reader with 6.8-inch display, adjustable warm light, waterproof."));
        dummyProducts.add(new Product("GoPro HERO11", "GoPro", 499.99, "Waterproof action camera, 5.3K video, front/rear LCD screens."));
        dummyProducts.add(new Product("Apple Watch Series 9", "Apple", 399.99, "Smartwatch with always-on Retina display, ECG, blood oxygen sensor."));
        dummyProducts.add(new Product("Dell XPS 13", "Dell", 999.99, "13.4-inch FHD laptop, Intel Core i7, 16GB RAM, 512GB SSD."));
        dummyProducts.add(new Product("Bose QuietComfort Earbuds II", "Bose", 299.99, "Noise-cancelling wireless earbuds, up to 6 hours battery life."));
        // Insert into SQLite
        dbHelper.clearAllProducts();
        for (Product p : dummyProducts) dbHelper.addProduct(p);
        // Reload from SQLite
        List<Product> productsFromDb = dbHelper.getAllProducts();
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Setting up test adapter with " + productsFromDb.size() + " products from SQLite");
            productAdapter = new ProductAdapter(productsFromDb);
            mRecyclerView.setAdapter(productAdapter);
            productAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), "Test: Loaded " + productsFromDb.size() + " real products from SQLite", Toast.LENGTH_LONG).show();
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dbHelper = new BookDatabaseHelper(context);
    }
} 