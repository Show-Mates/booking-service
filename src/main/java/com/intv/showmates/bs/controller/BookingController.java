package com.intv.showmates.bs.controller;

import com.intv.showmates.bs.exception.ErrorResponse;
import com.intv.showmates.bs.exception.SeatAlreadyBookedException;
import com.intv.showmates.bs.model.Booking;
import com.intv.showmates.bs.model.Theatre;
import com.intv.showmates.bs.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author NV
 * @version 1.0
 */
@RestController
@RequestMapping("/api/bookings/v1.0")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        try {
            Booking createdBooking = bookingService.createBooking(booking);
            return ResponseEntity.ok(createdBooking);
        } catch (SeatAlreadyBookedException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/fetch/customer/{customerId}")
    public ResponseEntity<List<Booking>> getBookingsByCustomer(@PathVariable String customerId) {
        List<Booking> bookings = bookingService.getBookingsByCustomerId(customerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/fetch/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.fetchAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/fetch/theatre/{theatreId}/time/{showTime}")
    public ResponseEntity<List<Booking>> getBookingsByTheatreAndTime(
            @PathVariable String theatreId,
            @PathVariable String showTime) {
        List<Booking> bookings = bookingService.getBookingsByTheatreAndTime(theatreId, showTime);
        return ResponseEntity.ok(bookings);
    }
    @GetMapping("/fetch/theatre/{theatreId}/movie/{movieId}")
    public ResponseEntity<List<Booking>> getBookingsByTheatreAndMovie(
            @PathVariable String theatreId,
            @PathVariable String movieId) {
        List<Booking> bookings = bookingService.getBookingsTheatreAndMovie(theatreId, movieId);
        return ResponseEntity.ok(bookings);
    }
    @GetMapping("/fetch/theatre/{theatreId}/movie/{movieId}/time/{showTime}")
    public ResponseEntity<List<Booking>> getBookingsByTheatreAndMovieAndTime(
            @PathVariable String theatreId,
            @PathVariable String movieId,
            @PathVariable String showTime) {
        List<Booking> bookings = bookingService.getBookingsByTheatreAndMovieAndTime(theatreId, movieId, showTime);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/fetch/movie/{movieId}/city/{city}/date/{date}")
    public ResponseEntity<List<Theatre>> getTheatresForMovieInTown(
            @PathVariable String movieId,
            @PathVariable String city,
            @PathVariable String date) {
        List<Theatre> theatres = bookingService.getTheatresForMovieInTown(movieId, city, date);
        return ResponseEntity.ok(theatres);
    }
}
