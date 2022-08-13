package com.example.springmicro.springmicroservice.service;

import com.example.springmicro.springmicroservice.domain.Tour;
import com.example.springmicro.springmicroservice.domain.TourRating;
import com.example.springmicro.springmicroservice.repository.TourRatingRepository;
import com.example.springmicro.springmicroservice.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;

@Service
@Transactional // roll back the database activity if something fails before completion
public class TourRatingService {
    private TourRatingRepository tourRatingRepository;
    private TourRepository  tourRepository;

    @Autowired
    public TourRatingService(TourRatingRepository tourRatingRepository, TourRepository tourRepository) {
        this.tourRatingRepository = tourRatingRepository;
        this.tourRepository = tourRepository;
    }

    public void createNewRating(int tourId, Integer custormerId, Integer score, String comment) throws NoSuchElementException {
        if(!verifyCustomerRatingNotPresent(tourId, custormerId))
            tourRatingRepository.save(new TourRating(verifyTour(tourId), custormerId, score, comment));
    }

    public Page<TourRating> lookUpRating(int tourId, Pageable pageable) {
        return tourRatingRepository.findByTourId(verifyTour(tourId).getId(), pageable);
    }

    public TourRating updateTourRating(int tourId, Integer customerId, Integer score, String comment) throws NoSuchElementException {
        TourRating rating = verifyTourRating(tourId, customerId);
        if(score!=null) {
            rating.setScore(score);
        }
        if(comment != null ) {
            rating.setComment(comment);
        }
        return tourRatingRepository.save(rating);
    }

    public void deleteTourRating(int tourId, Integer customerId) throws NoSuchElementException {
        TourRating rating = verifyTourRating(tourId, customerId);
        tourRatingRepository.delete(rating);
    }

    public Double getAverageScore(int tourId) throws NoSuchElementException {
        List<TourRating> ratings = tourRatingRepository.findByTourId(verifyTour(tourId).getId());
        OptionalDouble average = ratings.stream().mapToInt((rating) -> rating.getScore()).average();
        return average.isPresent() ? average.getAsDouble(): null;
    }

     public void saveManyRatings(int tourId, int score, Integer[] customers) {
        tourRepository.findById(tourId).ifPresent(tour -> {
            for(Integer c: customers) {
                tourRatingRepository.save(new TourRating(tour, c, score));
            }
        });
     }

    private Tour verifyTour(int tourId) throws NoSuchElementException {
        return tourRepository.findById(tourId).orElseThrow(() ->
                new NoSuchElementException("Tour does not exist " + tourId)
        );
    }
    /**
     * Verify and return the TourRating for a particular tourId and Customer
     * @param tourId
     * @param customerId
     * @return the found TourRating
     * @throws NoSuchElementException if no TourRating found
     */
    private TourRating verifyTourRating(int tourId, int customerId) throws NoSuchElementException {
        return tourRatingRepository.findByTourIdAndCustomerId(tourId, customerId).orElseThrow(() ->
                new NoSuchElementException("Tour-Rating pair for request("
                        + tourId + " for customer" + customerId));
    }

    private boolean verifyCustomerRatingNotPresent(int tourId, int customerId) throws KeyAlreadyExistsException {
        boolean customerRatingExists = tourRatingRepository.findByTourIdAndCustomerId(tourId, customerId).isPresent();
        if(customerRatingExists) {
            throw new KeyAlreadyExistsException("Customer Rating with customer id " + customerId + " for this tour already exists");
        }
        return false;
    }
}
