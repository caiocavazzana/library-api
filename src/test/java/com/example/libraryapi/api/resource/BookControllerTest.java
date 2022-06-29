package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {BookController.class})
@AutoConfigureMockMvc
public class BookControllerTest {

    private static final String BOOK_API = "/api/books";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService service;

    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {
        BookDTO dto = createNewBook();

        Book savedBook = Book.builder()
                .id(10L)
                .author("Artur")
                .title("As Aventuras")
                .isbn("001")
                .build();

        given(service.save(any(Book.class)))
                .willReturn(savedBook);

        var json = new ObjectMapper().writeValueAsString(dto);

        var request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro.")
    public void createInvalidBookTest() throws Exception {
        var json = new ObjectMapper().writeValueAsString(new BookDTO());

        var request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com ISBN já utilizado por outro.")
    public void createBookWithDuplicatedIsbn() throws Exception {
        var dto = createNewBook();
        var json = new ObjectMapper().writeValueAsString(dto);
        var mensagemErro = "ISBN já cadastrado";

        given(service.save(any(Book.class))).willThrow(new BusinessException(mensagemErro));

        var request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve obter informações de um livro.")
    public void getBookDetailsTest() throws Exception {
        //cenário (given)
        var id = 1L;
        var modelMapper = new ModelMapper();
        var book = modelMapper.map(createNewBook(), Book.class);
        book.setId(id);;
        given(service.getById(id)).willReturn(Optional.of(book));

        //execução (when)
        var request = MockMvcRequestBuilders
                .get(BOOK_API + "/" + id)
                .accept(MediaType.APPLICATION_JSON);

        //verificação (then)
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(book.getId()))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir.")
    public void bookNotFoundTest() throws Exception {
        //cenário
        given(service.getById(anyLong())).willReturn(Optional.empty());

        //execução
        var request = MockMvcRequestBuilders
                .get(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON);

        //verificação
        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() throws Exception {
        //cenário
        given(service.getById(anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        //execução
        var request = MockMvcRequestBuilders
                .delete(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON);

        //verificação
        mvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar.")
    public void deleteInexistentBookTest() throws Exception {
        //cenário
        given(service.getById(anyLong())).willReturn(Optional.empty());

        //execução
        var request = MockMvcRequestBuilders
                .delete(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON);

        //verificação
        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest() throws Exception {
        //cenário
        var id = 1L;
        var bookToUpdate = Book.builder().id(1L).title("Some Title").author("Some Author").isbn("321").build();
        var updatedBook = createNewBook();
        var json = new ObjectMapper().writeValueAsString(updatedBook);
        given(service.getById(id)).willReturn(Optional.of(bookToUpdate));
        given(service.update(bookToUpdate)).willReturn(modelMapper.map(updatedBook, Book.class));

        //execução
        var request = MockMvcRequestBuilders
                .put(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //verificação
        mvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("id").value(updatedBook.getId()))
                .andExpect(jsonPath("title").value(updatedBook.getTitle()))
                .andExpect(jsonPath("author").value(updatedBook.getAuthor()))
                .andExpect(jsonPath("isbn").value(updatedBook.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente.")
    public void updateInexistentBookTest() throws Exception {
        //cenário
        var id = 1L;
        var json = new ObjectMapper().writeValueAsString(createNewBook());
        given(service.getById(id)).willReturn(Optional.empty());

        //execução
        var request = MockMvcRequestBuilders
                .put(BOOK_API + "/" + 1)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //verificação
        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve buscar por livros")
    public void findBooksTest() throws Exception {
        var id = 1L;
        var book = modelMapper.map(createNewBook(), Book.class);
        book.setId(id);

        given(service.find(any(), any()))
                .willReturn(new PageImpl<>(List.of(book), PageRequest.of(0, 100), 1));

        var queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        var request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    private BookDTO createNewBook() {
        return BookDTO.builder().author("Artur").title("As Aventuras").isbn("001").build();
    }

}
