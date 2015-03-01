# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "Change" ("id" INTEGER PRIMARY KEY NOT NULL,"user" INTEGER NOT NULL,"file" VARCHAR(254) NOT NULL,"adds" INTEGER NOT NULL,"dels" INTEGER NOT NULL);
create table "Cluster" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"user" INTEGER NOT NULL,"created" BIGINT NOT NULL,"snapshots" TEXT NOT NULL,"files" TEXT NOT NULL);
create table "Coefficient" ("id" VARCHAR(254) PRIMARY KEY NOT NULL,"clusterCount" INTEGER NOT NULL,"totalCount" INTEGER NOT NULL);
create table "Commit" ("id" VARCHAR(254) PRIMARY KEY NOT NULL,"branch" VARCHAR(254) NOT NULL,"rawParents" TEXT NOT NULL);
create table "Memcache" ("id" VARCHAR(254) PRIMARY KEY NOT NULL,"value" VARCHAR(254) NOT NULL);
create table "RepoFile" ("file" VARCHAR(254) PRIMARY KEY NOT NULL,"lastCommit" VARCHAR(254) NOT NULL,"lastUpdated" BIGINT NOT NULL,"adds" INTEGER NOT NULL,"dels" INTEGER NOT NULL);
create table "Snapshot" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"timestamp" BIGINT NOT NULL,"file" VARCHAR(254) NOT NULL,"user" INTEGER NOT NULL,"commitId" VARCHAR(254) NOT NULL,"symbols" TEXT NOT NULL);
create table "Symbol" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"file" VARCHAR(254) NOT NULL,"name" VARCHAR(254) NOT NULL,"line" INTEGER NOT NULL,"kind" VARCHAR(254) NOT NULL);
create table "User" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"name" VARCHAR(254) NOT NULL,"email" VARCHAR(254) NOT NULL,"lastActivity" BIGINT NOT NULL);

# --- !Downs

drop table "Change";
drop table "Cluster";
drop table "Coefficient";
drop table "Commit";
drop table "Memcache";
drop table "RepoFile";
drop table "Snapshot";
drop table "Symbol";
drop table "User";

