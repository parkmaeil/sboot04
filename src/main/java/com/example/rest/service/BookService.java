package com.example.rest.service;

import com.example.rest.entity.Book;
import com.example.rest.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // 저장, 수정
    public Book save(Book book){
        return bookRepository.save(book);
    }
    // 전체리스트 가져오기
    public List<Book> findAll(){
         return bookRepository.findAll();
    }
    // 특정 레코드 한개 가져오기
    public Optional<Book> findById(Long id){
        return bookRepository.findById(id);
    }
    // 삭제
    public void delete(Book book){
         bookRepository.delete(book);
    }
}
