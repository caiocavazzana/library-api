package com.example.libraryapi.api.exception;

import com.example.libraryapi.exception.BusinessException;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ApiErrors {

    private final List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException exception) {
        this.errors = Collections.singletonList(exception.getMessage());
    }

    public ApiErrors(ResponseStatusException exception) {
        this.errors = Collections.singletonList(exception.getReason());
    }
}
