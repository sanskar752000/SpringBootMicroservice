package com.example.springmicro.springmicroservice.web;

import com.example.springmicro.springmicroservice.domain.Tour;
import com.example.springmicro.springmicroservice.domain.TourRating;
import com.example.springmicro.springmicroservice.domain.TourRatingPk;
import com.example.springmicro.springmicroservice.repository.TourRatingRepository;
import com.example.springmicro.springmicroservice.repository.TourRepository;
import com.example.springmicro.springmicroservice.service.TourRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/tours/{tourId}/ratings")
public class TourRatingController {


    private TourRatingService tourRatingService;
    @Autowired
    public TourRatingController(TourRatingService tourRatingService) {
        this.tourRatingService = tourRatingService;
    }

    protected TourRatingController() {}

    @GetMapping(path = "/getAllRatingByPagingAndSorting")
    public Page<RatingDto> getAllRatingsForTourByPaging(@PathVariable(value = "tourId") int tourId, Pageable pageable) {
        Page<TourRating> tourRatingPage = tourRatingService.lookUpRating(tourId, pageable);
        List<RatingDto> ratingDtoList = tourRatingPage.getContent()
                .stream().map(tourRating -> toDto(tourRating)).collect(Collectors.toList());
        return new PageImpl<RatingDto>(ratingDtoList, pageable, tourRatingPage.getTotalPages());
    }

    @GetMapping(path = "/average")
    public AbstractMap.SimpleEntry<String, Double> getAverage(@PathVariable(value = "tourId") int tourId) {
        return new AbstractMap.SimpleEntry<>("average", tourRatingService.getAverageScore(tourId));
    }

    @PostMapping(path = "/createTourRating")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTourRating(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDto ratingDto) {
        tourRatingService.createNewRating(tourId, ratingDto.getCustomerId(), ratingDto.getScore(), ratingDto.getComment());
    }

    /*
     * Update all parameters of the rating
     * @param tourId and RatingDto
     * @return the new RatingDto Object
     * */
    @PutMapping(path = "/updateRatingWithPut")
    public RatingDto updateWithPut(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDto ratingDto) {
        return toDto(tourRatingService.updateTourRating(tourId, ratingDto.getCustomerId(), ratingDto.getScore(), ratingDto.getComment()));
    }

    /*
    * Update some parameters of the rating
    * @param tourId and RatingDto
    * @return the new RatingDto Object
    * */
    @PatchMapping(path = "/updateRatingWithPatch")
    public RatingDto updateWithPatch(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDto ratingDto) {
        return toDto(tourRatingService.updateTourRating(tourId, ratingDto.getCustomerId(), ratingDto.getScore(), ratingDto.getComment()));
    }

    @DeleteMapping(path = "/delete/{customerId}")
    public void delete(@PathVariable(value = "tourId") int tourId,
                       @PathVariable(value = "customerId") int customerId) {
        tourRatingService.deleteTourRating(tourId, customerId);
    }

    @PostMapping("/createManyRatings/{score}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createManyTourRatings(@PathVariable(value = "tourId")int tourId,
                                      @PathVariable(value = "score") int score,
                                      @RequestParam("customers") Integer[] customers) {
        tourRatingService.saveManyRatings(tourId, score, customers);
    }


    private RatingDto toDto(TourRating tourRating) {
        return new RatingDto(tourRating.getScore(), tourRating.getComment(), tourRating.getCustomerId());
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
