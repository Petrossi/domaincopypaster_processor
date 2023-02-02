package com.domainsurvey.crawler.web.ws.publisher;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.domainsurvey.crawler.utils.JsonConverter;
import com.domainsurvey.crawler.web.ws.model.response.MessageInterface;

@Log4j2
@Component
public abstract class AbstractPublisher {

    @Autowired
    protected SimpMessagingTemplate messagingTemplate;

    protected void publishToChanel(MessageInterface message) {
        String urlToSend = "/channel/api/" + message.getId();

        log.info("publish data to channel {}: {}", message.getMessageType(), urlToSend);

        try {
            String body = JsonConverter.convertToJson(message);

            messagingTemplate.convertAndSend(urlToSend, body);
        } catch (Exception e) {
            System.out.println(message.getMessageType());
            e.printStackTrace();
        }
    }
}