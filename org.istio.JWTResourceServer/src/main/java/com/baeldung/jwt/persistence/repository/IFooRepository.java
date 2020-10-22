package com.baeldung.jwt.persistence.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.baeldung.jwt.persistence.model.Foo;

public interface IFooRepository extends PagingAndSortingRepository<Foo, Long> {
}
