package com.example.libraryapi.service;


import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.api.model.repository.BookRepository;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    private BookService service;

    @MockBean
    private BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        //cenário
        var book = createValidBook();
        when(repository.existsByIsbn(anyString())).thenReturn(false);
        when(repository.save(book)).thenReturn(Book
                .builder()
                .id(1L)
                .isbn("123")
                .title("As Aventuras")
                .author("Fulano")
                .build());

        //execução
        var savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As Aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado.")
    public void shouldNotSaveABookWithDuplicatedIsbn() {
        //cenário
        var book = createValidBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        //execução
        var exception = catchThrowable(() -> service.save(book));

        //verificações
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");

        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void getByIdTest() {
        //cenário
        var id = 1L;
        var book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //execução
        var foundBook = service.getById(id);

        //verificação
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base.")
    public void bookNotFoundByIdTest() {
        //cenário
        var id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        //execução
        var foundBook = service.getById(id);

        //verificação
        assertThat(foundBook.isPresent()).isFalse();
    }

//    COMO EU IMPLEMENTEI
//    @Test
//    @DisplayName("Deve excluir um livro cadastrado no banco.")
//    public void deleteBookTest() {
//        //cenário
//        var id = 1L;
//        var book = createValidBook();
//        book.setId(id);
//
//        //execução e verificação
//        assertDoesNotThrow(() -> service.delete(book));
//    }

//    COMO FOI IMPLEMENTADO NO CURSO
    @Test
    @DisplayName("Deve excluir um livro cadastrado no banco.")
    public void deleteBookTest() {
        //cenário
        var book = Book.builder().id(1L).build();

        //execução
        assertDoesNotThrow(() -> service.delete(book));

        //verificação
        verify(repository, times(1)).delete(book);
    }

//    COMO EU IMPLEMENTEI
//    @Test
//    @DisplayName("Deve retornar IllegalArgumentException ao tentar excluir um livro com id nulo.")
//    public void deleteInvalidBookTest() {
//        //cenário
//        var book = createValidBook();
//
//        //execução
//        var exception = assertThrows(IllegalArgumentException.class, () -> service.delete(book));
//
//        //verificação
//        assertThat(exception.getMessage()).isEqualTo("Id do livro não pode ser nulo.");
//    }

//    COMO FOI IMPLEMENTADO NO CURSO
    @Test
    @DisplayName("Deve retornar IllegalArgumentException ao tentar excluir um livro com id nulo.")
    public void deleteInvalidBookTest() {
        //cenário
        var book = new Book();

        //execução
        assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        //verificação
        verify(repository, never()).delete(book);
    }

//    COMO EU IMPLEMENTEI
//    @Test
//    @DisplayName("Deve atualizar um livro cadastrado no banco.")
//    public void updateBookTest() {
//        //cenário
//        var id = 1L;
//        var book = createValidBook();
//        book.setId(id);
//        when(service.update(book)).thenReturn(book);
//
//        //execução
//        var updatedBook = service.update(book);
//
//        //verificação
//        assertThat(updatedBook.getId()).isEqualTo(id);
//        assertThat(updatedBook.getTitle()).isEqualTo(book.getTitle());
//        assertThat(updatedBook.getAuthor()).isEqualTo(book.getAuthor());
//        assertThat(updatedBook.getIsbn()).isEqualTo(book.getIsbn());
//    }

//    COMO FOI IMPLEMENTADO NO CURSO
    @Test
    @DisplayName("Deve atualizar um livro cadastrado no banco.")
    public void updateBookTest() {
        //cenário
        var id = 1L;
        var bookToUpdate = Book.builder().id(id).build();
        var updatedBook = createValidBook();
        updatedBook.setId(id);
        when(service.update(bookToUpdate)).thenReturn(updatedBook);

        //execução
        var book = service.update(bookToUpdate);

        //verificação
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
    }

//    COMO EU IMPLEMENTEI
//    @Test
//    @DisplayName("Deve retornar IllegalArgumentException ao tentar atualizar um livro com id nulo.")
//    public void updateInvalidBookTest() {
//        //cenário
//        var book = createValidBook();
//
//        //execução
//        var exception = assertThrows(IllegalArgumentException.class, () -> service.update(book));
//
//        //verificação
//        assertThat(exception.getMessage()).isEqualTo("Id do livro não pode ser nulo.");
//    }

//    COMO FOI IMPLEMENTADO NO CURSO
    @Test
    @DisplayName("Deve retornar IllegalArgumentException ao tentar atualizar um livro com id nulo.")
    public void updateInvalidBookTest() {
        //cenário
        var book = new Book();

        //execução
        assertThrows(IllegalArgumentException.class, () -> service.update(book));

        //verificação
        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Deve buscar livros pelas propriedades.")
    public void findBookTest() {
        //cenário
        var book = createValidBook();
        var pageRequest = PageRequest.of(0, 10);
        var list = List.of(book);
        var page = new PageImpl<>(list, pageRequest, 1);
        when(repository.findAll(any(), any(PageRequest.class))).thenReturn(page);

        //execução
        var result = service.find(book, pageRequest);

        //verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As Aventuras").build();
    }

}
