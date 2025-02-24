package com.intv.showmates.bs.exception;
/**
 * @author NV
 * @version 1.0
 */
public class SeatAlreadyBookedException extends RuntimeException {

    public SeatAlreadyBookedException(String message) {
        super(message);
    }
}
