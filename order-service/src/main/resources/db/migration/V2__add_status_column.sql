-- Backward-compatible schema evolution: add column with a default value
-- Ensures older versions of the app can still read and write without failures
ALTER TABLE orders ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PENDING' NOT NULL;
