package com.psu.sweng888.gthenewapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.psu.sweng888.gthenewapp.R;
import com.psu.sweng888.gthenewapp.data.Book;
import android.net.Uri;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private List<Book> bookListFull;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
        this.bookListFull = new ArrayList<>(bookList);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        CardView cardView = (CardView) view.findViewById(R.id.book_card_view);
        cardView.setUseCompatPadding(true); // Optional: adds padding for pre-lollipop devices
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bookTitle.setText(book.getTitle() != null ? book.getTitle() : "");
        holder.bookAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "");
        holder.bookIsbn.setText(book.getIsbn() != null ? book.getIsbn() : "");
        holder.bookPublisher.setText(book.getPublisher() != null ? book.getPublisher() : "");
        if (book.getCoverImageUri() != null && !book.getCoverImageUri().isEmpty()) {
            try {
                holder.itemImage.setImageURI(Uri.parse(book.getCoverImageUri()));
            } catch (Exception e) {
                holder.itemImage.setImageResource(R.drawable.ic_book);
            }
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_book);
        }
    }

    /** Added a new method to include a new Book, and update the bookList
     * This will dynamically update the RecyclerView to incorporate the new Book*/
    public void addBook(Book book) {
        bookList.add(book);
        notifyDataSetChanged();
    }

    public void deleteBook(Book book) {
        int index = bookList.indexOf(book);
        if (index != -1) {
            bookList.remove(index);
            notifyItemRemoved(index);
        }
        // Also remove from bookListFull for search
        bookListFull.remove(book);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        bookList.clear();
        if (query == null || query.trim().isEmpty()) {
            bookList.addAll(bookListFull);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Book book : bookListFull) {
                if (book.getTitle().toLowerCase().contains(lowerQuery) ||
                    book.getAuthor().toLowerCase().contains(lowerQuery) ||
                    book.getIsbn().toLowerCase().contains(lowerQuery) ||
                    book.getPublisher().toLowerCase().contains(lowerQuery)) {
                    bookList.add(book);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Book getBookAt(int position) {
        return bookList.get(position);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder{
        private ImageView itemImage;
        private TextView bookTitle;
        private TextView bookAuthor;
        private TextView bookIsbn;
        private TextView bookPublisher;

        public BookViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookIsbn = itemView.findViewById(R.id.book_isbn);
            bookPublisher = itemView.findViewById(R.id.book_publisher);
        }
    }
}
