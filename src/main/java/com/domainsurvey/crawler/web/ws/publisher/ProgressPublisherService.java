package com.domainsurvey.crawler.web.ws.publisher;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import com.domainsurvey.crawler.web.ws.model.response.MessageFinished;
import com.domainsurvey.crawler.web.ws.model.response.MessageProcessUpdate;
import com.domainsurvey.crawler.web.ws.model.response.MessageStarted;

@Service
public class ProgressPublisherService extends AbstractPublisher {

    public void publishProcessUpdate(String id, StateStore stateStore) {
        publishToChanel(new MessageProcessUpdate(id, stateStore));
    }

    public void publishFinish(String id) {
        publishToChanel(new MessageFinished(id));
    }

    public void publishStarted(String id) {
        publishToChanel(new MessageStarted(id));
    }
}