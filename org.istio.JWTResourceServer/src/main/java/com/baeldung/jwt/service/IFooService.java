package com.baeldung.jwt.service;

import java.util.Optional;

import com.baeldung.jwt.persistence.model.Foo;


public interface IFooService {
    Optional<Foo> findById(Long id);

    Foo save(Foo foo);
    
    Iterable<Foo> findAll();

}
