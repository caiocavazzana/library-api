package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.LoanDTO;
import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.api.model.entity.Loan;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {LoanController.class})
@AutoConfigureMockMvc
public class LoanControllerTest {

    private static final String LOAN_API = "/api/loans";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empréstimo.")
    public void createLoanTest() throws Exception {
        var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        var json = new ObjectMapper().writeValueAsString(dto);
        var book = Book.builder().id(1L).isbn("123").build();

        given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        var loan = Loan.builder().id(1L).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
        given(loanService.save(any(Loan.class))).willReturn(loan);

        var request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empreśtimo de um livro inexistente.")
    public void createLoanWithInvalidIsbnTest() throws Exception {
        var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        var json = new ObjectMapper().writeValueAsString(dto);

        given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        var request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors").value(hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro não encontrado para o isbn informado."));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empreśtimo de um livro emprestado.")
    public void createLoanWithLoanedBookTest() throws Exception {
        var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        var json = new ObjectMapper().writeValueAsString(dto);
        var book = Book.builder().id(1L).isbn("123").build();

        given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
        given(loanService.save(any(Loan.class))).willThrow(new BusinessException("Livro já emprestado."));

        var request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors").value(hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro já emprestado."));
    }

}
