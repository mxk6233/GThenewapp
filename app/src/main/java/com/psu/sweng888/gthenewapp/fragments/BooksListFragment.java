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
        
        // Set up SearchView
        SearchView searchView = view.findViewById(R.id.book_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (bookAdapter != null) bookAdapter.filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (bookAdapter != null) bookAdapter.filter(newText);
                return true;
            }
        });
        
        Log.d(TAG, "BooksListFragment created, loading books...");
        
        // Load books
        loadBooks();
        
        return view;
    }
    
    private void loadBooks() {
        // Check if user is authenticated with Firebase
        boolean isFirebaseAuthenticated = firebaseDatabaseManager.isUserAuthenticated();
        Log.d(TAG, "Firebase authentication status: " + isFirebaseAuthenticated);
        
        if (isFirebaseAuthenticated) {
            // Try to load from Firebase first
            Log.d(TAG, "User is Firebase authenticated, loading from Firebase...");
            firebaseDatabaseManager.getAllBooks(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Book> books = task.getResult();
                    Log.d(TAG, "Firebase returned " + books.size() + " books");
                    
                    if (books.isEmpty()) {
                        // Firebase is empty, populate with sample data
                        Log.d(TAG, "Firebase is empty, populating with sample data...");
                        populateSampleData();
                        books = bookDatabaseHelper.getAllRecords();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Firebase was empty. Sample books loaded from local database.", Toast.LENGTH_LONG).show();
                        });
                    }
                    
                    final List<Book> finalBooks = books;
                    getActivity().runOnUiThread(() -> {
                        bookAdapter = new BookAdapter(finalBooks);
                        mRecyclerView.setAdapter(bookAdapter);
                        Log.d(TAG, "Displaying " + finalBooks.size() + " books from Firebase/SQLite");
                    });
                } else {
                    // Firebase failed, use SQLite
                    Log.e(TAG, "Firebase failed, falling back to SQLite", task.getException());
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Firebase connection failed. Loading from local database.", Toast.LENGTH_LONG).show();
                    });
                    loadFromSQLite();
                }
            });
        } else {
            // Not Firebase authenticated, use SQLite
            Log.d(TAG, "User not Firebase authenticated, loading from SQLite...");
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Not connected to Firebase. Loading from local database.", Toast.LENGTH_LONG).show();
            });
            loadFromSQLite();
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
        dummyBooks.add(new Book("Test Book 1", "Test Author 1", "1234567890", "Test Publisher 1"));
        dummyBooks.add(new Book("Test Book 2", "Test Author 2", "0987654321", "Test Publisher 2"));
        dummyBooks.add(new Book("Test Book 3", "Test Author 3", "1122334455", "Test Publisher 3"));
        
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Setting up test adapter with " + dummyBooks.size() + " dummy books");
            bookAdapter = new BookAdapter(dummyBooks);
            mRecyclerView.setAdapter(bookAdapter);
            bookAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), "Test: Loaded " + dummyBooks.size() + " dummy books", Toast.LENGTH_LONG).show();
        });
    }
    
    // Public method to refresh the books list
    public void refreshBooks() {
        Log.d(TAG, "Refreshing books list...");
        loadBooks();
    }

    // Method to reset the entire database
    private void resetDatabase() {
        Log.d(TAG, "Resetting database...");
        bookDatabaseHelper.clearAllBooks();
        Log.d(TAG, "Database cleared.");
        Toast.makeText(getActivity(), "Database reset.", Toast.LENGTH_SHORT).show();
        loadBooks(); // Reload data after reset
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
