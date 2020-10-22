package org.istio.jwt.service;

import java.util.Optional;

import org.istio.jwt.persistence.model.Foo;


public interface IFooService {
    Optional<Foo> findById(Long id);

    Foo save(Foo foo);
    
    Iterable<Foo> findAll();

}
