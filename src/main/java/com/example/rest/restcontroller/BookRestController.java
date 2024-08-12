package com.example.rest.restcontroller;

import com.example.rest.entity.Book;
import com.example.rest.entity.BookImage;
import com.example.rest.entity.BookPayloadDTO;
import com.example.rest.entity.BookViewDTO;
import com.example.rest.service.BookImageService;
import com.example.rest.service.BookService;
import com.example.rest.util.ImageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Book Controller", description = "책을 관리하는 컨트롤러")
public class BookRestController {
    // REST API -> GET : http:/localhost:8081/api/test
   /*  @GetMapping("/test")
     @Tag(name = "User API")
     @Operation(summary = "User 조회", description = "User 정보를 조회합니다.")
    public String test(){
         return "Hello World"; // --->응답(JSON)
     }*/
    @Autowired
    private BookService bookService;

    @Autowired
    private BookImageService bookImageService;

    @Value("${upload.path}")
    private String uploadPath;
    
    // POST : http://localhost:8081/api/books
    @PostMapping(value = "/books", consumes = "application/json", produces = "application/json")
    public ResponseEntity<BookViewDTO> addBook(@Valid @RequestBody BookPayloadDTO bookPayload){
         try{
             Book book=new Book();
             book.setSubject(bookPayload.getSubject());
             book.setPrice(bookPayload.getPrice());
             book.setAuthor(bookPayload.getAuthor());
             book.setPage(bookPayload.getPage());
             book=bookService.save(book);

             BookViewDTO bookViewDTO=new BookViewDTO(book.getId(),book.getSubject(),
                     book.getPrice(),book.getAuthor(),book.getPage(),book.getCreatedAt() );

             return ResponseEntity.ok(bookViewDTO); // (200:OK + JSON Data) : ResponseEntity
         }catch(Exception e){
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
         }
    }
    // 전체 목록 보여주기
    @GetMapping(value = "/books", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of books")
    @Operation(summary="List book API")
    public List<BookViewDTO> books(){
        List<BookViewDTO> books=new ArrayList<>();
        for(Book book  : bookService.findAll()){
            books.add(new BookViewDTO(book.getId(), book.getSubject(), book.getPrice(),
                    book.getAuthor(),book.getPage(), book.getCreatedAt()));
        }
        return books; // JSON Array, 200(OK)
    }

    @GetMapping(value = "/books/{id}", produces = "application/json")
    @ApiResponse(responseCode = "200", description ="id에 해당하는 책정보를 출력")
    @Operation(summary="book id API")
    public ResponseEntity<?> findById(@PathVariable Long id){
         Optional<Book> optionalBook=bookService.findById(id);
         Book book;
         if(optionalBook.isPresent()){
             book=optionalBook.get();
         }else{
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
         }
         BookViewDTO bookViewDTO=new BookViewDTO(book.getId(), book.getSubject(), book.getPrice(),
                 book.getAuthor(),book.getPage(), book.getCreatedAt());
         return ResponseEntity.ok(bookViewDTO);// 200(OK) + bookViewDTO(JSON)
    }

    // 수정하기
    @PutMapping(value = "/books/{id}" ,consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name a description")
    @ApiResponse(responseCode = "204", description = "Book update")
    @Operation(summary = "Update an Book")
    public ResponseEntity<?> update_Book(@Valid @RequestBody BookPayloadDTO payloadDTO,
                                                                         @PathVariable Long id){
          Optional<Book> optionalBook=bookService.findById(id);
          Book book;
          if(optionalBook.isPresent()){
              book=optionalBook.get();
          }else{
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
          }
        // 수정되는 데이터 교체
        book.setSubject(payloadDTO.getSubject()); // 제목
        book.setPrice(payloadDTO.getPrice()); // 가격
        book.setAuthor(payloadDTO.getAuthor()); // 저자
        book.setPage(payloadDTO.getPage()); // 페이지

        book=bookService.save(book);

        BookViewDTO bookViewDTO=new BookViewDTO(book.getId(), book.getSubject(), book.getPrice(),
                book.getAuthor(),book.getPage(), book.getCreatedAt());
        return ResponseEntity.ok(bookViewDTO);
    }

    // 삭제하기
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> delete_Book(@PathVariable Long id){
          Optional<Book> optionalBook=bookService.findById(id);
          Book book;
          if(optionalBook.isPresent()){
              book=optionalBook.get();
          }else{
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
          }
          bookService.delete(book);
          ImageUtil.deleteFolder(uploadPath, id); // 데렉토리 삭제
         return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
    // 파일(이미지) 업로드 REST API  
    // http://localhost:8081/1/1/upload/
    @PostMapping(value = "/{book_id}/{type}/upload", consumes ={"multipart/form-data"} )
    public ResponseEntity<?> images_upload(@RequestPart(required = true) MultipartFile[] files,
            @PathVariable Long book_id,  @PathVariable int type){
           Optional<Book> optionalBook=bookService.findById(book_id);
           Book book;
           if(optionalBook.isPresent()){
                book=optionalBook.get();
           }else{
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
           }

           List<String> successImageName=new ArrayList<>();
           List<String> errorImageName=new ArrayList<>();
          /* int length=10;
           boolean useLetters=true;
           boolean useNumbers=true;*/
           // MultipartFile[] files 여기에서 파일 하나씩을 가져와서 작업을 해야한다.
          //                                                       MultipartFile : png, jpg, jpeg
           Arrays.asList(files).stream().forEach(file->{
               String contentType=file.getContentType();
               if(contentType.equals("image/png")
                   || contentType.equals("image/jpg")
                   || contentType.equals("image/jpeg")){
                   // 정상적인 이미지인  경우 정보를 저장(List<String>)
                   successImageName.add(file.getOriginalFilename());
                  try{
                      String fileName=file.getOriginalFilename();
                      String generatedString= RandomStringUtils.random(10, true, true);
                      // 새로운 이미지 이름을 만든다.
                      String new_image_name = generatedString + fileName;
                      if(type==1){
                          new_image_name = "thumb_"+generatedString + fileName;
                      }
                      String absolute_fileLocation=ImageUtil.makePath(uploadPath, new_image_name, book_id);
                      System.out.println(absolute_fileLocation);
                      Path path= Paths.get(absolute_fileLocation);
                      if(type!=1){ // type = 2, 3
                          Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                      }
                      // 데이터베이스에 이미지 정보(BookImage)를 저장...
                      BookImage bookImage=new BookImage();
                      bookImage.setOriginalFileName(fileName);
                      bookImage.setFileName(new_image_name);
                      bookImage.setBook(book); // 관계
                      bookImage.setType(type);
                      bookImageService.save(bookImage);
                      // 썸네일 이미지 인경우
                      if(type==1){
                           BufferedImage thumbnail =ImageUtil.getThumbnail(file, 300);
                           String thumbnail_location=ImageUtil.makePath(uploadPath, new_image_name, book_id);
                           //Files.copy(thumbnail.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                          //                                                 image/png
                          ImageIO.write(thumbnail, file.getContentType().split("/")[1], new File(thumbnail_location));
                      }
                  }catch (Exception e){
                      e.printStackTrace();
                      // 이미지가 아닌 경우 정보를 저장((List<String>)
                      errorImageName.add(file.getOriginalFilename());
                  }
               }else{
                    // 이미지가 아닌 경우 정보를 저장(List<String>
                   errorImageName.add(file.getOriginalFilename());
               }
           });
           HashMap<String, List<String>> result=new HashMap<>();
           result.put("SUCCESS", successImageName);
           result.put("ERRORS", errorImageName);

           List<HashMap<String, List<String>>> response=new ArrayList<>();
           response.add(result);
        return ResponseEntity.ok(response);
    }
    // 이미지 뷰어 만들기
    @GetMapping("/{image_id}/imageSrc")
    public ResponseEntity<byte[]> getImage(@PathVariable("image_id") Long image_id) throws IOException {
        Optional<BookImage> optionalBook= bookImageService.findById(image_id);
        byte[] imageBytes;
        if(optionalBook.isPresent()){
            BookImage bookImage = optionalBook.get();
            Path imagePath = ImageUtil.getFileAsResource(uploadPath, bookImage.getBook().getId(), bookImage.getFileName());
            imageBytes = Files.readAllBytes(imagePath);

            // Determine the content type based on the file extension
            String fileName = bookImage.getFileName();
            String fileExtension = "";

            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
            }

            MediaType mediaType;
            switch (fileExtension) {
                case "png":
                    mediaType = MediaType.IMAGE_PNG;
                    break;
                case "jpg":
                case "jpeg":
                    mediaType = MediaType.IMAGE_JPEG;
                    break;
                case "gif":
                    mediaType = MediaType.IMAGE_GIF;
                    break;
                default:
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    // 이미지 삭제 하기
    @DeleteMapping(value = "/{image_id}/delete")
    public ResponseEntity<String> delete_photo(@PathVariable Long image_id) {
        try {
            Optional<BookImage> optionalBookImage = bookImageService.findById(image_id);
            if(optionalBookImage.isPresent()){
                BookImage bookImage = optionalBookImage.get();
                // 디렉토리에서 이미지 삭제
                ImageUtil.deleteImage(uploadPath, bookImage.getBook().getId(), bookImage.getFileName());
                // 테이불에서 삭제
                bookImageService.delete(bookImage);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
