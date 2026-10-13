create table lego_themes
(
    id   integer primary key, -- Rebrickable theme_id
    name varchar(100) not null
);

create table lego_sets
(
    id           bigint generated always as identity primary key,
    set_number   varchar(20)  not null unique, -- Rebrickable set_num, e.g. '10281-1'
    name         varchar(255) not null,
    theme_id     integer      not null references lego_themes (id),
    release_year smallint     not null,
    piece_count  integer      not null check (piece_count >= 0),
    image_url    varchar(500),
    status       varchar(20)  not null check (status in ('WISHLIST', 'ORDERED', 'QUEUED', 'BUILT', 'GIFTED')),
    added_at     timestamptz  not null default now(),
    updated_at   timestamptz  not null default now()
);

create index lego_sets_status_idx on lego_sets (status);
create index lego_sets_theme_id_idx on lego_sets (theme_id);
