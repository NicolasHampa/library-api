package com.nhamparsomia.libraryapi.api.resource;

import com.nhamparsomia.libraryapi.api.dto.BookDTO;
import com.nhamparsomia.libraryapi.api.dto.LoanDTO;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;
import com.nhamparsomia.libraryapi.service.BookService;

import com.nhamparsomia.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
@Slf4j
public class BookController {

    private final BookService service;
    private final ModelMapper modelMapper;
    private final LoanService loanService;

    public BookController(BookService service, ModelMapper modelMapper, LoanService loanService) {
        this.service = service;
        this.modelMapper = modelMapper;
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a new book")
    public BookDTO create(@RequestBody @Valid BookDTO dto) {

        Book entity = modelMapper.map(dto, Book.class);

        entity = service.save(entity);

        log.info("Book with isbn {} successfully created", entity.getIsbn());

        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Retrieve book information by id")
    public BookDTO get(@PathVariable Long id) {
        return service.getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete book information by id")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book successfully deleted")
    })
    public void delete(@PathVariable Long id) {
        Book book = service
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        service.delete(book);

        log.info("Book with isbn {} successfully deleted", book.getIsbn());
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Update book information by id")
    public BookDTO update(@PathVariable Long id, BookDTO dto) {
        Book book = service
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        book.setAuthor(dto.getAuthor());
        book.setTitle(dto.getAuthor());

        book = service.update(book);

        log.info("Book with isbn {} successfully updated", book.getIsbn());

        return modelMapper.map(book, BookDTO.class);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Retrieve page result with books that contains information related to the given parameters")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);

        List<BookDTO> bookListResult = result
                .getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(bookListResult, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Retrieve page result with loans related to the book id")
    public Page<LoanDTO> findLoansByBook(@PathVariable Long id, Pageable pageable) {
        Book book = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        List<LoanDTO> loansByBookResult = result
                .getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loansByBookResult, pageable, result.getTotalElements());
    }
}
