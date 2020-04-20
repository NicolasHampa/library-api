package com.nhamparsomia.libraryapi.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Book {
    private Long id;
    private String title;
    private String author;
    private String isbn;
}
