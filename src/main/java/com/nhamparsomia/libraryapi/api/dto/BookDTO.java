package com.nhamparsomia.libraryapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
}
