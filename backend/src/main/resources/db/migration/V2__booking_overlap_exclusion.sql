-- Database-level guarantee against double-booking a doctor.
--
-- The application performs a friendly overlap check before inserting, but
-- that check is advisory: two concurrent transactions can both pass it.
-- This GiST exclusion constraint makes overlapping time ranges for the same
-- doctor impossible to commit. Cancelled and no-show appointments do not
-- block their slot.

CREATE EXTENSION IF NOT EXISTS btree_gist;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_constraint
                       WHERE conname = 'no_overlapping_appointments') THEN
            ALTER TABLE appointments
                ADD CONSTRAINT no_overlapping_appointments
                    EXCLUDE USING gist (
                    doctor_id WITH =,
                    tsrange(scheduled_at, scheduled_at + make_interval(mins => duration_minutes), '[)') WITH &&
                    )
                    WHERE (status NOT IN ('CANCELLED', 'NO_SHOW'));
        END IF;
    END
$$;
