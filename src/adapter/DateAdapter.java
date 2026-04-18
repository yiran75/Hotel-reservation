package adapter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PATTERN 6 — ADAPTER  (Structural)
 * ─────────────────────────────────────────────────────────────────────────────
 * Intent
 *   Convert the interface of a class (java.util.Date from the JSpinner date
 *   picker) into another interface that clients expect (a clean, timezone-safe,
 *   file-storage-friendly representation).
 *
 * The incompatibility problem
 *   JSpinner/SpinnerDateModel returns java.util.Date, which carries:
 *     • Wall-clock time (hours, minutes, seconds, milliseconds)
 *     • The JVM's default timezone baked into the value
 *   When the same .dat file is opened on a machine in a different timezone,
 *   the deserialized Date shifts — a check-in of "2025-07-01" becomes
 *   "2025-06-30 23:00:00" in UTC-1.  For hotel bookings, the date (not the
 *   instant) is what matters.
 *
 * What the Adapter does
 *   1. Adaptee  — java.util.Date  (produced by JSpinner, timezone-sensitive)
 *   2. Target   — SerializableDate (date-only, timezone-free, fully Serializable)
 *   3. Adapter  — DateAdapter      (static methods toSerializable / toDate)
 *
 *   SerializableDate stores the date as three ints (year, month, day) which are
 *   completely immune to timezone drift.  It implements Serializable so it can
 *   replace java.util.Date in Reservation without breaking ObjectOutputStream.
 *
 * How to wire it in (ReservationForm.java / Reservation.java)
 *   // In ReservationForm — when reading from JSpinner before saving:
 *   Date rawDate = (Date) spnCheckIn.getValue();
 *   DateAdapter.SerializableDate safeDate = DateAdapter.toSerializable(rawDate);
 *
 *   // In Reservation model — store SerializableDate instead of Date:
 *   private DateAdapter.SerializableDate checkInDate;
 *
 *   // When populating the JSpinner from a loaded Reservation:
 *   spnCheckIn.setValue(DateAdapter.toDate(reservation.getCheckInDate()));
 *
 *   // For display in the JTable:
 *   String display = DateAdapter.format(safeDate);   // "2025-07-01"
 * ─────────────────────────────────────────────────────────────────────────────
 */
public final class DateAdapter {

    private static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateAdapter() { /* utility class — no instances */ }

    // ══════════════════════════════════════════════════════════════════════════
    // Target type — SerializableDate
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Immutable, timezone-free date value object.
     * Stores only year, month (1-12), and day — no time component, no timezone.
     * Safe for serialization to .dat files and comparison across JVMs.
     */
    public static final class SerializableDate implements Serializable, Comparable<SerializableDate> {
        private static final long serialVersionUID = 1L;

        private final int year;
        private final int month;  // 1 = January … 12 = December
        private final int day;

        public SerializableDate(int year, int month, int day) {
            if (month < 1 || month > 12) throw new IllegalArgumentException("month must be 1-12");
            if (day   < 1 || day   > 31) throw new IllegalArgumentException("day must be 1-31");
            this.year  = year;
            this.month = month;
            this.day   = day;
        }

        public int getYear()  { return year;  }
        public int getMonth() { return month; }
        public int getDay()   { return day;   }

        /** Convert to LocalDate for date arithmetic (e.g. calculating nights). */
        public LocalDate toLocalDate() {
            return LocalDate.of(year, month, day);
        }

        /** How many days between this date and another SerializableDate. */
        public long daysUntil(SerializableDate other) {
            return toLocalDate().until(other.toLocalDate()).getDays();
        }

        @Override
        public int compareTo(SerializableDate other) {
            return this.toLocalDate().compareTo(other.toLocalDate());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SerializableDate)) return false;
            SerializableDate d = (SerializableDate) o;
            return year == d.year && month == d.month && day == d.day;
        }

        @Override
        public int hashCode() {
            return year * 10000 + month * 100 + day;
        }

        @Override
        public String toString() {
            return String.format("%04d-%02d-%02d", year, month, day);
        }
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Adapter methods
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Adapt — Adaptee → Target.
     * Converts a java.util.Date (from JSpinner) to a timezone-safe SerializableDate.
     * Uses the system default zone only for interpretation; the result is zone-free.
     *
     * @param date java.util.Date from JSpinner getValue() — must not be null.
     * @return     A SerializableDate containing only the calendar date.
     */
    public static SerializableDate toSerializable(Date date) {
        if (date == null) throw new IllegalArgumentException("date must not be null");
        LocalDate local = date.toInstant()
                              .atZone(ZoneId.systemDefault())
                              .toLocalDate();
        return new SerializableDate(local.getYear(), local.getMonthValue(), local.getDayOfMonth());
    }

    /**
     * Adapt — Target → Adaptee.
     * Converts a SerializableDate back to a java.util.Date for loading into JSpinner.
     * Time is set to noon (12:00:00) to avoid DST edge cases near midnight.
     *
     * @param sd A SerializableDate — must not be null.
     * @return   A java.util.Date at noon on that calendar date, in the system default zone.
     */
    public static Date toDate(SerializableDate sd) {
        if (sd == null) throw new IllegalArgumentException("SerializableDate must not be null");
        return Date.from(
            sd.toLocalDate()
              .atTime(12, 0)
              .atZone(ZoneId.systemDefault())
              .toInstant()
        );
    }

    /**
     * Parse a "yyyy-MM-dd" string directly into a SerializableDate.
     * Useful for test data, import, and CSV parsing.
     *
     * @param isoDate  A string in "yyyy-MM-dd" format.
     * @return         The corresponding SerializableDate.
     * @throws IllegalArgumentException if the string is null, blank, or malformed.
     */
    public static SerializableDate parse(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            throw new IllegalArgumentException("isoDate must not be null or blank");
        }
        try {
            LocalDate ld = LocalDate.parse(isoDate.trim(), DISPLAY_FORMAT);
            return new SerializableDate(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Cannot parse date '" + isoDate + "' — expected format: yyyy-MM-dd", e);
        }
    }

    /**
     * Format a SerializableDate for display (e.g. in a JTable cell).
     *
     * @param sd A SerializableDate, or null.
     * @return   "yyyy-MM-dd" string, or "" if null.
     */
    public static String format(SerializableDate sd) {
        return (sd == null) ? "" : sd.toString();
    }

    /**
     * Format a legacy java.util.Date for display, converting through the adapter.
     * Convenience method for JTable renderers that still hold java.util.Date fields.
     *
     * @param date java.util.Date or null.
     * @return     "yyyy-MM-dd" string, or "" if null.
     */
    public static String format(Date date) {
        return (date == null) ? "" : format(toSerializable(date));
    }

    /**
     * Validate that a java.util.Date from JSpinner is safe to persist
     * (non-null, not the epoch 1970-01-01 which often signals an uninitialised spinner).
     *
     * @param date The Date to check.
     * @return     true if the date is plausible for a hotel booking.
     */
    public static boolean isSafeForStorage(Date date) {
        if (date == null) return false;
        SerializableDate sd = toSerializable(date);
        return sd.getYear() >= 2000 && sd.getYear() <= 2100;
    }
}
