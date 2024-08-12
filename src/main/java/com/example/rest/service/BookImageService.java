package com.example.rest.service;

import com.example.rest.entity.BookImage;
import com.example.rest.repository.BookImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookImageService {

    @Autowired
    private BookImageRepository bookImageRepository;

    public BookImage save(BookImage bookImage){
         return  bookImageRepository.save(bookImage);
    }

    public Optional<BookImage> findById(Long image_id) {
        return  bookImageRepository.findById(image_id);
    }

    public void delete(BookImage bookImage) {
         bookImageRepository.delete(bookImage);
    }
}
