package com.psu.sweng888.gthenewapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BookDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "BookDatabaseHelper";
    private static final String DATABASE_NAME = "bookstore_database";
    private static final int DATABASE_VERSION = 3; // Bumped version to force upgrade
    private static final String TABLE_BOOKS = "books";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_ISBN = "isbn";
    private static final String KEY_PUBLISHER = "publisher";
    private static final String KEY_COVER_IMAGE_URI = "cover_image_uri";

    private static final String TABLE_PODCASTS = "podcasts";
    private static final String KEY_PODCAST_ID = "id";
    private static final String KEY_PODCAST_TITLE = "title";
    private static final String KEY_PODCAST_HOST = "host";
    private static final String KEY_PODCAST_EPISODE_COUNT = "episode_count";
    private static final String KEY_PODCAST_PUBLISHER = "publisher";

    private static final String TABLE_PRODUCTS = "products";
    private static final String KEY_PRODUCT_ID = "id";
    private static final String KEY_PRODUCT_NAME = "name";
    private static final String KEY_PRODUCT_BRAND = "brand";
    private static final String KEY_PRODUCT_PRICE = "price";
    private static final String KEY_PRODUCT_DESCRIPTION = "description";

    private Context mContext; // Added mContext field

    public BookDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "BookDatabaseHelper initialized with database: " + DATABASE_NAME);
        this.mContext = context; // Initialize mContext
        
        // Check if database file exists
        String dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        Log.d(TAG, "Database file path: " + dbPath);
        Log.d(TAG, "Database file exists: " + context.getDatabasePath(DATABASE_NAME).exists());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database table: " + TABLE_BOOKS);
        db.execSQL(createMoviesTable());
        Log.d(TAG, "Database table created successfully");
        db.execSQL(createPodcastsTable());
        db.execSQL(createProductsTable());
        android.widget.Toast.makeText(mContext, "Book DB onCreate: Tables created", android.widget.Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PODCASTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
        android.widget.Toast.makeText(mContext, "Book DB onUpgrade: Tables dropped and recreated", android.widget.Toast.LENGTH_LONG).show();
    }

    /** Query to Create the Database*/
    private String createMoviesTable(){
        String QUERY_CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_BOOKS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, "+
                KEY_TITLE + " TEXT," +
                KEY_AUTHOR + " TEXT," +
                KEY_ISBN + " TEXT," +
                KEY_PUBLISHER + " TEXT," +
                KEY_COVER_IMAGE_URI + " TEXT" +
                ")";
        return QUERY_CREATE_MOVIES_TABLE;
    }

    private String createPodcastsTable() {
        return "CREATE TABLE " + TABLE_PODCASTS + "(" +
                KEY_PODCAST_ID + " INTEGER PRIMARY KEY, " +
                KEY_PODCAST_TITLE + " TEXT," +
                KEY_PODCAST_HOST + " TEXT," +
                KEY_PODCAST_EPISODE_COUNT + " INTEGER," +
                KEY_PODCAST_PUBLISHER + " TEXT" +
                ")";
    }
    private String createProductsTable() {
        return "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                KEY_PRODUCT_ID + " INTEGER PRIMARY KEY, " +
                KEY_PRODUCT_NAME + " TEXT," +
                KEY_PRODUCT_BRAND + " TEXT," +
                KEY_PRODUCT_PRICE + " REAL," +
                KEY_PRODUCT_DESCRIPTION + " TEXT" +
                ")";
    }

    /** Other queries */
    public void addBook(Book book){
        Log.d(TAG, "Adding book to database: " + book.getTitle());
        /** Get a Writable instance of the database */
        SQLiteDatabase database = this.getWritableDatabase();
        /** Create a ContentValues to persist information on the database */
        ContentValues values = new ContentValues();
        /** Populate the object with the values from the Movie to be added.
         *  There is no need to include the ID because it is autogenerated by the SQLIte*/
        values.put(KEY_TITLE, book.getTitle());
        values.put(KEY_AUTHOR, book.getAuthor());
        values.put(KEY_ISBN, book.getIsbn());
        values.put(KEY_PUBLISHER, book.getPublisher());
        values.put(KEY_COVER_IMAGE_URI, book.getCoverImageUri());
        /** Insert the values on the TABLE_MOVIES */
        long result = database.insert(TABLE_BOOKS, null, values);
        Log.d(TAG, "Book insert result: " + result + " (row ID)");
        if (result == -1) {
            Log.e(TAG, "Failed to add book to SQLite: " + book.getTitle());
        } else {
            Log.d(TAG, "Book added to SQLite successfully: " + book.getTitle());
        }
        /** Close the connection with the database */
        database.close();
    }

    public List<Book> getMoviesByCategory (String category){
        List<Book> movieList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS + " WHERE " + KEY_AUTHOR + " = ?";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, new String[]{category});

        if (cursor.moveToFirst()){
            do {
                Book book = new Book(
                        cursor.getString(1), // TITLE
                        cursor.getString(2), // AUTHOR
                        cursor.getString(3), // ISBN
                        cursor.getString(4) // PUBLISHER
                );
                movieList.add(book);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return  movieList;
    }

    public List<Book> getAllRecords(){
        Log.d(TAG, "Getting all records from database");
        List<Book> bookList = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+ TABLE_BOOKS;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        Log.d(TAG, "Cursor count: " + cursor.getCount());
        Log.d(TAG, "Column count: " + cursor.getColumnCount());
        String[] columnNames = cursor.getColumnNames();
        for (int i = 0; i < columnNames.length; i++) {
            Log.d(TAG, "Column " + i + ": " + columnNames[i]);
        }

        int idxTitle = cursor.getColumnIndex(KEY_TITLE);
        int idxAuthor = cursor.getColumnIndex(KEY_AUTHOR);
        int idxIsbn = cursor.getColumnIndex(KEY_ISBN);
        int idxPublisher = cursor.getColumnIndex(KEY_PUBLISHER);
        int idxCoverUri = cursor.getColumnIndex(KEY_COVER_IMAGE_URI);

        if (cursor.moveToFirst()){
            do {
                String title = idxTitle != -1 ? cursor.getString(idxTitle) : "";
                String author = idxAuthor != -1 ? cursor.getString(idxAuthor) : "";
                String isbn = idxIsbn != -1 ? cursor.getString(idxIsbn) : "";
                String publisher = idxPublisher != -1 ? cursor.getString(idxPublisher) : "";
                String coverUri = idxCoverUri != -1 ? cursor.getString(idxCoverUri) : null;
                Book book = new Book(title, author, isbn, publisher, coverUri);
                bookList.add(book);
                Log.d(TAG, "Retrieved book: " + book.getTitle() + " by " + book.getAuthor());
            } while (cursor.moveToNext());
        } else {
            Log.w(TAG, "No books found in database");
        }

        cursor.close();
        database.close();
        Log.d(TAG, "Returning " + bookList.size() + " books from database");
        return bookList;
    }

    public boolean isDatabaseEmpty() {
        boolean isEmpty = true;
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKS, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            Log.d(TAG, "Database record count: " + count);
            if (count > 0) {
                isEmpty = false;
            }
            cursor.close();
        }
        database.close();
        return isEmpty;
    }

    public void populateMoviesDatabase(){
        Log.d(TAG, "Populating database with sample books");
        addBook(new Book("Atomic Habits", "James Clear", "0735211299", "Avery"));
        addBook(new Book("Clean Code", "Robert C. Martin", "9780132350884", "Prentice Hall"));
        addBook(new Book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "9780201616224", "Addison-Wesley"));
        addBook(new Book("Design Patterns", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "9780201633610", "Addison-Wesley"));
        addBook(new Book("Introduction to Algorithms", "Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein", "9780262033848", "MIT Press"));
        Log.d(TAG, "Sample books added to database");
    }

    public void clearAllBooks() {
        Log.d(TAG, "Clearing all books from database");
        SQLiteDatabase database = this.getWritableDatabase();
        int deletedRows = database.delete(TABLE_BOOKS, null, null);
        Log.d(TAG, "Deleted " + deletedRows + " rows from database");
        database.close();
    }

    public void deleteBook(Book book) {
        Log.d(TAG, "Deleting book from database: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
        SQLiteDatabase database = this.getWritableDatabase();
        int deletedRows = database.delete(TABLE_BOOKS, KEY_ISBN + " = ?", new String[]{book.getIsbn()});
        Log.d(TAG, "Deleted " + deletedRows + " row(s) for book with ISBN: " + book.getIsbn());
        database.close();
    }

    // --- PODCASTS CRUD ---
    public void addPodcast(Podcast podcast) {
        Log.d(TAG, "Adding podcast to database: " + podcast.getTitle());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PODCAST_TITLE, podcast.getTitle());
        values.put(KEY_PODCAST_HOST, podcast.getHost());
        values.put(KEY_PODCAST_EPISODE_COUNT, podcast.getEpisodeCount());
        values.put(KEY_PODCAST_PUBLISHER, podcast.getPublisher());
        db.insert(TABLE_PODCASTS, null, values);
        db.close();
    }
    public List<Podcast> getAllPodcasts() {
        Log.d(TAG, "Getting all podcasts from database");
        List<Podcast> podcastList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PODCASTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Podcast podcast = new Podcast(
                        cursor.getString(1), // TITLE
                        cursor.getString(2), // HOST
                        cursor.getInt(3),    // EPISODE_COUNT
                        cursor.getString(4)  // PUBLISHER
                );
                podcastList.add(podcast);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Returning " + podcastList.size() + " podcasts from database");
        return podcastList;
    }
    public void clearAllPodcasts() {
        Log.d(TAG, "Clearing all podcasts from database");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PODCASTS, null, null);
        db.close();
    }
    public boolean isPodcastsEmpty() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PODCASTS, null);
        boolean isEmpty = true;
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            isEmpty = count == 0;
            cursor.close();
        }
        db.close();
        return isEmpty;
    }
    public void populatePodcastsDatabase() {
        Log.d(TAG, "Populating database with sample podcasts");
        addPodcast(new Podcast("The Daily", "Michael Barbaro", 2000, "The New York Times"));
        addPodcast(new Podcast("How I Built This", "Guy Raz", 500, "NPR"));
        addPodcast(new Podcast("Science Vs", "Wendy Zukerman", 300, "Gimlet"));
        addPodcast(new Podcast("99% Invisible", "Roman Mars", 500, "PRX"));
        addPodcast(new Podcast("Radiolab", "Jad Abumrad, Robert Krulwich", 600, "WNYC Studios"));
        Log.d(TAG, "Sample podcasts added to database");
    }
    public void deletePodcast(Podcast podcast) {
        Log.d(TAG, "Deleting podcast from database: " + podcast.getTitle());
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_PODCASTS, KEY_PODCAST_TITLE + " = ?", new String[]{podcast.getTitle()});
        Log.d(TAG, "Deleted " + deletedRows + " row(s) for podcast with title: " + podcast.getTitle());
        db.close();
    }

    // --- PRODUCTS CRUD ---
    public void addProduct(Product product) {
        Log.d(TAG, "Adding product to database: " + product.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_NAME, product.getName());
        values.put(KEY_PRODUCT_BRAND, product.getBrand());
        values.put(KEY_PRODUCT_PRICE, product.getPrice());
        values.put(KEY_PRODUCT_DESCRIPTION, product.getDescription());
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }
    public List<Product> getAllProducts() {
        Log.d(TAG, "Getting all products from database");
        List<Product> productList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(1), // NAME
                        cursor.getString(2), // BRAND
                        cursor.getDouble(3), // PRICE
                        cursor.getString(4)  // DESCRIPTION
                );
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Returning " + productList.size() + " products from database");
        return productList;
    }
    public void clearAllProducts() {
        Log.d(TAG, "Clearing all products from database");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, null, null);
        db.close();
    }
    public boolean isProductsEmpty() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        boolean isEmpty = true;
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            isEmpty = count == 0;
            cursor.close();
        }
        db.close();
        return isEmpty;
    }
    public void populateProductsDatabase() {
        Log.d(TAG, "Populating database with sample products");
        addProduct(new Product("iPhone 15 Pro Max", "Apple", 1199.99, "Flagship Apple smartphone with A17 chip, 6.7-inch display, triple camera system."));
        addProduct(new Product("Galaxy S23 Ultra", "Samsung", 1099.99, "Samsung's top-tier phone with S Pen, 200MP camera, 6.8-inch AMOLED display."));
        addProduct(new Product("MacBook Pro 16-inch", "Apple", 2499.99, "Apple laptop with M2 Pro chip, 16-inch Retina display, 1TB SSD."));
        addProduct(new Product("Surface Pro 9", "Microsoft", 1399.99, "Microsoft 2-in-1 laptop/tablet, 13-inch touchscreen, Intel Evo platform."));
        addProduct(new Product("Sony WH-1000XM5", "Sony", 399.99, "Industry-leading noise-canceling wireless headphones, 30-hour battery life."));
        Log.d(TAG, "Sample products added to database");
    }
    public void deleteProduct(Product product) {
        Log.d(TAG, "Deleting product from database: " + product.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_PRODUCTS, KEY_PRODUCT_NAME + " = ?", new String[]{product.getName()});
        Log.d(TAG, "Deleted " + deletedRows + " row(s) for product with name: " + product.getName());
        db.close();
    }
}
