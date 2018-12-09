package com.tradency.kubemq.sdk.examples.commandqueryresponder;

import com.tradency.kubemq.sdk.basic.ServerAddressNotSuppliedException;

import javax.net.ssl.SSLException;
import java.io.IOException;

public class Program {
    public static void main(String[] args) {
        System.out.println();
        System.out.println("Starting ReqRepResponder...");
        System.out.println();
        System.out.println("Press 'Enter' to stop the application...");
        System.out.println();

        try {
            new CommandQueryResponder();
        } catch (io.grpc.StatusRuntimeException e) {
            System.out.println("Error: KubeMQ is unreachable.");
        } catch (ServerAddressNotSuppliedException e) {
            System.out.println("Error: Can not determine KubeMQ server address.");
        } catch (SSLException e) {
            System.out.println("Error: error detected by an SSL subsystem");
        }

        try {
            int read = System.in.read();
        } catch (IOException e) {
            System.out.println("Error:  I/O error occurred.");
        }
    }
}
