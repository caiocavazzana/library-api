package com.example.libraryapi.service;

import com.example.libraryapi.api.dto.LoanFilterDTO;
import com.example.libraryapi.api.model.entity.Book;
import com.example.libraryapi.api.model.entity.Loan;
import com.example.libraryapi.api.model.repository.LoanRepository;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService loanService;

    @MockBean
    private LoanRepository repository;

    @BeforeEach
    public void setup() {
        loanService = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo.")
    public void saveLoanTest() {
        var customer = "Fulano";
        var book = Book.builder().id(1L).build();
        var loanToSave = Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();
        var savedLoan = Loan.builder().id(1L).book(book).customer(customer).loanDate(LocalDate.now()).build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        when(repository.save(loanToSave)).thenReturn(savedLoan);

        var loan = loanService.save(loanToSave);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar um erro de negócio ao salvar um empreśtimo com livro já emprestado.")
    public void saveLoanInvalidBookTest() {
        var book = Book.builder().id(1L).build();
        var loanToSave = createLoan();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        var exception = catchThrowable(() -> loanService.save(loanToSave));

        assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Livro já emprestado.");
        verify(repository, never()).save(loanToSave);
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo id.")
    public void getLoanDetailsTest() {
        //cenário
        var id = 1L;
        var loan = createLoan();
        loan.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        //execução
        var result = loanService.getById(id);

        //verificação
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo.")
    public void updateLoanTest() {
        var loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        var updatedLoan = loanService.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();

        verify(repository, times(1)).save(loan);
    }

    @Test
    @DisplayName("Deve buscar empréstimos pelas propriedades.")
    public void findLoanTest() {
        //cenário
        var loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
        var loan = createLoan();
        loan.setId(1L);

        var pageRequest = PageRequest.of(0, 10);
        var list = List.of(loan);
        var page = new PageImpl<>(list, pageRequest, list.size());
        when(repository.findByBookIsbnOrCustomer(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        //execução
        var result = loanService.find(loanFilterDTO, pageRequest);

        //verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan() {
        var customer = "Fulano";
        var book = Book.builder().id(1L).build();

        return Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();
    }

}
