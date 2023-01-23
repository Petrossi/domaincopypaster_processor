set search_path to public;

create table page
(
    id                   bigint PRIMARY KEY,
    status_code          smallint       default 0,
    score                smallint       default 0,
    filters              integer[]      default null,
    weight               numeric(10, 2) default 0.25,
    incoming_count_total integer        default 0,
    saved_meta_data      jsonb          default null,
    hashed_meta_data     jsonb          default null
);
