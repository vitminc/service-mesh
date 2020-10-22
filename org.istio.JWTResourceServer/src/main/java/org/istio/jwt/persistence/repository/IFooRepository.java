package org.istio.jwt.persistence.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import org.istio.jwt.persistence.model.Foo;

public interface IFooRepository extends PagingAndSortingRepository<Foo, Long> {
}
