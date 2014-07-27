# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "Dad" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"name" VARCHAR(254) NOT NULL);
create table "Snapshot" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"timestamp" VARCHAR(254) NOT NULL,"file" VARCHAR(254) NOT NULL,"user" INTEGER NOT NULL,"commit" VARCHAR(254) NOT NULL,"lines" VARCHAR(254) NOT NULL);

# --- !Downs

drop table "Dad";
drop table "Snapshot";

