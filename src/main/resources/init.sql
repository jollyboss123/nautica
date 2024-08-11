create table if not exists trips
(
    id       text check ( length(id) < 50 ) primary key,
    route_id text check ( length(route_id) < 50 )
);

create table if not exists vehicles
(
    id            text check ( length(id) < 50 ) primary key,
    license_plate text check ( length(license_plate) < 20 )
);

create table if not exists curr_vehicle_positions
(
--     vehicle_id text check ( length(vehicle_id) < 50 ) references vehicles (id) primary key,
--     trip_id    text check ( length(trip_id) < 50 ) references trips (id),
    vehicle_id text primary key,
    trip_id    text,
    position   geography(point),
    bearing    float,
    speed      float,
    created_on timestamp without time zone not null,
    updated_on timestamp without time zone
);

-- #revisit require later partitioning
-- #revisit require archiving -> aws glacier
create sequence if not exists hist_vehicle_positions_seq start with 1 increment by 80;
create table if not exists hist_vehicle_positions
(
    id         bigint primary key,
--     vehicle_id text check ( length(vehicle_id) < 50 ) references vehicles (id),
--     trip_id    text check ( length(trip_id) < 50 ) references trips (id),
    vehicle_id text,
    trip_id    text,
    position   geography(point),
    bearing    float,
    speed      float,
    created_on timestamp without time zone not null,
    updated_on timestamp without time zone
);

select *
from hist_vehicle_positions;

select * from curr_vehicle_positions;