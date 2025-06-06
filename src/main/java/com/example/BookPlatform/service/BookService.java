package com.example.BookPlatform.service;


import com.example.BookPlatform.dto.request.BookIdDto;
import com.example.BookPlatform.dto.request.SaveBookInfoDto;
import com.example.BookPlatform.dto.request.UpdateBookDto;
import com.example.BookPlatform.dto.response.BookInfoDto;
import com.example.BookPlatform.dto.response.BookListDto;
import com.example.BookPlatform.entity.Book;
import com.example.BookPlatform.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final ImageService imageService;
    public List<BookListDto> getBookList(){
        List<Book> bookList = bookRepository.findAll();
        List<BookListDto> bookInfoDtoList = new ArrayList<>();
        for (Book book: bookList){
            BookListDto bookInfoDto = BookListDto.builder().
                    id(book.getId()).
                    title(book.getTitle()).
                    coverUrl(book.getCoverUrl()).
                    createdAt(book.getCreatedAt()).
                    views(book.getViews()).
                    author(book.getAuthor()).
                    build();
            bookInfoDtoList.add(bookInfoDto);
        }
        return bookInfoDtoList;
    }
    public BookInfoDto getBookDetailInfo(Long id){
        Optional<Book> book = bookRepository.findById(id);
        book.get().addViews(); //조회수 증가
        BookInfoDto bookInfoDto = BookInfoDto.builder().
                title(book.get().getTitle()).
                content(book.get().getContent()).
                author(book.get().getAuthor()).
                coverUrl(book.get().getCoverUrl()).
                createdAt(book.get().getCreatedAt()).
                updatedAt(book.get().getUpdatedAt()).
                author(book.get().getAuthor()).
                views(book.get().getViews()).
                build();
        return bookInfoDto;
    }
    public List<BookListDto> getBooksByAuthorName(String author){
        List<Book> bookList = bookRepository.findByAuthor(author);
        List<BookListDto> bookInfoDtoList = new ArrayList<>();
        for (Book book: bookList){
            BookListDto bookInfoDto = BookListDto.builder().
                    id(book.getId()).
                    title(book.getTitle()).
                    coverUrl(book.getCoverUrl()).
                    createdAt(book.getCreatedAt()).
                    views((book.getViews())).
                    author(book.getAuthor()).
                    build();
            bookInfoDtoList.add(bookInfoDto);
        }
        return bookInfoDtoList;
    }
    @Async
    public void registBook(SaveBookInfoDto saveBookInfoDto){
        String prompt =  "The title of the book is " +saveBookInfoDto.getTitle()+"and the content of the book is "
                +saveBookInfoDto.getContent()+ ". Please generate a cover image for this book.";

        Book book = Book.builder().
                title(saveBookInfoDto.getTitle()).
                content(saveBookInfoDto.getContent()).
                author(saveBookInfoDto.getAuthor()).
                coverUrl(imageService.generateImage(prompt)).
                createdAt(LocalDateTime.now()).
                updatedAt(LocalDateTime.now()).
                views(0).
                build();
        bookRepository.save(book);
    }

    public void  deleteBook(BookIdDto bookIdDto) {
        bookRepository.deleteById(bookIdDto.getId());
    }

    @Async
    public void updateBook(UpdateBookDto updateBookDto){
        Optional<Book> optionalBook = bookRepository.findById(updateBookDto.getId());

        Book existingBook = optionalBook.orElseThrow(() -> new RuntimeException("Book not found"));

        // *************************************************************************
        // AI 이미지 재생성 여부 확인
        String coverUrl = existingBook.getCoverUrl(); // 기본값: 기존 이미지

        // 체크박스가 true일 때만 이미지 재생성
        if (updateBookDto.isGenerateImage()) {
            String prompt = "The title of the book is " + updateBookDto.getTitle() +
                    " and the content of the book is " + updateBookDto.getContent() +
                    ". Please generate a cover image for this book.";
            coverUrl = imageService.generateImage(prompt);
        }
        // *************************************************************************

        Book updatedBook = Book.builder()
                .id(updateBookDto.getId())
                .title(updateBookDto.getTitle())
                .content(updateBookDto.getContent())
                .author(updateBookDto.getAuthor())
                .coverUrl(coverUrl) // ✅ 조건에 따라 새 이미지 or 기존 이미지
                .createdAt(existingBook.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .views(existingBook.getViews())
                .build();

        bookRepository.save(updatedBook);
    }
}
