package com.nhamparsomia.libraryapi.api.resource;

import com.nhamparsomia.libraryapi.api.dto.BookDTO;
import com.nhamparsomia.libraryapi.api.dto.LoanDTO;
import com.nhamparsomia.libraryapi.api.dto.LoanFilterDTO;
import com.nhamparsomia.libraryapi.api.dto.ReturnedLoanDTO;
import com.nhamparsomia.libraryapi.model.entity.Book;
import com.nhamparsomia.libraryapi.model.entity.Loan;
import com.nhamparsomia.libraryapi.service.BookService;
import com.nhamparsomia.libraryapi.service.LoanService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Api("Loan API")
@Slf4j
public class LoanController {

    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a new loan")
    public Long create(@RequestBody LoanDTO dto) {
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Book not found for given isbn")
                );

        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);

        log.info("Loan for book with isbn {} successfully created at {}",
                entity.getBook().getIsbn(),
                LocalDate.now());

        return entity.getId();
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Returns back to library the book borrowed by customer")
    public void giveBackTheBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        loan.setReturned(dto.getReturned());

        service.update(loan);

        log.info("Book with isbn {} returned back to library at {}. Loan code: {}",
                loan.getBook().getIsbn(),
                LocalDate.now(),
                loan.getId());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Retrieve page result with loans that contains information related to the given parameters")
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
        Page<Loan> result = service.find(dto, pageRequest);

        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map(entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                })
                .collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }
}
