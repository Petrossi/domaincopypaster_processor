set search_path to public;

create table domain_filter
(
    id                      bigserial
        constraint domain_filter_sequence PRIMARY KEY,
    domain_crawling_info_id bigint   not null,
    filter_id               bigint   not null,
    count                   bigint   not null,
    version                 smallint not null
);

ALTER TABLE domain_filter
    ADD FOREIGN KEY (domain_crawling_info_id) REFERENCES domain_crawling_info (id) on delete cascade;