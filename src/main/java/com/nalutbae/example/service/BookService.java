package com.nalutbae.example.service;

import com.nalutbae.example.domain.Book;
import com.nalutbae.example.domain.CustomRuntimeException;
import com.nalutbae.example.domain.enumeration.Genre;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class BookService {
    private final ConcurrentMap<String, Book> books = new ConcurrentHashMap<>();

    public BookService() {
        books.put("9780747532743", new Book("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "9780747532743", Genre.FANTASY, "Bloomsbury Publishing", 1997));
        books.put("9780061120084", new Book("To Kill a Mockingbird", "Harper Lee", "9780061120084", Genre.FICTION, "Harper Perennial Modern Classics", 1960));
        books.put("9780451524935", new Book("1984", "George Orwell", "9780451524935", Genre.SCIENCE_FICTION, "Signet Classics", 1949));
        books.put("9780140283297", new Book("Pride and Prejudice", "Jane Austen", "9780140283297", Genre.ROMANCE, "Penguin Classics", 1813));
        books.put("9780316769488", new Book("The Catcher in the Rye", "J.D. Salinger", "9780316769488", Genre.FICTION, "Little, Brown and Company", 1951));
        books.put("9780590353427", new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "9780590353427", Genre.FANTASY, "Arthur A. Levine Books", 1997));
        books.put("9780345339683", new Book("The Hobbit", "J.R.R. Tolkien", "9780345339683", Genre.FANTASY, "Houghton Mifflin Harcourt", 1937));
        books.put("9780307887449", new Book("The Lord of the Rings", "J.R.R. Tolkien", "9780307887449", Genre.FANTASY, "Delacorte Press", 2003));
        books.put("9780307277671", new Book("The Da Vinci Code", "Dan Brown", "9780307277671", Genre.MYSTERY, "Anchor Books", 2008));
        books.put("9780439023528", new Book("The Hunger Games", "Suzanne Collins", "9780439023528", Genre.SCIENCE_FICTION, "Scholastic Press", 2008));
    }

    public Collection<Book> getBooks() {
        return this.books.values();
    }

    public Uni<Book> getBook(String bookId) {
        return Uni.createFrom().item(this.books.get(bookId));
    }

    public Uni<Book> addOrUpdateBook(Book book) {
        this.books.put(book.getIsbn(), book);
        return Uni.createFrom().item(book);
    }

    public Uni<Void> deleteBook(String bookId) {
        this.books.remove(bookId);
        return Uni.createFrom().voidItem(); // or Uni.createFrom().nullItem() if you don't want to return anything.
    }

    public Uni<Book> performWorkGeneratingError() {
        throw new CustomRuntimeException("Got some kind of error from somewhere");
    }

    public Multi<Book> streamBooks() {
        return Multi.createFrom()
            .ticks()
            .every(Duration.ofSeconds(1))
            .map(tick ->
                this.books.values()
                    .stream()
                    .sorted(Comparator.comparing(Book::getTitle))
                    .toList()
                    .get(tick.intValue())
            )
            .select().first(this.books.size()); // or .last() if you want the last item.
    }

}
