-- 1. Your 3 real PostgreSQL Enums
DO $$ BEGIN
    CREATE TYPE journal_stage AS ENUM ('PAYMENT_PENDING', 'STARTED', 'DONE', 'FAILED');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE role AS ENUM ('EMPLOYEE', 'ADMIN', 'MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR', 'ACCOUNTANT');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'VETOED');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE from_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'VETOED');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE to_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'VETOED');
EXCEPTION WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE current_stage AS ENUM ('PAYMENT_PENDING', 'STARTED', 'DONE', 'FAILED');
EXCEPTION WHEN duplicate_object THEN null;
END $$;