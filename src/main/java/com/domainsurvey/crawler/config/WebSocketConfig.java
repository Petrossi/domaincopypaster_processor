package com.domainsurvey.crawler.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeHandler;
import com.domainsurvey.crawler.service.dao.DomainService;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final DomainService domainService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/crawler/ws")
//                .setHandshakeHandler(handshakeHandler())
                .setAllowedOrigins("*")
                .withSockJS()
        ;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/api");
        registry.enableSimpleBroker("/channel");
    }

    private HandshakeHandler handshakeHandler() {
        return (serverHttpRequest, serverHttpResponse, webSocketHandler, map) -> {
            String query = serverHttpRequest.getURI().getPath();

            String domainId = query.split("/")[3];

            boolean valid = domainService.find(domainId).isPresent();

            try {
                HttpStatus status = valid ? HttpStatus.OK : HttpStatus.NOT_FOUND;

                serverHttpResponse.setStatusCode(status);
                serverHttpResponse.flush();
                serverHttpResponse.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        };
    }
}