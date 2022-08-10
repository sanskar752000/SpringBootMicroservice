package com.example.springmicro.web;

import com.example.springmicro.domain.TourRating;
import com.example.springmicro.repository.TourRatingRepository;
import com.example.springmicro.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
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
    private TourRatingRepository tourRatingRepository;
    private TourRepository tourRepository;

    @Autowired
    public TourRatingController(TourRatingRepository tourRatingRepository, TourRepository tourRepository) {
        this.tourRatingRepository = tourRatingRepository;
        this.tourRepository = tourRepository;
    }

    protected TourRatingController() {

    }

    /**
     * Create a Tour Rating.
     *
     * @param tourId tour identifier
     * @param tourRating rating data transfer object
     */
    @PostMapping(path = "/createTourRating")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTourRating(@PathVariable(value = "tourId") String tourId,
                                 @RequestBody @Validated TourRating tourRating) {
        verifyTour(tourId);
        if(!verifyCustomerRatingNotPresent(tourId, tourRating.getCustomerId()))
            tourRatingRepository.save(new TourRating(tourId, tourRating.getCustomerId(),
                    tourRating.getScore(), tourRating.getComment()));
    }

    @GetMapping(path = "/getAllRating")
    public List<TourRating> getAllRatingsForTour(@PathVariable(value = "tourId") String tourId) {
        verifyTour(tourId);
        return tourRatingRepository.findByTourId(tourId);
    }

    /**
     * Lookup a page of Ratings for a tour.
     *
     * @param tourId Tour Identifier
     * @param pageable paging details
     * @return Requested page of Tour Ratings as RatingDto's
     */
    @GetMapping(path = "/getAllRatingByPagingAndSorting")
    public Page<TourRating> getRatings(@PathVariable(value = "tourId") String tourId,
                                       Pageable pageable){
        return tourRatingRepository.findByTourId(tourId, pageable);
    }

    /**
     * Calculate the average Score of a Tour.
     *
     * @param tourId tour identifier
     * @return Tuple of "average" and the average value.
     */
    @GetMapping(path = "/average")
    public Map<String, Double> getAverage(@PathVariable(value = "tourId") String tourId) {
        verifyTour(tourId);
        return Map.of("average",tourRatingRepository.findByTourId(tourId).stream()
                .mapToInt(TourRating::getScore).average()
                .orElseThrow(() ->
                        new NoSuchElementException("Tour has no Ratings")));
    }

    /**
     * Update score and comment of a Tour Rating
     *
     * @param tourId tour identifier
     * @param tourRating rating Data Transfer Object
     * @return The modified Rating DTO.
     */
    @PutMapping(path = "/updateRatingWithPut")
    public TourRating updateWithPut(@PathVariable(value = "tourId") String tourId,
                                    @RequestBody @Validated TourRating tourRating) {
        TourRating rating = verifyTourRating(tourId, tourRating.getCustomerId());
        rating.setScore(tourRating.getScore());
        rating.setComment(tourRating.getComment());
        return tourRatingRepository.save(rating);
    }
    /**
     * Update score or comment of a Tour Rating
     *
     * @param tourId tour identifier
     * @param tourRating rating Data Transfer Object
     * @return The modified Rating DTO.
     */
    @PatchMapping(path = "/updateRatingWithPatch")
    public TourRating updateWithPatch(@PathVariable(value = "tourId") String tourId,
                                      @RequestBody @Validated TourRating tourRating) {
        TourRating rating = verifyTourRating(tourId, tourRating.getCustomerId());
        if (tourRating.getScore() != null) {
            rating.setScore(tourRating.getScore());
        }
        if (tourRating.getComment() != null) {
            rating.setComment(tourRating.getComment());
        }
        return tourRatingRepository.save(rating);
    }

    /**
     * Delete a Rating of a tour made by a customer
     *
     * @param tourId tour identifier
     * @param customerId customer identifier
     */
    @DeleteMapping(path = "/delete/{customerId}")
    public void delete(@PathVariable(value = "tourId") String tourId,
                       @PathVariable(value = "customerId") int customerId) {
        TourRating rating = verifyTourRating(tourId, customerId);
        tourRatingRepository.delete(rating);
    }

    /**
     * Verify and return the TourRating for a particular tourId and Customer
     * @param tourId tour identifier
     * @param customerId customer identifier
     * @return the found TourRating
     * @throws NoSuchElementException if no TourRating found
     */
    private TourRating verifyTourRating(String tourId, int customerId) throws NoSuchElementException {
        return tourRatingRepository.findByTourIdAndCustomerId(tourId, customerId).orElseThrow(() ->
                new NoSuchElementException("Tour-Rating pair for request("
                        + tourId + " for customer" + customerId));
    }

    /**
     * Verify and return the Tour given a tourId.
     *
     * @param tourId tour identifier
     * @throws NoSuchElementException if no Tour found.
     */
    private void verifyTour(String tourId) throws NoSuchElementException {
        if (!tourRepository.existsById(tourId)) {
            throw new NoSuchElementException("Tour does not exist " + tourId);
        }
    }

    private boolean verifyCustomerRatingNotPresent(String tourId, int customerId) throws KeyAlreadyExistsException {
        boolean customerRatingExists = tourRatingRepository.findByTourIdAndCustomerId(tourId, customerId).isPresent();
        if(customerRatingExists) {
            throw new KeyAlreadyExistsException("Customer Rating with customer id " + customerId + " for this tour already exists");
        }
        return false;
    }

    /**
     * Exception handler if NoSuchElementException is thrown in this Controller
     *
     * @param ex exception
     * @return Error message String.
     */
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
