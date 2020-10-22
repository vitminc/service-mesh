package org.istio.web;

import org.springframework.data.repository.CrudRepository;

public interface CustomerDAO extends CrudRepository<Customer, Long> {

}
