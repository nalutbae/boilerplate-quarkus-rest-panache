package com.nalutbae.example.rest;

import com.nalutbae.example.domain.Book;
import com.nalutbae.example.service.BookService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Collection;

@Path("/books")
@Tag(name = "Book Resource", description = "Book API")
public class BookResource {
    private final BookService bookService;

    public BookResource(BookService bookService) {
        this.bookService = bookService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all books", description = "Get all books")
    @APIResponse(responseCode = "200", description = "All books")
    @APIResponse(responseCode = "404", description = "Books are not found")
    public Collection<Book> list() {
        return this.bookService.getBooks();
    }

    @GET
    @Path("/{isbn}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a book by isbn", description = "Get a book by isbn")
    @APIResponse(responseCode = "200", description = "Book by isbn", content = @Content(schema = @Schema(implementation = Book.class)))
    @APIResponse(responseCode = "404", description = "Book is not found")
    public Uni<Response> getBook(@Parameter(required = true, description = "Book ISBN") @PathParam("isbn") String isbn) {
        return this.bookService.getBook(isbn)
                .onItem().ifNotNull().transform(book -> Response.ok(book).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a new book", description = "Add a new book")
    @APIResponse(responseCode = "201", description = "Book added")
    @APIResponse(responseCode = "400", description = "Book is invalid")
    public Uni<Response> addBook(@Parameter(required = true, description = "Book to add") @NotNull @Valid Book book) {
        return this.bookService.addOrUpdateBook(book)
                .onItem().transform(item -> Response.status(Response.Status.CREATED)
                        .entity(item)
                        .type(MediaType.APPLICATION_JSON)
                        .build());
    }

    @PATCH
    @Path("/{isbn}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a book", description = "Update a book")
    @APIResponse(responseCode = "200", description = "Book updated")
    @APIResponse(responseCode = "400", description = "Book is invalid")
    public Uni<Book> updateBook(@Parameter(required = true, description = "Book to update") @NotNull @Valid Book book) {
        return this.bookService.addOrUpdateBook(book);
    }

    @DELETE
    @Path("/{isbn}")
    @Operation(summary = "Delete a book", description = "Delete a book")
    @APIResponse(responseCode = "204", description = "Book deleted")
    public void deleteBook(@Parameter(required = true, description = "Book ISBN") @PathParam("isbn") String isbn) {
        this.bookService.deleteBook(isbn);
    }

    @GET
    @Path("/error")
    @Operation(summary = "Do something that will most likely generate an error", description = "Do something that will most likely generate an error")
    @APIResponse(responseCode = "204", description = "Success")
    @APIResponse(responseCode = "500", description = "Something bad happened")
    public void doSomethingGeneratingError() {
        this.bookService.performWorkGeneratingError();
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Operation(summary = "Stream a book every second", description = "Stream a book every second")
    @APIResponse(responseCode = "200", description = "One book every second")
    public Multi<Book> streamBooks() {
        return this.bookService.streamBooks(); // or this.bookService.streamBooks().onItem().delayIt(1000); to stream every second.
    }
}
