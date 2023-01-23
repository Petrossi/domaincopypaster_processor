set search_path to public;

create table domain_crawling_info
(
    id                           bigserial
        constraint domain_crawling_info_sequence PRIMARY KEY,
    domain_id                    varchar(10) default null,
    finalize_status              smallint not null,
    hard_retry_count             smallint    default 0,
    queue_added_timestamp        timestamp   default null,
    crawling_started_timestamp   timestamp   default null,
    crawling_finished_timestamp  timestamp   default null,
    finalizer_started_timestamp  timestamp   default null,
    finalizer_finished_timestamp timestamp   default null,
    total                        integer     default null,
    blocked                      integer     default null,
    in_queue                     integer     default null,
    error                        integer     default null,
    warning                      integer     default null,
    notice                       integer     default null,
    no_issue                     integer     default null,
    score                        smallint    default null
);

ALTER TABLE domain_crawling_info
    ADD FOREIGN KEY (domain_id) REFERENCES domain (id) on delete cascade;