package com.example.rest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookImage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private int type;
    private String originalFileName;
    private String fileName;

    // 책(1) : 이미지(N)
    @ManyToOne
    // FK
    @JoinColumn(name="book_id", referencedColumnName = "id", nullable = false)
    private Book book; // book pk id
}
