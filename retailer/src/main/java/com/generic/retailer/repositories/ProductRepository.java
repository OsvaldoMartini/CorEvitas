package com.generic.retailer.repositories;

import com.generic.retailer.domain.Trolley;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Trolley, Integer> {}
