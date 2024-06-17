package superapp.kr_superapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

//    /**
//     * Парсит строку даты в миллисекунды с момента эпохи.
//     *
//     * Parses a date string to milliseconds since epoch.
//     *
//     * @param dateString строка даты / date string
//     * @throws ParseException если строка даты не может быть распознана / if the date string cannot be parsed
//     */
//    public static long parseDate(String dateString) throws ParseException {
//        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
//        Date date = format.parse(dateString);
//        return date.getTime();
//    }

    public static String formatDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date(date));
    }
}
