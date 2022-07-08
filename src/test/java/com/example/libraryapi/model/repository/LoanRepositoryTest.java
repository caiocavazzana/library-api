package com.example.libraryapi.model.repository;

import com.example.libraryapi.api.model.entity.Loan;
import com.example.libraryapi.api.model.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.example.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
    public void existsByBookAndNotReturnedTest() {
        //cenário
        var loan = createAndPersistLoan(LocalDate.now());

        //execução
        var exists = loanRepository.existsByBookAndNotReturned(loan.getBook());

        //verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstmo pelo isbn do livro ou customer do empréstimo.")
    public void findByBookIsbnOrCustomerTest() {
        var loan = createAndPersistLoan(LocalDate.now());

        var result = loanRepository.findByBookIsbnOrCustomer(
                "123", "Fulano", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve obter empréstimos cuja data de empréstimo seja menor ou igual a 3 dias atrás e não foram retornados.")
    public void findByLoanDateLessThanAndNotReturnedTest() {
        var loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        var result = loanRepository
                .findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Deve retornar vazio quando não houverem empréstimos atrasados.")
    public void notFindByLoanDateLessThanAndNotReturnedTest() {
        var loan = createAndPersistLoan(LocalDate.now());

        var result = loanRepository
                .findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).isEmpty();
    }

    public Loan createAndPersistLoan(LocalDate localDate) {
        var book = createNewBook("123");
        entityManager.persist(book);

        var loan = Loan.builder().book(book).customer("Fulano").loanDate(localDate).build();
        entityManager.persist(loan);

        return loan;
    }
}
