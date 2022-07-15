package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.api.dto.LoanDTO;
import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book API")
@Slf4j
public class BookController {

    @Autowired
    private BookService service;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "CREATE A BOOK")
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info("CREATING A BOOK FOR ISBN: {}", dto.getIsbn());

        Book entity = modelMapper.map(dto, Book.class);

        entity = service.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @Operation(description = "OBTAIN A BOOK DETAILS BY ID")
    public BookDTO get(@PathVariable Long id) {
        log.info("OBTAINING DETAILS FOR BOOK ID: {}", id);

        return service.getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @Operation(description = "DELETE A BOOK BY ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book succesfully deleted.")
    })
    public void delete(@PathVariable Long id) {
        log.info("DELETING BOOK OF ID: {}", id);

        var book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    @Operation(description = "UPDATE A BOOK")
    public BookDTO update(@PathVariable Long id, BookDTO dto) {
        log.info("UPDATING BOOK OF ID: {}", id);

        var book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        book.setAuthor(dto.getAuthor());
        book.setTitle(dto.getTitle());
        book = service.update(book);

        return modelMapper.map(book, BookDTO.class);
    }

    @GetMapping
    @Operation(description = "FIND BOOK BY PARAMS")
    public Page<BookDTO> find(BookDTO dto, Pageable pageable) {
        var filter = modelMapper.map(dto, Book.class);

        var result = service.find(filter, pageable);

        var list = result
                .getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        var book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var result = loanService.getLoansByBook(book, pageable);

        var list = result.getContent()
                .stream()
                .map(loan -> {
                    var loanBook = loan.getBook();
                    var bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    var loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);

                    return loanDTO;})
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, result.getTotalElements());
    }

}
