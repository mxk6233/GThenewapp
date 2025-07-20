package com.psu.sweng888.gthenewapp.data;

import java.util.ArrayList;
import java.util.List;

public class BookDataSource {

    private BookDatabaseHelper databaseHelper;

    private List<Book> bookList;

    public BookDataSource() {
        bookList = new ArrayList<>();
    }

    public void addBook(Book book) {
        bookList.add(book);
    }

    public List<Book> getBookList() {
        return bookList;
    }
}