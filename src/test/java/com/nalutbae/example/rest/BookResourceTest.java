package com.nalutbae.example.rest;

import com.nalutbae.example.domain.Book;
import com.nalutbae.example.domain.CustomRuntimeException;
import com.nalutbae.example.domain.enumeration.Genre;
import com.nalutbae.example.service.BookService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@QuarkusTest
class BookResourceTest {
    @InjectMock
    BookService bookService;

    @Test
    void list() {
        when(this.bookService.getBooks())
                .thenReturn(List.of(
                        new Book("The Adventures of Huckleberry Finn", "Mark Twain", "9780486280615", Genre.FICTION, "Dover Publications", 1884)
                ));

        given()
                .when().get("/books")
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                        "size()", is(1),
                        "[0].title", is("The Adventures of Huckleberry Finn"),
                        "[0].author", is("Mark Twain"),
                        "[0].isbn", is("9780486280615"),
                        "[0].genre", is("FICTION"),
                        "[0].publisher", is("Dover Publications"),
                        "[0].yearPublished", is(1884)
                );

        verify(this.bookService).getBooks();
        verifyNoMoreInteractions(this.bookService);
    }

    @Test
    void getBookFound() {
        when(this.bookService.getBook("9780486280615"))
                .thenReturn(Uni.createFrom().item(new Book("The Adventures of Huckleberry Finn", "Mark Twain", "9780486280615", Genre.FICTION, "Dover Publications", 1884)));

        given()
                .when().get("/books/9780486280615")
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                        "title", is("The Adventures of Huckleberry Finn"),
                        "author", is("Mark Twain"),
                        "isbn", is("9780486280615"),
                        "genre", is("FICTION"),
                        "publisher", is("Dover Publications"),
                        "yearPublished", is(1884)
                );

        verify(this.bookService).getBook("9780486280615");
        verifyNoMoreInteractions(this.bookService);
    }

    @Test
    void getBookNotFound() {
        when(this.bookService.getBook("9780486280615"))
                .thenReturn(Uni.createFrom().nullItem());

        given()
                .when().get("/books/9780486280615")
                .then()
                .log().all()
                .statusCode(404);

        verify(this.bookService).getBook("9780486280615");
        verifyNoMoreInteractions(this.bookService);
    }

    @Test
    void addBook() {
        Book book = new Book("The Adventures of Huckleberry Finn", "Mark Twain", "9780486280615", Genre.FICTION, "Dover Publications", 1884);
        when(this.bookService.addOrUpdateBook(book))
                .thenReturn(Uni.createFrom().item(book));

        given()
                .contentType(ContentType.JSON)
                .body(book)
                .when().post("/books")
                .then()
                .log().all()
                .statusCode(201)
                .contentType(ContentType.JSON);

        verify(this.bookService).addOrUpdateBook(book);
        verifyNoMoreInteractions(this.bookService);
    }

    @Test
    void deleteBook() {
        given()
                .when().delete("/books/9780486280615")
                .then()
                .log().all()
                .statusCode(204);

        verify(this.bookService).deleteBook("9780486280615");
        verifyNoMoreInteractions(this.bookService);
    }

    @Test
    void doSomethingGeneratingError() {
        doThrow(new CustomRuntimeException("Error"))
                .when(this.bookService).performWorkGeneratingError();

        given()
                .when().get("/books/error")
                .then()
                .log().all()
                .statusCode(500)
                .header("X-CUSTOM-ERROR", "500")
                .body(
                        "errorCode", is(500),
                        "errorMessage", is("Error")
                );

        verify(this.bookService).performWorkGeneratingError();
        verifyNoMoreInteractions(this.bookService);
    }

    @Test
    void streamBooks() {
        when(this.bookService.streamBooks())
                .thenReturn(streamBooksMock());

        given()
                .when().get("/books/stream")
                .then()
                .log().all()
                .statusCode(200)
                .contentType("text/event-stream")
                .body(is("data:{\"title\":\"The Adventures of Huckleberry Finn\",\"author\":\"Mark Twain\",\"isbn\":\"9780486280615\",\"genre\":\"FICTION\",\"publisher\":\"Dover Publications\",\"yearPublished\":1884}\n\ndata:{\"title\":\"The Adventures of Tom Sawyer\",\"author\":\"Mark Twain\",\"isbn\":\"9780486280615\",\"genre\":\"FICTION\",\"publisher\":\"Dover Publications\",\"yearPublished\":1884}\n\n"));
    }

    private Multi<Book> streamBooksMock() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofSeconds(1))
                .onItem().transform(tick ->
                        Stream.of(
                                        new Book("The Adventures of Huckleberry Finn", "Mark Twain", "9780486280615", Genre.FICTION, "Dover Publications", 1884),
                                        new Book("The Adventures of Tom Sawyer", "Mark Twain", "9780486280615", Genre.FICTION, "Dover Publications", 1884)
                                )
                                .sorted(Comparator.comparing(Book::getTitle))
                                .collect(Collectors.toList())
                                .get(tick.intValue())
                )
                .select().first(2);
    }

}