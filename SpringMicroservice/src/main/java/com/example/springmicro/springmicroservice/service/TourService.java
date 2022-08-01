package com.example.springmicro.springmicroservice.service;

import com.example.springmicro.springmicroservice.domain.Difficulty;
import com.example.springmicro.springmicroservice.domain.Region;
import com.example.springmicro.springmicroservice.domain.Tour;
import com.example.springmicro.springmicroservice.domain.TourPackage;
import com.example.springmicro.springmicroservice.repository.TourPackageRepository;
import com.example.springmicro.springmicroservice.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TourService {
    private TourRepository tourRepository;
    private TourPackageRepository tourPackageRepository;

    @Autowired
    public TourService(TourRepository tourRepository, TourPackageRepository tourPackageRepository) {
        this.tourRepository = tourRepository;
        this.tourPackageRepository = tourPackageRepository;
    }

    public Tour createTour(String title, String description, String blurb, Integer price,
                           String duration, String bullets,
                           String keywords, String tourPackageName, Difficulty difficulty, Region region ){
        TourPackage tourPackage = tourPackageRepository.findByName(tourPackageName)
                .orElseThrow(() -> new RuntimeException("Tour Package does not exists"));
        return tourRepository.save(new Tour(title, description, blurb,
                price, duration, bullets, keywords, tourPackage, difficulty, region));
    }

    public long total() {
        return tourRepository.count();
    }
}
