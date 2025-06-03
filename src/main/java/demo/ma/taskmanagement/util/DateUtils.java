package demo.ma.taskmanagement.util;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
    }

    public static boolean isOverdue(LocalDateTime dueDate) {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }

    public static boolean isDueSoon(LocalDateTime dueDate, int hoursThreshold) {
        if (dueDate == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().plusHours(hoursThreshold);
        return dueDate.isBefore(threshold) && dueDate.isAfter(LocalDateTime.now());
    }
}
