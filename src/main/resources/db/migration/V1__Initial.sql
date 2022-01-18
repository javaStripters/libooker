create extension if not exists pg_trgm;

create sequence hibernate_sequence;

create table day_off (
                         date date not null,
                         primary key (date)
);

create table users (
                       id int8 not null,
                       created_at timestamp,
                       updated_at timestamp,
                       password text not null,
                       role varchar(255) not null,
                       username text not null unique,
                       primary key (id)
);

create table student (
                         id int8 not null,
                         created_at timestamp,
                         updated_at timestamp,
                         password text not null,
                         role varchar(255) not null,
                         username text not null unique,
                         firstname text,
                         lastname text,
                         patronymic text,
                         testbook text,
                         primary key (id)
);

create table workplace (
                           id int8 not null,
                           created_at timestamp,
                           updated_at timestamp,
                           name text not null unique,
                           primary key (id)
);

create table booking (
                         id int8 not null,
                         created_at timestamp,
                         updated_at timestamp,
                         canceled boolean not null,
                         date date not null,
                         end_time time not null,
                         finished_manually boolean not null,
                         start_time time not null,
                         user_id int8 not null,
                         workplace_id int8 not null references workplace(id),
                         primary key (id)
);

create index student_username_hash on student using hash(username);
create index student_trgm on student using gist(firstname gist_trgm_ops, lastname gist_trgm_ops, patronymic gist_trgm_ops, testbook gist_trgm_ops);