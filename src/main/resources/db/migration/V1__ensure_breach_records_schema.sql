-- Ensure breach_records table exists with required columns
CREATE TABLE IF NOT EXISTS breach_records (
    id UUID PRIMARY KEY,
    scan_record_id UUID NOT NULL,
    source_name TEXT NOT NULL,
    exposed_data TEXT NOT NULL,
    breach_date TIMESTAMPTZ NOT NULL,
    added_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    pwn_count BIGINT,
    description TEXT
);

-- Create index and FK if not present
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE schemaname = current_schema() AND indexname = 'idx_breach_records_scan_record_id'
    ) THEN
        CREATE INDEX idx_breach_records_scan_record_id ON breach_records (scan_record_id);
    END IF;
END $$;

-- Add foreign key constraint if missing
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'breach_records'
          AND tc.constraint_type = 'FOREIGN KEY'
          AND tc.constraint_name = 'fk_breach_records_scan_record'
    ) THEN
        ALTER TABLE breach_records
            ADD CONSTRAINT fk_breach_records_scan_record
            FOREIGN KEY (scan_record_id)
            REFERENCES scan_records(id)
            ON DELETE CASCADE;
    END IF;
END $$;

-- Ensure added_date column exists if table pre-existed without it
ALTER TABLE breach_records
    ADD COLUMN IF NOT EXISTS added_date TIMESTAMPTZ NOT NULL DEFAULT NOW();

