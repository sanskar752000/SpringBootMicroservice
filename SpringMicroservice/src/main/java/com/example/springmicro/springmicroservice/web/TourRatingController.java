package com.example.springmicro.springmicroservice.web;

import com.example.springmicro.springmicroservice.domain.Tour;
import com.example.springmicro.springmicroservice.domain.TourRating;
import com.example.springmicro.springmicroservice.domain.TourRatingPk;
import com.example.springmicro.springmicroservice.repository.TourRatingRepository;
import com.example.springmicro.springmicroservice.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/tours/{tourId}/ratings")
public class TourRatingController {

    TourRatingRepository tourRatingRepository;
    TourRepository tourRepository;

    @Autowired
    public TourRatingController(TourRatingRepository tourRatingRepository, TourRepository tourRepository) {
        this.tourRatingRepository = tourRatingRepository;
        this.tourRepository = tourRepository;
    }

    protected TourRatingController() {}

    /*
    * method to get all the Rating for a Tour
     */

    @GetMapping(path = "/getAllRating")
    public List<RatingDto> getAllRatingsForTour(@PathVariable(value = "tourId") int tourId) {
        verifyTour(tourId);
        return tourRatingRepository.findByPkTourId(tourId).stream()
                .map(RatingDto::new).collect(Collectors.toList());
    }

    @GetMapping(path = "/getAllRatingByPagingAndSorting")
    public Page<RatingDto> getAllRatingsForTourByPaging(@PathVariable(value = "tourId") int tourId, Pageable pageable) {
        verifyTour(tourId);
        Page<TourRating> ratings = tourRatingRepository.findByPkTourId(tourId, pageable);
        return new PageImpl<>(
                ratings.get().map(RatingDto::new).collect(Collectors.toList()),
                pageable,
                ratings.getTotalElements()
        );
    }

    @GetMapping(path = "/average")
    public Map<String, Double> getAverage(@PathVariable(value = "tourId") int tourId) {
        verifyTour(tourId);
        return Map.of("average", tourRatingRepository.findByPkTourId(tourId).stream()
                .mapToInt(TourRating::getScore).average()
                .orElseThrow(() -> new NoSuchElementException("Tour has no Ratings")));
    }

    @PostMapping(path = "/createTourRating")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTourRating(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDto ratingDto) {
        Tour tour = verifyTour(tourId);
        if(!verifyCustomerRatingNotPresent(tourId, ratingDto.getCustomerId()))
            tourRatingRepository.save(new TourRating(new TourRatingPk(
                    tour, ratingDto.getCustomerId()), ratingDto.getScore(), ratingDto.getComment()
            ));
    }

    /*
     * Update all parameters of the rating
     * @param tourId and RatingDto
     * @return the new RatingDto Object
     * */
    @PutMapping(path = "/updateRatingWithPut")
    public RatingDto updateWithPut(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDto ratingDto) {
        TourRating rating = verifyTourRating(tourId, ratingDto.getCustomerId());
        rating.setScore(ratingDto.getScore());
        rating.setComment(ratingDto.getComment());
        return new RatingDto(tourRatingRepository.save(rating));
    }

    /*
    * Update some parameters of the rating
    * @param tourId and RatingDto
    * @return the new RatingDto Object
    * */
    @PatchMapping(path = "/updateRatingWithPatch")
    public RatingDto updateWithPatch(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDto ratingDto) {
        TourRating rating = verifyTourRating(tourId, ratingDto.getCustomerId());
        if(ratingDto.getScore() != null)
            rating.setScore(ratingDto.getScore());
        if(ratingDto.getComment() != null)
            rating.setComment(ratingDto.getComment());
        return new RatingDto(tourRatingRepository.save(rating));
    }

    @DeleteMapping(path = "/delete/{customerId}")
    public void delete(@PathVariable(value = "tourId") int tourId,
                       @PathVariable(value = "customerId") int customerId) {
        TourRating rating = verifyTourRating(tourId, customerId);
        tourRatingRepository.delete(rating);
    }

    /*
    * Verify and return the Tour given a tourId
    * @param tourId tour identifier
    * @return the found Tour
    * @throws NoSuchElementException if no Tour found
    * */
    private Tour verifyTour(int tourId) throws NoSuchElementException {
        return tourRepository.findById(tourId).orElseThrow(() ->
                new NoSuchElementException("Tour does not exist " + tourId));
    }

    /*
     * Verify and return the TourRating given a tourId and customerId
     * @param tourId and customerId
     * @return the found TourRating
     * @throws NoSuchElementException if no TourRating found
     * */
    private TourRating verifyTourRating(int tourId, int customerId) throws NoSuchElementException {
        return tourRatingRepository.findByPkTourIdAndPkCustomerId(tourId, customerId).orElseThrow(() ->
                new NoSuchElementException("Tour-Rating pair for request("
                + tourId + " for customer" + customerId));
    }

    private boolean verifyCustomerRatingNotPresent(int tourId, int customerId) throws KeyAlreadyExistsException {
        boolean customerRatingExists = tourRatingRepository.findByPkTourIdAndPkCustomerId(tourId, customerId).isPresent();
        if(customerRatingExists) {
            throw new KeyAlreadyExistsException("Customer Rating with customer id " + customerId + " for this tour already exists");
        }
        return false;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String return400(NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.ALREADY_REPORTED)
    @ExceptionHandler(KeyAlreadyExistsException.class)
    public String return208(KeyAlreadyExistsException ex) {
        return ex.getMessage();
    }

}
