package com.tradency.kubemq.sdk.basic;

public class ServerAddressNotSuppliedException extends Exception {
    ServerAddressNotSuppliedException() {
        super("Server Address was not supplied");
    }
}
