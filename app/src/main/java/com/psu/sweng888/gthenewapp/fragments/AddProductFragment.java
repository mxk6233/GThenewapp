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
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.data.Product;

public class AddProductFragment extends Fragment {
    private EditText nameInput, brandInput, priceInput, descriptionInput;
    private Button saveButton;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);
        nameInput = view.findViewById(R.id.input_product_name);
        brandInput = view.findViewById(R.id.input_product_brand);
        priceInput = view.findViewById(R.id.input_product_price);
        descriptionInput = view.findViewById(R.id.input_product_description);
        saveButton = view.findViewById(R.id.save_product_button);
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
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
        Product product = new Product(name, brand, price, description);
        firebaseDatabaseManager.addProduct(product, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Product added!", Toast.LENGTH_SHORT).show();
                nameInput.setText("");
                brandInput.setText("");
                priceInput.setText("");
                descriptionInput.setText("");
            } else {
                Toast.makeText(getActivity(), "Failed to add product", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 