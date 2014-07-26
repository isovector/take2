# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "Dad" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"name" VARCHAR(254) NOT NULL);

# --- !Downs

drop table "Dad";


