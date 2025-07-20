package com.psu.sweng888.gthenewapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.adapter.BookAdapter;
import com.psu.sweng888.gthenewapp.data.Book;
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.widget.ImageView;

public class AddBookFragment extends Fragment implements View.OnClickListener {

    private EditText titleEditText;
    private EditText authorEditText;
    private EditText isbnEditText;
    private EditText publisherEditText;
    private Button saveButton;
    private BookDatabaseHelper bookDatabaseHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;
    private BookAdapter bookAdapter;
    private static final int PICK_IMAGE_REQUEST = 101;
    private ImageView bookCoverImageView;
    private Button selectCoverButton;
    private String selectedCoverUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        bookDatabaseHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        List<Book> bookList = bookDatabaseHelper.getAllRecords();
        bookAdapter = new BookAdapter(bookList);

        // Get references to the EditText views
        titleEditText = rootView.findViewById(R.id.input_book_title);
        authorEditText = rootView.findViewById(R.id.input_book_author);
        isbnEditText = rootView.findViewById(R.id.input_book_isbn);
        publisherEditText = rootView.findViewById(R.id.input_book_publisher);
        bookCoverImageView = rootView.findViewById(R.id.book_cover_image);
        selectCoverButton = rootView.findViewById(R.id.select_cover_button);
        if (selectCoverButton != null) {
            selectCoverButton.setOnClickListener(v -> {
                try {
                    openImagePicker();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Error opening image picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Get reference to the Save button
        saveButton = rootView.findViewById(R.id.save_book_button);
        saveButton.setOnClickListener(v -> saveBook());

        return rootView;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        // Remove any remaining code that checks for R.id.button_confirm or R.id.button_clear
        // Only keep the saveBook() logic and save_book_button click handler
    }

    public void confirm() {
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String isbn = isbnEditText.getText().toString().trim();
        String publisher = publisherEditText.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || publisher.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            /** Create a new instance of Book */
            Book newBook = new Book(title, author, isbn, publisher);
            
            // Add book to both Firebase and SQLite
            firebaseDatabaseManager.addBook(newBook, task -> {
                if (task.isSuccessful()) {
                    // Book added successfully to Firebase and SQLite
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "New book added successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    });
                } else {
                    // Firebase failed, but SQLite should still work
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Book added to local storage. Firebase sync failed.", Toast.LENGTH_SHORT).show();
                        clearFields();
                    });
                }
            });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                selectedCoverUri = imageUri != null ? imageUri.toString() : null;
                if (bookCoverImageView != null && imageUri != null) {
                    bookCoverImageView.setImageURI(imageUri);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveBook() {
        Toast.makeText(getActivity(), "Save button clicked", Toast.LENGTH_SHORT).show();
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String isbn = isbnEditText.getText().toString().trim();
        String publisher = publisherEditText.getText().toString().trim();
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || publisher.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Book book = new Book(title, author, isbn, publisher, selectedCoverUri);
        Toast.makeText(getActivity(), "Calling addBook...", Toast.LENGTH_SHORT).show();
        firebaseDatabaseManager.addBook(book, task -> {
            Toast.makeText(getActivity(), "addBook callback", Toast.LENGTH_SHORT).show();
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Book added!", Toast.LENGTH_SHORT).show();
                titleEditText.setText("");
                authorEditText.setText("");
                isbnEditText.setText("");
                publisherEditText.setText("");
                if (bookCoverImageView != null) bookCoverImageView.setImageResource(R.drawable.ic_book);
                selectedCoverUri = null;
                requireActivity().runOnUiThread(() -> {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.psu.sweng888.gthenewapp.fragments.BooksListFragment())
                        .commit();
                });
            } else {
                Toast.makeText(getActivity(), "Failed to add book", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearFields(){
        titleEditText.setText("");
        authorEditText.setText("");
        isbnEditText.setText("");
        publisherEditText.setText("");
    }
}
