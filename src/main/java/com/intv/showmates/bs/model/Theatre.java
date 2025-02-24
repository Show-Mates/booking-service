package com.intv.showmates.bs.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author NV
 * @version 1.0
 */
@Data
@Document(collection = "theatres")
public class Theatre {

    @Id
    private String id;

    private String name;
    private String location;
    private String city;
    private String country;
    private String description;
    private int totalSeats;
    private String contactNumber;
    private String email;

}
