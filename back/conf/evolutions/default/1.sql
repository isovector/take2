# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "Commit" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"branch" VARCHAR(254) NOT NULL,"name" VARCHAR(254) NOT NULL,"email" VARCHAR(254) NOT NULL);
create table "RepoFile" ("file" VARCHAR(254) PRIMARY KEY NOT NULL,"lastCommit" VARCHAR(254) NOT NULL,"lastUpdated" BIGINT NOT NULL);
create table "Snapshot" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"timestamp" BIGINT NOT NULL,"file" VARCHAR(254) NOT NULL,"user" INTEGER NOT NULL,"commit" VARCHAR(254) NOT NULL,"lines" TEXT NOT NULL);
create table "User" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"name" VARCHAR(254) NOT NULL,"email" VARCHAR(254) NOT NULL,"lastActivity" BIGINT NOT NULL);

# --- !Downs

drop table "Commit";
drop table "RepoFile";
drop table "Snapshot";
drop table "User";

