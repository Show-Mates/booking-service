package com.intv.showmates.bs.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
/**
 * @author NV
 * @version 1.0
 */
@Data
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;
    
    private String customerId;
    private String movieId;
    private String theatreId;
    private String showTime;
    private String seatNumber;
    private String status;
    private Double amountPaid;


}
