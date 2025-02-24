package com.intv.showmates.bs.repository;

import com.intv.showmates.bs.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * @author NV
 * @version 1.0
 */
@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByCustomerId(String customerId);
    List<Booking> findByTheatreIdAndShowTime(String theatreId, String showTime);
    List<Booking> findByTheatreIdAndMovieId(String theatreId, String movieId);
    List<Booking> findByTheatreIdAndMovieIdAndShowTime(String theatreId, String movieId, String showTime);
    Optional<Booking> findByTheatreIdAndShowTimeAndSeatNumber(String theatreId, String showTime, String seatNumber);

}
