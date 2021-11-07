-- create role pass_owner
CREATE ROLE pass_owner INHERIT LOGIN ENCRYPTED PASSWORD 'H7BFiLRfW060oiHd';

-- create role pass_user
CREATE ROLE pass_user INHERIT LOGIN ENCRYPTED PASSWORD 'PhhxMVloMYIpAMXL';

-- create database personal_pass
CREATE DATABASE personal_pass WITH OWNER pass_owner ENCODING 'UTF8' LC_COLLATE 'pl_PL.UTF-8' LC_CTYPE 'pl_PL.UTF-8';

-- switch to personal_pass database
\c personal_pass postgres

-- create schema extensions
CREATE SCHEMA IF NOT EXISTS extensions AUTHORIZATION pass_owner;

-- install uuid-ossp extensions
CREATE EXTENSION "uuid-ossp" SCHEMA extensions;

-- create schema integration
CREATE SCHEMA IF NOT EXISTS integration AUTHORIZATION pass_owner;

-- grant connect privilege to pass_user
GRANT CONNECT,TEMPORARY ON DATABASE personal_pass TO pass_user;

-- grant usage privilege on schema extensions to pass_user
GRANT USAGE ON SCHEMA extensions TO pass_user;

-- grant usage privilege on schema integration to pass_user
GRANT USAGE ON SCHEMA integration TO pass_user;

-- grant default privileges for tables created in schema integration by pass_owner to pass_user
ALTER DEFAULT PRIVILEGES FOR USER pass_owner IN SCHEMA integration GRANT SELECT,INSERT,UPDATE,DELETE ON TABLES TO pass_user;

-- grant default privileges for sequences created in schema integration by pass_owner to pass_user
ALTER DEFAULT PRIVILEGES FOR USER pass_owner IN SCHEMA integration GRANT USAGE ON SEQUENCES TO pass_user