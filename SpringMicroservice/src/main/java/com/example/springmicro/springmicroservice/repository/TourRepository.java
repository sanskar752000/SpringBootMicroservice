package com.example.springmicro.springmicroservice.repository;

import com.example.springmicro.springmicroservice.domain.Tour;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TourRepository extends CrudRepository<Tour, Integer> {
    List<Tour> findByTourPackageCode(String code);
}
