package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkMetaData implements Serializable {
    private static final long serialVersionUID = -4527154551914260163L;

    private boolean robotsValid;
    private short depth;
    private byte redirectCount;
}