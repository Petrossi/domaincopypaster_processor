set search_path to public;

CREATE EXTENSION pgcrypto;

create table node
(
    id               bigint PRIMARY KEY,
    url              varchar(2000) not null,
    type             smallint      not null,
    depth            smallint      not null,
    robots_valid     boolean       not null,
    redirect_count   smallint      not null,
    redirected_links jsonb         not null
);

create table edge
(
    id        uuid default gen_random_uuid() not null
        constraint edge_pkey primary key,
    target_id bigint                         not null,
    source_id bigint                         not null,
    meta_data jsonb                          not null
);