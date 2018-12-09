package com.tradency.kubemq.sdk.event;

import com.tradency.kubemq.sdk.basic.GrpcClient;
import com.tradency.kubemq.sdk.basic.ServerAddressNotSuppliedException;
import com.tradency.kubemq.sdk.grpc.Kubemq;
import com.tradency.kubemq.sdk.subscription.EventsStoreType;
import com.tradency.kubemq.sdk.subscription.SubscribeRequest;
import com.tradency.kubemq.sdk.subscription.SubscribeType;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.Iterator;

public class Subscriber extends GrpcClient {

    private static Logger logger = LoggerFactory.getLogger(Subscriber.class);

    /**
     * Initialize a new Subscriber to incoming messages
     * KubeMQAddress will be parsed from Config or environment parameter
     */
    public Subscriber() {
        this(null);
    }

    /**
     * Initialize a new Subscriber to incoming messages
     *
     * @param KubeMQAddress KubeMQ server address
     */
    public Subscriber(String KubeMQAddress) {
        _kubemqAddress = KubeMQAddress;
    }

    /**
     * Register to kubeMQ Channel using com.tradency.kubemq.sdk.Subscription.SubscribeRequest.
     * This method is blocking.
     *
     * @param subscribeRequest Parameters list represent by com.tradency.kubemq.sdk.Subscription.SubscribeRequest
     *                         that will determine the subscription configuration.
     * @return com.tradency.kubemq.sdk.PubSub.MessageReceive.
     * @throws ServerAddressNotSuppliedException Thrown exception when KubeMQ server address can not be determined.
     * @throws SSLException                      Indicates some kind of error detected by an SSL subsystem.
     */
    public EventReceive SubscribeToEvents(SubscribeRequest subscribeRequest) throws ServerAddressNotSuppliedException, SSLException {

        ValidateSubscribeRequest(subscribeRequest);

        Iterator<Kubemq.EventReceive> call = GetKubeMQClient()
                .subscribeToEvents(subscribeRequest.ToInnerSubscribeRequest());

        if (call.hasNext()) {
            Kubemq.EventReceive innerMessage = call.next();
            LogIncomingMessage(innerMessage);
            return new EventReceive(innerMessage);
        }
        return null;
    }

    /**
     * Register to kubeMQ Channel using com.tradency.kubemq.sdk.Subscription.SubscribeRequest.
     * This method is async.
     *
     * @param subscribeRequest Parameters list represent by com.tradency.kubemq.sdk.Subscription.SubscribeRequest
     *                         that will determine the subscription configuration.
     * @param streamObserver   Async StreamObserver to handle queue messages
     * @throws ServerAddressNotSuppliedException Thrown exception when KubeMQ server address can not be determined.
     * @throws SSLException                      Indicates some kind of error detected by an SSL subsystem.
     */
    public void SubscribeToEvents(
            SubscribeRequest subscribeRequest,
            StreamObserver<EventReceive> streamObserver
    ) throws ServerAddressNotSuppliedException, SSLException {

        Kubemq.Subscribe innerSubscribeRequest = subscribeRequest.ToInnerSubscribeRequest();
        StreamObserver<Kubemq.EventReceive> observer = new StreamObserver<Kubemq.EventReceive>() {
            @Override
            public void onNext(Kubemq.EventReceive messageReceive) {
                LogIncomingMessage(messageReceive);
                streamObserver.onNext(new EventReceive(messageReceive));
            }

            @Override
            public void onError(Throwable t) {
                streamObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                streamObserver.onCompleted();
            }
        };

        GetKubeMQAsyncClient().subscribeToEvents(innerSubscribeRequest, observer);
    }

    private void ValidateSubscribeRequest(SubscribeRequest subscribeRequest) {
        if (StringUtils.isBlank(subscribeRequest.getChannel())) {
            throw new IllegalArgumentException("Parameter Channel is mandatory");
        }
        if (subscribeRequest.IsNotValidType("Events")) {
            throw new IllegalArgumentException("Invalid Subscribe Type for this Class");
        }
        if (subscribeRequest.getSubscribeType() == SubscribeType.EventsStore) {
            if (StringUtils.isBlank(subscribeRequest.getClientID())) {
                throw new IllegalArgumentException("Parameter ClientID is mandatory");
            }
            if (subscribeRequest.getEventsStoreType() == EventsStoreType.Undefined) {
                throw new IllegalArgumentException("Parameter EventsStoreType is mandatory for this type");
            }
        }
    }

    private void LogIncomingMessage(Kubemq.EventReceive message) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Subscriber Received Event: EventID:'{}', Channel:'{}', Metadata: '{}'",
                    message.getEventID(),
                    message.getChannel(),
                    message.getMetadata()
            );
        }
    }
}
