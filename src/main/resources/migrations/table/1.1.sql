set search_path to public;

create table config
(
    id             bigserial
        constraint config_sequence PRIMARY KEY,
    ignore_robots  boolean  not null,
    spa            boolean  not null,
    pages_limit    int      not null,
    thread_count   smallint not null,
    robots_content text default null
);

create table domain
(
    id                       varchar(10) PRIMARY KEY not null,
    host                     varchar(253)            not null,
    protocol                 varchar(5)              not null,
    created_timestamp        timestamp               not null,
    status                   smallint                not null,
    priority                 smallint                not null,
    config_id                bigint                  not null,
    process_crawling_info_id bigint   default null,
    final_crawling_info_id   bigint   default null,
    robots_id                bigint   default null,
    score                    smallint default null,
    crawl_count              int      default null,
    non_deletable            bool     default null
);

CREATE INDEX idx_domain_status ON domain (status);
CREATE INDEX idx_domain_priority ON domain (priority);

ALTER TABLE domain
    ADD FOREIGN KEY (config_id) REFERENCES config (id) on delete cascade;