package com.example.chat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for time and date formatting
 */
public class TimeUtils {

    private static final long MINUTE_MILLIS = 60 * 1000;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long WEEK_MILLIS = 7 * DAY_MILLIS;

    /**
     * Get relative time string (e.g., "2 minutes ago", "Yesterday", "Last week")
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "";

        try {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < MINUTE_MILLIS) {
                return "Just now";
            } else if (diff < HOUR_MILLIS) {
                int minutes = (int) (diff / MINUTE_MILLIS);
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else if (diff < DAY_MILLIS) {
                int hours = (int) (diff / HOUR_MILLIS);
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (diff < 2 * DAY_MILLIS) {
                return "Yesterday";
            } else if (diff < WEEK_MILLIS) {
                int days = (int) (diff / DAY_MILLIS);
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else if (diff < 2 * WEEK_MILLIS) {
                return "Last week";
            } else {
                // More than 2 weeks ago, show date
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());

                // Check if it's this year
                Calendar now = Calendar.getInstance();
                Calendar then = Calendar.getInstance();
                then.setTime(date);

                if (now.get(Calendar.YEAR) != then.get(Calendar.YEAR)) {
                    sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                }

                return sdf.format(date);
            }
        } catch (Exception e) {
            // Fallback to simple date format
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    /**
     * Get time for message display (e.g., "14:30", "Yesterday 14:30", "Dec 25")
     */
    public static String getMessageTime(Date date) {
        if (date == null) return "";

        try {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            if (diff < DAY_MILLIS) {
                // Today - just show time
                return timeFormat.format(date);
            } else if (diff < 2 * DAY_MILLIS) {
                // Yesterday
                return "Yesterday " + timeFormat.format(date);
            } else if (diff < WEEK_MILLIS) {
                // This week - show day and time
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE HH:mm", Locale.getDefault());
                return dayFormat.format(date);
            } else {
                // Older - show date
                Calendar now = Calendar.getInstance();
                Calendar then = Calendar.getInstance();
                then.setTime(date);

                if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
                    // Same year
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    return dateFormat.format(date);
                } else {
                    // Different year
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    return dateFormat.format(date);
                }
            }
        } catch (Exception e) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault());
            return sdf.format(date);
        }
    }

    /**
     * Get conversation time for chat list (e.g., "14:30", "Yesterday", "Dec 25")
     */
    public static String getConversationTime(Date date) {
        if (date == null) return "";

        try {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < MINUTE_MILLIS) {
                return "Now";
            } else if (diff < DAY_MILLIS) {
                // Today - show time
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return timeFormat.format(date);
            } else if (diff < 2 * DAY_MILLIS) {
                return "Yesterday";
            } else if (diff < WEEK_MILLIS) {
                // This week - show day
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                return dayFormat.format(date);
            } else {
                // Older - show date
                Calendar now = Calendar.getInstance();
                Calendar then = Calendar.getInstance();
                then.setTime(date);

                if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
                    // Same year
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    return dateFormat.format(date);
                } else {
                    // Different year
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    return dateFormat.format(date);
                }
            }
        } catch (Exception e) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(date);
        }
    }

    /**
     * Get date separator text for message list
     */
    public static String getDateSeparator(Date date) {
        if (date == null) return "";

        try {
            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < DAY_MILLIS) {
                return "Today";
            } else if (diff < 2 * DAY_MILLIS) {
                return "Yesterday";
            } else {
                Calendar now = Calendar.getInstance();
                Calendar then = Calendar.getInstance();
                then.setTime(date);

                if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
                    // Same year
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
                    return dateFormat.format(date);
                } else {
                    // Different year
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
                    return dateFormat.format(date);
                }
            }
        } catch (Exception e) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    /**
     * Check if two dates are on the same day
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(Date date) {
        if (date == null) return false;

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        return isSameDay(date, yesterday.getTime());
    }

    /**
     * Check if date is within the last week
     */
    public static boolean isThisWeek(Date date) {
        if (date == null) return false;

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        return diff < WEEK_MILLIS;
    }

    /**
     * Format time for input fields (24-hour format)
     */
    public static String formatTime24(Date date) {
        if (date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format time for input fields (12-hour format)
     */
    public static String formatTime12(Date date) {
        if (date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format date for input fields
     */
    public static String formatDate(Date date) {
        if (date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format date and time for display
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Parse date from string
     */
    public static Date parseDate(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) return null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get current timestamp
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Convert timestamp to date
     */
    public static Date timestampToDate(long timestamp) {
        return new Date(timestamp);
    }

    /**
     * Get online status text based on last seen
     */
    public static String getOnlineStatus(Date lastSeen) {
        if (lastSeen == null) return "Offline";

        long now = System.currentTimeMillis();
        long time = lastSeen.getTime();
        long diff = now - time;

        if (diff < 5 * MINUTE_MILLIS) {
            return "Online";
        } else if (diff < HOUR_MILLIS) {
            int minutes = (int) (diff / MINUTE_MILLIS);
            return "Last seen " + minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (diff < DAY_MILLIS) {
            int hours = (int) (diff / HOUR_MILLIS);
            return "Last seen " + hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (diff < WEEK_MILLIS) {
            int days = (int) (diff / DAY_MILLIS);
            return "Last seen " + days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return "Last seen long time ago";
        }
    }
}