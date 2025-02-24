package com.intv.showmates.bs.service;

import com.intv.showmates.bs.exception.ResourceNotFoundException;
import com.intv.showmates.bs.exception.SeatAlreadyBookedException;
import com.intv.showmates.bs.model.Booking;
import com.intv.showmates.bs.model.Theatre;
import com.intv.showmates.bs.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author NV
 * @version 1.0
 */

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${theatre.service.base.url}")
    private String theatreServiceUrl;

    public Booking createBooking(Booking booking) {

        Optional<Booking> existingBooking = bookingRepository.findByTheatreIdAndShowTimeAndSeatNumber(
                booking.getTheatreId(), booking.getShowTime(), booking.getSeatNumber());

        if (existingBooking.isPresent() && "confirmed".equals(existingBooking.get().getStatus())) {
            throw new SeatAlreadyBookedException("The seat " + booking.getSeatNumber() + " is already booked and confirmed.");
        }
        booking.setId(sequenceGeneratorService.generateUserId());
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("savedBooking : " + savedBooking);
        reduceSeatCount(savedBooking.getTheatreId());
        applyDiscounts(savedBooking);
        return savedBooking;
    }

    public List<Booking> getBookingsByCustomerId(String customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> fetchAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByTheatreAndTime(String theatreId, String showTime) {
        return bookingRepository.findByTheatreIdAndShowTime(theatreId, showTime);
    }

    public List<Booking> getBookingsTheatreAndMovie(String theatreId, String movieId) {
        return bookingRepository.findByTheatreIdAndMovieId(theatreId, movieId);
    }

    public List<Booking> getBookingsByTheatreAndMovieAndTime(String theatreId, String movieId, String showTime) {
        return bookingRepository.findByTheatreIdAndMovieIdAndShowTime(theatreId, movieId, showTime);
    }

    private int reduceSeatCount(String theatreID) {
        String theatreFindUrl = theatreServiceUrl + "/api/theatres/v1.0/fetch/id/" + theatreID;
        System.out.println("theatre Find Url : " + theatreFindUrl);
        int count = 0;
        try {
            Theatre theatre = restTemplate.getForObject(theatreFindUrl, Theatre.class);
            System.out.println("theatre : " + theatre);
            if (theatre == null) {
                throw new ResourceNotFoundException("Theatre with ID " + theatreID + " not found.");
            }
            String theatreUpdateUrl = theatreServiceUrl + "/api/theatres/v1.0/update/" + theatreID;
            System.out.println("theatreUpdateUrl : " + theatreUpdateUrl);
            theatre.setTotalSeats(theatre.getTotalSeats() - 1);
            HttpEntity<Theatre> requestEntity = new HttpEntity<>(theatre);

            ResponseEntity<Theatre> responseEntity = restTemplate.exchange(
                    theatreUpdateUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    Theatre.class
            );
            System.out.println("responseEntity.getStatusCode() : " + responseEntity.getStatusCode());
            if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Theatre with ID " + theatreID + " not found.");
            }
            count = Objects.requireNonNull(responseEntity.getBody()).getTotalSeats();
        } catch (Exception e) {
            System.out.println("Error occurred while checking the department details : " + e.getMessage());
        }
        return count;
    }

    private void applyDiscounts(Booking booking) {
        int totalBookingsForCustomer = bookingRepository.findByCustomerId(booking.getCustomerId()).size();
        Double discount = 0.0;

        if (totalBookingsForCustomer == 3) {
            discount += 0.50; // 50% discount
        }
        LocalDateTime dateTime = LocalDateTime.parse(booking.getShowTime());
        LocalTime showTime = dateTime.toLocalTime();
//        LocalTime showTime = LocalTime.parse(booking.getShowTime());
        if (showTime.isAfter(LocalTime.of(12, 0)) && showTime.isBefore(LocalTime.of(18, 0))) {
            discount += 0.20; // 20% discount
        }

        if (discount > 0) {
            double originalAmount = booking.getAmountPaid();
            double discountAmount = originalAmount * discount;
            double finalAmount = originalAmount - discountAmount;
            booking.setAmountPaid(finalAmount);

            bookingRepository.save(booking);
        }
    }

    public List<Theatre> getTheatresForMovieInTown(String movieId, String city, String date) {
        String url = theatreServiceUrl + "/api/theatres/v1.0/fetch/movie/" + movieId + "/city/" + city + "/date/" + date;
        ResponseEntity<List<Theatre>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Theatre>>() {
                }
        );
        return response.getBody();
    }

    public List<Booking> createBulkBooking(List<Booking> bookings) {
        List<Booking> createdBookings = new ArrayList<>();

        for (Booking booking : bookings) {
            // Check if the seat is already booked
            Optional<Booking> existingBooking = bookingRepository.findByTheatreIdAndShowTimeAndSeatNumber(
                    booking.getTheatreId(), booking.getShowTime(), booking.getSeatNumber());

            if (existingBooking.isPresent() && "confirmed".equals(existingBooking.get().getStatus())) {
                throw new SeatAlreadyBookedException("The seat " + booking.getSeatNumber() + " is already booked and confirmed.");
            }

            booking.setId(sequenceGeneratorService.generateUserId());
            Booking savedBooking = bookingRepository.save(booking);
            reduceSeatCount(savedBooking.getTheatreId());
            applyDiscounts(savedBooking);
            createdBookings.add(savedBooking);
        }
        return createdBookings;
    }

    public List<Booking> cancelBulkBooking(List<Booking> bookings) {
        List<Booking> cancelledBookings = new ArrayList<>();

        for (Booking booking : bookings) {
            Optional<Booking> existingBooking = bookingRepository.findById(booking.getId());

            if (existingBooking.isPresent()) {
                Booking foundBooking = existingBooking.get();
                if ("cancelled".equals(foundBooking.getStatus())) {
                    throw new ResourceNotFoundException("The booking with ID " + booking.getId() + " is already cancelled.");
                }
                foundBooking.setStatus("cancelled");
                Booking cancelledBooking = bookingRepository.save(foundBooking);
                increaseSeatCount(cancelledBooking.getTheatreId()); // Adjust seat count after cancellation
                cancelledBookings.add(cancelledBooking);
            } else {
                throw new ResourceNotFoundException("Booking with ID " + booking.getId() + " not found.");
            }
        }
        return cancelledBookings;
    }

    private void increaseSeatCount(String theatreID) {
        String theatreFindUrl = theatreServiceUrl + "/api/theatres/v1.0/fetch/id/" + theatreID;
        try {
            Theatre theatre = restTemplate.getForObject(theatreFindUrl, Theatre.class);
            if (theatre == null) {
                throw new ResourceNotFoundException("Theatre with ID " + theatreID + " not found.");
            }
            String theatreUpdateUrl = theatreServiceUrl + "/api/theatres/v1.0/update/" + theatreID;
            theatre.setTotalSeats(theatre.getTotalSeats() + 1);
            HttpEntity<Theatre> requestEntity = new HttpEntity<>(theatre);

            restTemplate.exchange(
                    theatreUpdateUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    Theatre.class
            );
        } catch (Exception e) {
            System.out.println("Error occurred while updating seat count: " + e.getMessage());
        }
    }


}
