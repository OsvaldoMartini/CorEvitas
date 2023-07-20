package com.generic.retailer.repositories;

import com.generic.retailer.domain.Trolley;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Trolley, Long> {}
