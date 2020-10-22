package org.istio.jwt.service.impl;

import java.util.Optional;

import org.istio.jwt.persistence.model.Foo;
import org.istio.jwt.persistence.repository.IFooRepository;
import org.springframework.stereotype.Service;

import org.istio.jwt.service.IFooService;

@Service
public class FooServiceImpl implements IFooService {

    private IFooRepository fooRepository;

    public FooServiceImpl(IFooRepository fooRepository) {
        this.fooRepository = fooRepository;
    }

    @Override
    public Optional<Foo> findById(Long id) {
        return fooRepository.findById(id);
    }

    @Override
    public Foo save(Foo foo) {
        return fooRepository.save(foo);
    }

    @Override
    public Iterable<Foo> findAll() {
        return fooRepository.findAll();
    }
}
