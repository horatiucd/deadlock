drop table if exists entity1 cascade;

create table if not exists entity1(
    id serial primary key,
    text varchar not null
);

drop table if exists entity2 cascade;

create table if not exists entity2(
    id serial primary key,
    text varchar not null
);
