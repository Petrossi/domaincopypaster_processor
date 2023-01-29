package com.domainsurvey.crawler.model.link;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import com.domainsurvey.crawler.dto.RedirectedLink;
import com.domainsurvey.crawler.model.type.NodeType;

@Data
@Builder
@Entity(name = Node.TABLE_PREFIX)
public class Node {
    public static final String TABLE_PREFIX = "node";

    @Id
    @Column(name = "id", nullable = false)
    protected long id;

    @Column(name = "url", length = 2000)
    protected String url;

    @Column(name = "type")
    protected NodeType type;

    @Column(name = "download_status")
    protected NodeType downloadStatus;

    @Column(name = "robots_valid")
    private boolean robotsValid;

    @Column(name = "depth")
    private short depth;

    @Column(name = "redirect_count")
    @Builder.Default
    private byte redirectCount = 0;

    @Type(type = "jsonb")
    @Column(name = "redirected_links", columnDefinition = "jsonb")
    @Builder.Default
    protected List<RedirectedLink> redirectedLinks = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return getId() == node.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}