package com.psu.sweng888.gthenewapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ArrayList;

import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.Book;
import com.psu.sweng888.gthenewapp.adapter.BookAdapter;
import com.psu.sweng888.gthenewapp.data.BookDatabaseHelper;
import com.psu.sweng888.gthenewapp.data.FirebaseDatabaseManager;
import com.psu.sweng888.gthenewapp.util.RecyclerItemClickListener;
import com.psu.sweng888.gthenewapp.fragments.EditBookFragment;

public class BooksListFragment extends Fragment {

    private static final String TAG = "BooksListFragment";
    private BookAdapter bookAdapter;
    private RecyclerView mRecyclerView;
    private BookDatabaseHelper bookDatabaseHelper;
    private FirebaseDatabaseManager firebaseDatabaseManager;
    private ArrayAdapter<String> autoAdapter; // Store as a field

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /** Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        /** Instantiate the RecyclerView */
        mRecyclerView = view.findViewById(R.id.recyclerView);
        
        if (mRecyclerView == null) {
            Log.e(TAG, "RecyclerView is null!");
            return view;
        }
        
        Log.d(TAG, "RecyclerView found, setting up layout manager");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        // Debug: Check RecyclerView properties
        Log.d(TAG, "RecyclerView width: " + mRecyclerView.getWidth() + ", height: " + mRecyclerView.getHeight());
        Log.d(TAG, "RecyclerView visibility: " + mRecyclerView.getVisibility());

        bookDatabaseHelper = new BookDatabaseHelper(getActivity());
        firebaseDatabaseManager = new FirebaseDatabaseManager(getActivity());
        
        // Set up refresh button
        Button refreshButton = view.findViewById(R.id.refresh_button);
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> {
                Log.d(TAG, "Refresh button clicked");
                // First test RecyclerView with dummy data
                testRecyclerView();
                // Then try to load real data
                refreshBooks();
            });
            
            // Long press to reset database
            refreshButton.setOnLongClickListener(v -> {
                Log.d(TAG, "Refresh button long pressed - resetting database");
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
                    firebaseDatabaseManager.getAllBooks(task -> {
                        List<Book> books = task.getResult();
                        if (books != null && !books.isEmpty()) {
                            getActivity().runOnUiThread(() -> {
                                bookAdapter = new BookAdapter(books);
                                mRecyclerView.setAdapter(bookAdapter);
                                bookAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "Synced " + books.size() + " books from Firebase", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "No books found in Firebase", Toast.LENGTH_SHORT).show());
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
                    List<Book> books = bookDatabaseHelper.getAllRecords();
                    if (books != null && !books.isEmpty()) {
                        int[] completed = {0};
                        for (Book book : books) {
                            firebaseDatabaseManager.addBook(book, task -> {
                                completed[0]++;
                                if (completed[0] == books.size()) {
                                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Pushed " + books.size() + " books to Firebase", Toast.LENGTH_SHORT).show());
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getActivity(), "No books in SQLite to push", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Not authenticated with Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Set up AutoCompleteTextView
        AutoCompleteTextView searchAuto = view.findViewById(R.id.book_search_autocomplete);
        autoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
        searchAuto.setAdapter(autoAdapter);
        searchAuto.setThreshold(1);
        searchAuto.setOnItemClickListener((parent, v, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (bookAdapter != null) bookAdapter.filter(selected);
        });
        searchAuto.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (bookAdapter != null) bookAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        
        Log.d(TAG, "BooksListFragment created, loading books...");
        
        // Load books
        loadBooks(autoAdapter);
        
        return view;
    }
    
    private void loadBooks(ArrayAdapter<String> autoAdapter) {
        // Check if user is authenticated with Firebase
        boolean isFirebaseAuthenticated = firebaseDatabaseManager.isUserAuthenticated();
        Log.d(TAG, "Firebase authentication status: " + isFirebaseAuthenticated);
        
        if (isFirebaseAuthenticated) {
            // Try to load from Firebase first
            Log.d(TAG, "User is Firebase authenticated, loading from Firebase...");
            firebaseDatabaseManager.getAllBooks(task -> {
                List<Book> books = task.getResult();
                if (books == null || books.isEmpty()) {
                    bookDatabaseHelper.populateMoviesDatabase();
                    books = bookDatabaseHelper.getAllRecords();
                }
                final List<Book> finalBooks = books;
                getActivity().runOnUiThread(() -> {
                    bookAdapter = new BookAdapter(finalBooks);
                    mRecyclerView.setAdapter(bookAdapter);
                    autoAdapter.clear();
                    for (Book b : finalBooks) autoAdapter.add(b.getTitle() + " — " + b.getAuthor() + " — " + b.getIsbn() + " — " + b.getPublisher());
                    autoAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Displaying " + finalBooks.size() + " books from Firebase/SQLite");
                });
            });
        } else {
            // Not Firebase authenticated, use SQLite
            Log.d(TAG, "User not Firebase authenticated, loading from SQLite...");
            if (bookDatabaseHelper.isDatabaseEmpty()) {
                bookDatabaseHelper.populateMoviesDatabase();
            }
            List<Book> books = bookDatabaseHelper.getAllRecords();
            final List<Book> finalBooks = books;
            getActivity().runOnUiThread(() -> {
                bookAdapter = new BookAdapter(finalBooks);
                mRecyclerView.setAdapter(bookAdapter);
                autoAdapter.clear();
                for (Book b : finalBooks) autoAdapter.add(b.getTitle() + " — " + b.getAuthor() + " — " + b.getIsbn() + " — " + b.getPublisher());
                autoAdapter.notifyDataSetChanged();
                Log.d(TAG, "Displaying " + finalBooks.size() + " books from SQLite");
            });
        }
    }
    
    private void loadFromSQLite() {
        // Ensure SQLite has sample data
        if (bookDatabaseHelper.isDatabaseEmpty()) {
            Log.d(TAG, "SQLite database is empty, populating with sample data...");
            bookDatabaseHelper.populateMoviesDatabase();
        }
        
        List<Book> books = bookDatabaseHelper.getAllRecords();
        Log.d(TAG, "SQLite returned " + books.size() + " books");
        
        // Debug: Print each book
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            Log.d(TAG, "Book " + i + ": " + book.getTitle() + " by " + book.getAuthor());
        }
        
        // Fallback: If still no books, force populate with sample data
        if (books.isEmpty()) {
            Log.w(TAG, "Still no books after population, forcing sample data...");
            forcePopulateSampleData();
            books = bookDatabaseHelper.getAllRecords();
        }
        
        // Create final variable for lambda
        final List<Book> finalBooks = books;
        
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Setting up RecyclerView adapter with " + finalBooks.size() + " books");
            bookAdapter = new BookAdapter(finalBooks);
            mRecyclerView.setAdapter(bookAdapter);
            // Add long-press for edit/delete
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {}
                @Override
                public void onLongItemClick(View view, int position) {
                    showEditDeleteDialog(finalBooks.get(position));
                }
            }));
            Log.d(TAG, "Displaying " + finalBooks.size() + " books from SQLite");
            
            // Debug: Check if adapter has items
            Log.d(TAG, "Adapter item count: " + bookAdapter.getItemCount());
            
            // Force refresh the RecyclerView
            bookAdapter.notifyDataSetChanged();
            
            // Show a toast with the number of books
            Toast.makeText(getActivity(), "Loaded " + finalBooks.size() + " books from local database", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void populateSampleData() {
        // Clear existing data and add sample books
        bookDatabaseHelper.clearAllBooks();
        bookDatabaseHelper.populateMoviesDatabase();
        Log.d(TAG, "Sample data populated in SQLite");
    }
    
    private void forcePopulateSampleData() {
        Log.d(TAG, "Force populating sample data...");
        bookDatabaseHelper.clearAllBooks();
        bookDatabaseHelper.populateMoviesDatabase();
        
        // Verify the data was added
        List<Book> books = bookDatabaseHelper.getAllRecords();
        Log.d(TAG, "After force population, database has " + books.size() + " books");
        
        for (Book book : books) {
            Log.d(TAG, "Force populated book: " + book.getTitle() + " by " + book.getAuthor());
        }
    }
    
    // Test method to verify RecyclerView is working
    private void testRecyclerView() {
        Log.d(TAG, "Testing RecyclerView with dummy data...");
        List<Book> dummyBooks = new ArrayList<>();
        dummyBooks.add(new Book("Atomic Habits", "James Clear", "0735211299", "Avery"));
        dummyBooks.add(new Book("Clean Code", "Robert C. Martin", "9780132350884", "Prentice Hall"));
        dummyBooks.add(new Book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "9780201616224", "Addison-Wesley"));
        dummyBooks.add(new Book("Design Patterns", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "9780201633610", "Addison-Wesley"));
        dummyBooks.add(new Book("Introduction to Algorithms", "Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein", "9780262033848", "MIT Press"));
        dummyBooks.add(new Book("Refactoring", "Martin Fowler", "9780201485677", "Addison-Wesley"));
        dummyBooks.add(new Book("Effective Java", "Joshua Bloch", "9780134685991", "Addison-Wesley"));
        dummyBooks.add(new Book("Head First Design Patterns", "Eric Freeman, Bert Bates, Kathy Sierra, Elisabeth Robson", "9780596007126", "O'Reilly Media"));
        dummyBooks.add(new Book("You Don't Know JS", "Kyle Simpson", "9781491904244", "O'Reilly Media"));
        dummyBooks.add(new Book("Cracking the Coding Interview", "Gayle Laakmann McDowell", "9780984782857", "CareerCup"));
        // Insert into SQLite
        bookDatabaseHelper.clearAllBooks();
        for (Book b : dummyBooks) bookDatabaseHelper.addBook(b);
        // Reload from SQLite
        List<Book> booksFromDb = bookDatabaseHelper.getAllRecords();
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Setting up test adapter with " + booksFromDb.size() + " books from SQLite");
            bookAdapter = new BookAdapter(booksFromDb);
            mRecyclerView.setAdapter(bookAdapter);
            bookAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), "Test: Loaded " + booksFromDb.size() + " real books from SQLite", Toast.LENGTH_LONG).show();
        });
    }
    
    // Public method to refresh the books list
    public void refreshBooks() {
        Log.d(TAG, "Refreshing books list...");
        loadBooks(autoAdapter);
    }

    // Method to reset the entire database
    private void resetDatabase() {
        Log.d(TAG, "Resetting database...");
        bookDatabaseHelper.clearAllBooks();
        Log.d(TAG, "Database cleared.");
        Toast.makeText(getActivity(), "Database reset.", Toast.LENGTH_SHORT).show();
        loadBooks(autoAdapter); // Reload data after reset
    }

    private void showEditDeleteDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(book.getTitle())
            .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                if (which == 0) {
                    // Edit
                    EditBookFragment editFragment = EditBookFragment.newInstance(book);
                    getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
                } else if (which == 1) {
                    // Delete
                    bookDatabaseHelper.clearAllBooks(); // Remove all books and repopulate (for demo, replace with deleteBook(book) for real app)
                    firebaseDatabaseManager.deleteBook(book.getIsbn(), task -> refreshBooks());
                    Toast.makeText(getActivity(), "Book deleted", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bookDatabaseHelper = new BookDatabaseHelper(context);
    }
}
