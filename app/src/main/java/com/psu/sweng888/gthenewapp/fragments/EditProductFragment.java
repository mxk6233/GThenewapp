package com.psu.sweng888.gthenewapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.data.Product;

public class EditProductFragment extends Fragment {
    private static final String ARG_PRODUCT = "arg_product";
    private Product product;
    private EditText nameInput, brandInput, priceInput, descriptionInput;
    private Button saveButton;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    public static EditProductFragment newInstance(Product product) {
        EditProductFragment fragment = new EditProductFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);
        nameInput = view.findViewById(R.id.input_product_name);
        brandInput = view.findViewById(R.id.input_product_brand);
        priceInput = view.findViewById(R.id.input_product_price);
        descriptionInput = view.findViewById(R.id.input_product_description);
        saveButton = view.findViewById(R.id.save_product_button);
        dbHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
            if (product != null) {
                nameInput.setText(product.getName());
                brandInput.setText(product.getBrand());
                priceInput.setText(String.valueOf(product.getPrice()));
                descriptionInput.setText(product.getDescription());
            }
        }
        saveButton.setOnClickListener(v -> saveProduct());
        return view;
    }
    private void saveProduct() {
        String name = nameInput.getText().toString().trim();
        String brand = brandInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(brand) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(description)) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Price must be a number", Toast.LENGTH_SHORT).show();
            return;
        }
        Product updatedProduct = new Product(name, brand, price, description);
        dbHelper.clearAllProducts(); // For demo, replace with updateProduct(updatedProduct) for real app
        firebaseDatabaseManager.addProduct(updatedProduct, task -> {
            Toast.makeText(getActivity(), "Product updated!", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }
} 