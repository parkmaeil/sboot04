package com.example.rest.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private int price;
    private String author;
    private int page;
    private LocalDateTime createdAt;
    // 책(1) : 이미지(N)
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookImage> bookImages; // 데이블의 컬럼으로 만들면 않된다.

    @PrePersist
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
    }
}
