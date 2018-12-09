package com.tradency.kubemq.sdk.event;

import com.tradency.kubemq.sdk.basic.ServerAddressNotSuppliedException;
import com.tradency.kubemq.sdk.event.lowlevel.Sender;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLException;

/**
 * Represents a Sender with predefined parameters.
 */
public class Channel {

    private Sender sender;

    private String channelName;
    private String clientID;
    private boolean store;
    private boolean returnResult;

    public Channel(ChannelParameters parameters) {
        this(
                parameters.getChannelName(),
                parameters.getClientID(),
                parameters.isStore(),
                parameters.getKubeMQAddress()
        );
    }

    /**
     * Initializes a new instance of the MessageChannel class using a set of parameters.
     *
     * @param channelName   Represents The channel name to send to using the KubeMQ.
     * @param clientID      Represents the sender ID that the messages will be send under.
     * @param store         Represents if the messages should be set to persistence.
     * @param kubeMQAddress Represents The address of the KubeMQ server.
     */
    public Channel(String channelName, String clientID, boolean store, String kubeMQAddress) {
        this.channelName = channelName;
        this.clientID = clientID;
        this.store = store;

        isValid();

        sender = new Sender(kubeMQAddress);
    }

    /**
     * Send a single message using the KubeMQ.
     *
     * @param event The com.tradency.kubemq.sdk.pubsub.Event to send using KubeMQ.
     * @return com.tradency.kubemq.sdk.event.MessageDeliveryReport that contain info regarding message status.
     * @throws ServerAddressNotSuppliedException KubeMQ server address can not be determined.
     * @throws SSLException                      Indicates some kind of error detected by an SSL subsystem.
     */
    public Result SendEvent(Event event) throws ServerAddressNotSuppliedException, SSLException {
        return sender.SendEvent(CreateLowLevelEvent(event));
    }

    public Result SendEvent(Event event, boolean returnResult) throws ServerAddressNotSuppliedException, SSLException {
        return sender.SendEvent(CreateLowLevelEvent(event, returnResult));
    }

    /**
     * Publish constant stream of messages.
     *
     * @param messageDeliveryReportStreamObserver Observer for Delivery Reports.
     * @return StreamObserver used to stream messages
     * @throws ServerAddressNotSuppliedException KubeMQ server address can not be determined.
     * @throws SSLException                      Indicates some kind of error detected by an SSL subsystem.
     */
    public StreamObserver<Event> StreamEvent(final StreamObserver<Result> messageDeliveryReportStreamObserver) throws ServerAddressNotSuppliedException, SSLException {
        StreamObserver<com.tradency.kubemq.sdk.event.lowlevel.Event> observer = sender.StreamEvent(messageDeliveryReportStreamObserver);
        return new StreamObserver<Event>() {

            @Override
            public void onNext(Event value) {
                observer.onNext(CreateLowLevelEvent(value));
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }
        };
    }

    /**
     * Publish constant stream of messages.
     *
     * @param messageDeliveryReportStreamObserver Observer for Delivery Reports.
     * @param returnResult                        Represents if the end user does not need the Result.
     * @return StreamObserver used to stream messages
     * @throws ServerAddressNotSuppliedException KubeMQ server address can not be determined.
     * @throws SSLException                      Indicates some kind of error detected by an SSL subsystem.
     */
    public StreamObserver<Event> StreamEvent(final StreamObserver<Result> messageDeliveryReportStreamObserver, boolean returnResult) throws ServerAddressNotSuppliedException, SSLException {
        StreamObserver<com.tradency.kubemq.sdk.event.lowlevel.Event> observer = sender.StreamEvent(messageDeliveryReportStreamObserver);
        return new StreamObserver<Event>() {

            @Override
            public void onNext(Event value) {
                observer.onNext(CreateLowLevelEvent(value, returnResult));
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }
        };
    }

    private void isValid() {
        if (StringUtils.isEmpty(channelName)) {
            throw new IllegalArgumentException("Parameter channelName is mandatory");
        }
    }

    private com.tradency.kubemq.sdk.event.lowlevel.Event CreateLowLevelEvent(Event notification) {
        return new com.tradency.kubemq.sdk.event.lowlevel.Event(
                getChannelName(),
                notification.getMetadata(),
                notification.getBody(),
                notification.getEventId(),
                getClientID(),
                isStore()
        );
    }


    private com.tradency.kubemq.sdk.event.lowlevel.Event CreateLowLevelEvent(Event notification, boolean returnResult) {
        com.tradency.kubemq.sdk.event.lowlevel.Event event = CreateLowLevelEvent(notification);
        event.setReturnResult(returnResult);
        return event;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public boolean isStore() {
        return store;
    }

    public void setStore(boolean store) {
        this.store = store;
    }

    public boolean isReturnResult() {
        return returnResult;
    }

    public void setReturnResult(boolean returnResult) {
        this.returnResult = returnResult;
    }

}