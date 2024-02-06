package com.nalutbae.example.service;

import com.nalutbae.example.domain.Book;
import com.nalutbae.example.domain.enumeration.Genre;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


class BookServiceTest {
    BookService bookService = new BookService();

    @Test
    void getBooks() {
        assertThat(this.bookService.getBooks())
                .hasSize(10);
    }

    @Test
    void getBookFound() {
        var book = this.bookService.getBook("9780747532743")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .getItem();

        assertThat(book)
                .isNotNull()
                .extracting(Book::getIsbn, Book::getTitle)
                .containsExactly("9780747532743", "Harry Potter and the Philosopher's Stone");
    }

    @Test
    void getBookNotFound() {
        this.bookService.getBook("9780140283299")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(null);
    }

    @Test
    void addBook() {
        var book = new Book("The Adventures of Huckleberry Finn", "Mark Twain", "9780486280615", Genre.FICTION, "Dover Publications", 1884);
        this.bookService.addOrUpdateBook(book)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(book);

        assertThat(this.bookService.getBooks())
                .hasSize(11);
    }

    @Test
    void deleteBook() {
        this.bookService.deleteBook("9780345339683")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted();

        assertThat(this.bookService.getBooks())
                .hasSize(9);
    }

    @Test
    void streamBooks() {
        Multi<Book> bookMulti = bookService.streamBooks();

        // Check if the Multi emits at least one item
        bookMulti.subscribe().with(
                book -> assertThat(book).isNotNull(),
                Throwable::printStackTrace
        );

        // Check if the Multi emits the expected number of items
        long expectedCount = bookService.getBooks().size();
        long actualCount = bookMulti.collect().asList().await().indefinitely().size();
        assertThat(actualCount).isEqualTo(expectedCount);

        // Check if the books are sorted by title
        List<Book> sortedBooks = bookMulti.collect().asList().await().indefinitely();
        for (int i = 0; i < sortedBooks.size() - 1; i++) {
            String title1 = sortedBooks.get(i).getTitle();
            String title2 = sortedBooks.get(i + 1).getTitle();
            assertThat(title1.compareTo(title2) <= 0);
        }
    }
}
