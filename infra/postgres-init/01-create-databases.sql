-- Runs once on first container start: create both service databases.
-- (POSTGRES_USER's default database "postgres" is used for neither service.)
CREATE DATABASE clinic_portal;
CREATE DATABASE clinic_notifications_db;
