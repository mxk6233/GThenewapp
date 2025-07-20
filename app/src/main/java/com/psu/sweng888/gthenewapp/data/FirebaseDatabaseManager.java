package com.psu.sweng888.gthenewapp.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseManager {
    private static final String TAG = "FirebaseDatabaseManager";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private BookDatabaseHelper mSqliteHelper;
    private Context mContext;

    public FirebaseDatabaseManager(Context context) {
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mSqliteHelper = new BookDatabaseHelper(context);
        
        Log.d(TAG, "FirebaseDatabaseManager initialized");
        Log.d(TAG, "Firebase Database URL: " + mDatabase.toString());
        Log.d(TAG, "Firebase Auth instance: " + (mAuth != null ? "created" : "null"));
        
        // Test Firebase connection
        testFirebaseConnection();
    }
    
    // Test Firebase connection
    private void testFirebaseConnection() {
        Log.d(TAG, "Testing Firebase connection...");
        mDatabase.child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean connected = dataSnapshot.getValue(Boolean.class);
                Log.d(TAG, "Firebase connection status: " + (connected != null && connected ? "connected" : "disconnected"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase connection test failed", databaseError.toException());
            }
        });
    }

    // Add book to Firebase and sync to SQLite
    public void addBook(Book book, OnCompleteListener<Void> listener) {
        // Always add to SQLite
        mSqliteHelper.addBook(book);
        Log.d(TAG, "Book added to SQLite: " + book.getTitle());

        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated, skipping Firebase add");
            if (listener != null) {
                listener.onComplete(Tasks.forResult(null));
            }
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String bookId = mDatabase.child("users").child(userId).child("books").push().getKey();
        Map<String, Object> bookValues = new HashMap<>();
        bookValues.put("title", book.getTitle());
        bookValues.put("author", book.getAuthor());
        bookValues.put("isbn", book.getIsbn());
        bookValues.put("publisher", book.getPublisher());
        bookValues.put("id", bookId);

        mDatabase.child("users").child(userId).child("books").child(bookId)
                .setValue(bookValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Book added to Firebase successfully");
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(null));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add book to Firebase", e);
                        if (listener != null) {
                            listener.onComplete(Tasks.forException(e));
                        }
                    }
                });
    }

    // Get all books from Firebase and sync to SQLite
    public void getAllBooks(OnCompleteListener<List<Book>> listener) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated with Firebase");
            // Return SQLite data if Firebase is not available
            List<Book> books = mSqliteHelper.getAllRecords();
            Log.d(TAG, "Returning " + books.size() + " books from SQLite due to no Firebase auth");
            if (listener != null) {
                listener.onComplete(Tasks.forResult(books));
            }
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Fetching books for Firebase user: " + userId);
        
        mDatabase.child("users").child(userId).child("books")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Book> books = new ArrayList<>();
                        Log.d(TAG, "Firebase data snapshot has " + dataSnapshot.getChildrenCount() + " children");
                        
                        for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                            Book book = bookSnapshot.getValue(Book.class);
                            if (book != null) {
                                books.add(book);
                                Log.d(TAG, "Found book: " + book.getTitle() + " by " + book.getAuthor());
                            } else {
                                Log.w(TAG, "Failed to parse book from snapshot: " + bookSnapshot.getKey());
                            }
                        }
                        
                        Log.d(TAG, "Successfully loaded " + books.size() + " books from Firebase");
                        
                        // If Firebase is empty, populate with sample data
                        if (books.isEmpty()) {
                            Log.d(TAG, "Firebase is empty, populating with sample books...");
                            populateSampleBooksToFirebase(userId, () -> {
                                // After populating, fetch the books again
                                getAllBooks(listener);
                            });
                            return;
                        }
                        
                        // Sync to SQLite
                        syncToSQLite(books);
                        
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(books));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to get books from Firebase", databaseError.toException());
                        Log.e(TAG, "Database error code: " + databaseError.getCode() + ", message: " + databaseError.getMessage());
                        // Return SQLite data if Firebase fails
                        List<Book> books = mSqliteHelper.getAllRecords();
                        Log.d(TAG, "Returning " + books.size() + " books from SQLite due to Firebase error");
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(books));
                        }
                    }
                });
    }

    // Populate Firebase with sample books
    private void populateSampleBooksToFirebase(String userId, Runnable onComplete) {
        Log.d(TAG, "Adding sample books to Firebase for user: " + userId);
        
        // Sample books
        Book[] sampleBooks = {
            new Book("Atomic Habits 1", "James Clear", "0735211299", "Avery"),
            new Book("Android Programming", "Bryan Sills and Brian Gardner", "0137645546", "Addison-Wesley"),
            new Book("Software Architecture in Practice", "Less Bass and Paul Clements", "0136886094", "Addison-Wesley"),
            new Book("Rich Dad, Poor Dad", "Robert Kiyosaki", "1612681131", "Plata Publishing")
        };
        
        int[] completedBooks = {0};
        int totalBooks = sampleBooks.length;
        
        for (Book book : sampleBooks) {
            String bookId = mDatabase.child("users").child(userId).child("books").push().getKey();
            
            Map<String, Object> bookValues = new HashMap<>();
            bookValues.put("title", book.getTitle());
            bookValues.put("author", book.getAuthor());
            bookValues.put("isbn", book.getIsbn());
            bookValues.put("publisher", book.getPublisher());
            bookValues.put("id", bookId);
            
            mDatabase.child("users").child(userId).child("books").child(bookId)
                    .setValue(bookValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Added sample book to Firebase: " + book.getTitle());
                        completedBooks[0]++;
                        if (completedBooks[0] == totalBooks) {
                            Log.d(TAG, "All sample books added to Firebase successfully");
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add sample book to Firebase: " + book.getTitle(), e);
                        completedBooks[0]++;
                        if (completedBooks[0] == totalBooks) {
                            Log.d(TAG, "Some sample books failed to add, but continuing...");
                            onComplete.run();
                        }
                    });
        }
    }

    // Delete book from Firebase and SQLite
    public void deleteBook(String bookId, OnCompleteListener<Void> listener) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("books").child(bookId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Book deleted from Firebase successfully");
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(null));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to delete book from Firebase", e);
                        if (listener != null) {
                            listener.onComplete(Tasks.forException(e));
                        }
                    }
                });
    }

    // Sync Firebase data to SQLite
    private void syncToSQLite(List<Book> books) {
        // Clear existing SQLite data and repopulate
        mSqliteHelper.clearAllBooks();
        for (Book book : books) {
            mSqliteHelper.addBook(book);
        }
        Log.d(TAG, "Synced " + books.size() + " books to SQLite");
    }

    // Get books by author (search functionality)
    public void getBooksByAuthor(String author, OnCompleteListener<List<Book>> listener) {
        if (mAuth.getCurrentUser() == null) {
            // Use SQLite for offline search
            List<Book> books = mSqliteHelper.getMoviesByCategory(author);
            if (listener != null) {
                listener.onComplete(Tasks.forResult(books));
            }
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("books")
                .orderByChild("author")
                .equalTo(author)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Book> books = new ArrayList<>();
                        for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                            Book book = bookSnapshot.getValue(Book.class);
                            if (book != null) {
                                books.add(book);
                            }
                        }
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(books));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to search books by author", databaseError.toException());
                        // Fallback to SQLite
                        List<Book> books = mSqliteHelper.getMoviesByCategory(author);
                        if (listener != null) {
                            listener.onComplete(Tasks.forResult(books));
                        }
                    }
                });
    }

    // Check if user is authenticated
    public boolean isUserAuthenticated() {
        return mAuth.getCurrentUser() != null;
    }

    // Get current user ID
    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    // Public method to manually populate Firebase with sample books
    public void populateSampleBooks(OnCompleteListener<Void> listener) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot populate Firebase: User not authenticated");
            if (listener != null) {
                listener.onComplete(Tasks.forException(new Exception("User not authenticated")));
            }
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Manually populating Firebase with sample books for user: " + userId);
        
        populateSampleBooksToFirebase(userId, () -> {
            Log.d(TAG, "Manual population completed");
            if (listener != null) {
                listener.onComplete(Tasks.forResult(null));
            }
        });
    }

    // Check Firebase database configuration
    public void checkDatabaseConfiguration(OnCompleteListener<Boolean> listener) {
        Log.d(TAG, "Checking Firebase database configuration...");
        
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "Firebase database check failed: User not authenticated");
            if (listener != null) {
                listener.onComplete(Tasks.forResult(false));
            }
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Testing database access for user: " + userId);
        
        // Try to write a test value to check permissions
        mDatabase.child("users").child(userId).child("test").setValue("test_value")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase database write test successful");
                    // Clean up test value
                    mDatabase.child("users").child(userId).child("test").removeValue();
                    if (listener != null) {
                        listener.onComplete(Tasks.forResult(true));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase database write test failed", e);
                    if (listener != null) {
                        listener.onComplete(Tasks.forResult(false));
                    }
                });
    }
    
    // Sync SQLite data to Firebase
    public void syncSQLiteToFirebase(OnCompleteListener<Void> listener) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot sync to Firebase: User not authenticated");
            if (listener != null) {
                listener.onComplete(Tasks.forException(new Exception("User not authenticated")));
            }
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        List<Book> sqliteBooks = mSqliteHelper.getAllRecords();
        Log.d(TAG, "Syncing " + sqliteBooks.size() + " books from SQLite to Firebase");
        
        if (sqliteBooks.isEmpty()) {
            Log.d(TAG, "SQLite is empty, nothing to sync");
            if (listener != null) {
                listener.onComplete(Tasks.forResult(null));
            }
            return;
        }
        
        int[] completedBooks = {0};
        int totalBooks = sqliteBooks.size();
        
        for (Book book : sqliteBooks) {
            String bookId = mDatabase.child("users").child(userId).child("books").push().getKey();
            
            Map<String, Object> bookValues = new HashMap<>();
            bookValues.put("title", book.getTitle());
            bookValues.put("author", book.getAuthor());
            bookValues.put("isbn", book.getIsbn());
            bookValues.put("publisher", book.getPublisher());
            bookValues.put("id", bookId);
            
            mDatabase.child("users").child(userId).child("books").child(bookId)
                    .setValue(bookValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Synced book to Firebase: " + book.getTitle());
                        completedBooks[0]++;
                        if (completedBooks[0] == totalBooks) {
                            Log.d(TAG, "All books synced to Firebase successfully");
                            if (listener != null) {
                                listener.onComplete(Tasks.forResult(null));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to sync book to Firebase: " + book.getTitle(), e);
                        completedBooks[0]++;
                        if (completedBooks[0] == totalBooks) {
                            Log.d(TAG, "Some books failed to sync, but continuing...");
                            if (listener != null) {
                                listener.onComplete(Tasks.forResult(null));
                            }
                        }
                    });
        }
    }

    // --- PODCASTS ---
    public void addPodcast(Podcast podcast, OnCompleteListener<Void> listener) {
        // Always add to SQLite
        mSqliteHelper.addPodcast(podcast);
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated, skipping Firebase add");
            if (listener != null) listener.onComplete(Tasks.forResult(null));
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        String podcastId = mDatabase.child("users").child(userId).child("podcasts").push().getKey();
        Map<String, Object> podcastValues = new HashMap<>();
        podcastValues.put("title", podcast.getTitle());
        podcastValues.put("host", podcast.getHost());
        podcastValues.put("episodeCount", podcast.getEpisodeCount());
        podcastValues.put("publisher", podcast.getPublisher());
        podcastValues.put("id", podcastId);
        mDatabase.child("users").child(userId).child("podcasts").child(podcastId)
                .setValue(podcastValues)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(Tasks.forResult(null));
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(Tasks.forException(e));
                });
    }
    public void getAllPodcasts(OnCompleteListener<List<Podcast>> listener) {
        if (mAuth.getCurrentUser() == null) {
            List<Podcast> podcasts = mSqliteHelper.getAllPodcasts();
            if (listener != null) listener.onComplete(Tasks.forResult(podcasts));
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("podcasts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Podcast> podcasts = new ArrayList<>();
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Podcast podcast = snap.getValue(Podcast.class);
                            if (podcast != null) podcasts.add(podcast);
                        }
                        syncPodcastsToSQLite(podcasts);
                        if (listener != null) listener.onComplete(Tasks.forResult(podcasts));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        List<Podcast> podcasts = mSqliteHelper.getAllPodcasts();
                        if (listener != null) listener.onComplete(Tasks.forResult(podcasts));
                    }
                });
    }
    private void syncPodcastsToSQLite(List<Podcast> podcasts) {
        mSqliteHelper.clearAllPodcasts();
        for (Podcast podcast : podcasts) mSqliteHelper.addPodcast(podcast);
    }
    // --- PRODUCTS ---
    public void addProduct(Product product, OnCompleteListener<Void> listener) {
        // Always add to SQLite
        mSqliteHelper.addProduct(product);
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not authenticated, skipping Firebase add");
            if (listener != null) listener.onComplete(Tasks.forResult(null));
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        String productId = mDatabase.child("users").child(userId).child("products").push().getKey();
        Map<String, Object> productValues = new HashMap<>();
        productValues.put("name", product.getName());
        productValues.put("brand", product.getBrand());
        productValues.put("price", product.getPrice());
        productValues.put("description", product.getDescription());
        productValues.put("id", productId);
        mDatabase.child("users").child(userId).child("products").child(productId)
                .setValue(productValues)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(Tasks.forResult(null));
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(Tasks.forException(e));
                });
    }
    public void getAllProducts(OnCompleteListener<List<Product>> listener) {
        if (mAuth.getCurrentUser() == null) {
            List<Product> products = mSqliteHelper.getAllProducts();
            if (listener != null) listener.onComplete(Tasks.forResult(products));
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Product> products = new ArrayList<>();
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Product product = snap.getValue(Product.class);
                            if (product != null) products.add(product);
                        }
                        syncProductsToSQLite(products);
                        if (listener != null) listener.onComplete(Tasks.forResult(products));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        List<Product> products = mSqliteHelper.getAllProducts();
                        if (listener != null) listener.onComplete(Tasks.forResult(products));
                    }
                });
    }
    private void syncProductsToSQLite(List<Product> products) {
        mSqliteHelper.clearAllProducts();
        for (Product product : products) mSqliteHelper.addProduct(product);
    }

    // Sync all products from SQLite to Firebase
    public void syncProductsSQLiteToFirebase(OnCompleteListener<Void> listener) {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot sync products to Firebase: User not authenticated");
            if (listener != null) {
                listener.onComplete(Tasks.forException(new Exception("User not authenticated")));
            }
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        List<Product> sqliteProducts = mSqliteHelper.getAllProducts();
        Log.d(TAG, "Syncing " + sqliteProducts.size() + " products from SQLite to Firebase");
        if (sqliteProducts.isEmpty()) {
            Log.d(TAG, "SQLite is empty, nothing to sync");
            if (listener != null) {
                listener.onComplete(Tasks.forResult(null));
            }
            return;
        }
        int[] completed = {0};
        int total = sqliteProducts.size();
        for (Product product : sqliteProducts) {
            String productId = mDatabase.child("users").child(userId).child("products").push().getKey();
            Map<String, Object> productValues = new HashMap<>();
            productValues.put("name", product.getName());
            productValues.put("brand", product.getBrand());
            productValues.put("price", product.getPrice());
            productValues.put("description", product.getDescription());
            productValues.put("id", productId);
            mDatabase.child("users").child(userId).child("products").child(productId)
                .setValue(productValues)
                .addOnSuccessListener(aVoid -> {
                    completed[0]++;
                    if (completed[0] == total && listener != null) {
                        listener.onComplete(Tasks.forResult(null));
                    }
                })
                .addOnFailureListener(e -> {
                    completed[0]++;
                    if (completed[0] == total && listener != null) {
                        listener.onComplete(Tasks.forException(e));
                    }
                });
        }
    }
} 