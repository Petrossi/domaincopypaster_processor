package com.domainsurvey.crawler.model.page;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.urlProcessor.model.HashedMetaData;
import com.domainsurvey.crawler.service.urlProcessor.model.SavedMetaData;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Data
@Entity(name = "page")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class)
})
public class Page implements Serializable {
    private static final long serialVersionUID = -4527345351914260163L;

    public static final String TABLE_PREFIX = "page";

    @Id
    @Column(name = "id", nullable = false)
    protected long id;

    @Column(name = "status_code")
    private Short statusCode;

    @Type(type = "int-array")
    @Column(name = "filters", columnDefinition = "integer[]")
    private List<Integer> filters = new ArrayList<>();

    @Column(name = "weight", columnDefinition = "numeric(10, 2)")
    private double weight;

    @Column(name = "incoming_count_total")
    protected long incomingCountTotal;

    @Column(name = "score")
    protected short score;

    @Type(type = "jsonb")
    @Column(name = "saved_meta_data", columnDefinition = "jsonb")
    private SavedMetaData savedMetaData;

    @Type(type = "jsonb")
    @Column(name = "hashed_meta_data", columnDefinition = "jsonb")
    private HashedMetaData hashedMetaData;

    public Page(PageResult pageResult) {
        this();
        this.id = pageResult.getId();
        this.statusCode = pageResult.getStatusCode();
        this.filters = pageResult.getFilters();
        this.score = pageResult.getScore();
        this.savedMetaData = pageResult.getSavedMetaData();
        this.hashedMetaData = pageResult.getHashedMetaData();
    }

    public Page() {
    }
}