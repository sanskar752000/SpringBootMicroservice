package com.example.springmicro.springmicroservice.repository;

import com.example.springmicro.springmicroservice.domain.Tour;
import org.springframework.data.repository.CrudRepository;

public interface TourRepository extends CrudRepository<Tour, Integer> {
}
