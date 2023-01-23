package com.domainsurvey.crawler.model.link;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import com.domainsurvey.crawler.service.urlProcessor.model.EdgeMetaData;

@Data
@Builder
@Entity(name = Edge.TABLE_PREFIX)
public class Edge {
    public static final String TABLE_PREFIX = "edge";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    protected UUID id;

    @Column(name = "target_id", nullable = false)
    protected long targetId;

    @Column(name = "source_id", nullable = false)
    protected long sourceId;

    @Type(type = "jsonb")
    @Column(name = "meta_data", columnDefinition = "jsonb")
    protected EdgeMetaData metaData;
}