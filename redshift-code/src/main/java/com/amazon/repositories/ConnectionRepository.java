package com.amazon.repositories;

import com.amazon.domain.Connections;
import org.springframework.data.repository.CrudRepository;


public interface ConnectionRepository extends CrudRepository<Connections, Long> {

}