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
import com.psu.sweng888.gthenewapp.data.Book;
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;

public class EditBookFragment extends Fragment {
    private static final String ARG_BOOK = "arg_book";
    private Book book;
    private EditText titleInput, authorInput, isbnInput, publisherInput;
    private Button saveButton;
    private BookDatabaseHelper dbHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    public static EditBookFragment newInstance(Book book) {
        EditBookFragment fragment = new EditBookFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_book, container, false);
        titleInput = view.findViewById(R.id.input_book_title);
        authorInput = view.findViewById(R.id.input_book_author);
        isbnInput = view.findViewById(R.id.input_book_isbn);
        publisherInput = view.findViewById(R.id.input_book_publisher);
        saveButton = view.findViewById(R.id.save_book_button);
        dbHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        if (getArguments() != null) {
            book = (Book) getArguments().getSerializable(ARG_BOOK);
            if (book != null) {
                titleInput.setText(book.getTitle());
                authorInput.setText(book.getAuthor());
                isbnInput.setText(book.getIsbn());
                publisherInput.setText(book.getPublisher());
            }
        }
        saveButton.setOnClickListener(v -> saveBook());
        return view;
    }
    private void saveBook() {
        String title = titleInput.getText().toString().trim();
        String author = authorInput.getText().toString().trim();
        String isbn = isbnInput.getText().toString().trim();
        String publisher = publisherInput.getText().toString().trim();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(isbn) || TextUtils.isEmpty(publisher)) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Book updatedBook = new Book(title, author, isbn, publisher);
        dbHelper.clearAllBooks(); // For demo, replace with updateBook(updatedBook) for real app
        firebaseDatabaseManager.addBook(updatedBook, task -> {
            Toast.makeText(getActivity(), "Book updated!", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }
} 