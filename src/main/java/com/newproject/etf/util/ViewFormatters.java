package com.newproject.etf.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ViewFormatters {
    private static final Locale KOREA = Locale.KOREA;
    private static final NumberFormat INT_FMT = NumberFormat.getIntegerInstance(KOREA);
    private static final NumberFormat DEC2_FMT;
    static {
        DEC2_FMT = NumberFormat.getNumberInstance(KOREA);
        DEC2_FMT.setMinimumFractionDigits(2);
        DEC2_FMT.setMaximumFractionDigits(2);
    }
    private static final DateTimeFormatter YMD_NOSEP = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YMD_DOTS  = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private ViewFormatters() {}

    /** 1) 원 단위 콤마 */
    public static String formatWon(Long amount) {
        if (amount == null) return "-";
        return INT_FMT.format(amount) + "원";
    }

    /** 2) 원 단위, 소수 2자리 (예: NAV) */
    public static String formatWon(BigDecimal amount) {
        if (amount == null) return "-";
        return DEC2_FMT.format(amount) + "원";
    }

    /** 3) 억/조 단위로 예쁘게 (정책은 필요에 맞게 수정) */
    public static String formatKrwUnit(Long amount) {
        if (amount == null) return "-";
        final long JO  = 1_0000_0000_0000L;  // 1조
        final long EOK =   100_000_000L;     // 1억
        if (amount >= JO) {
            double v = amount / (double) JO;
            return DEC2_FMT.format(v) + "조 원";
        } else if (amount >= EOK) {
            double v = amount / (double) EOK;
            return DEC2_FMT.format(v) + "억 원";
        } else {
            return INT_FMT.format(amount) + "원";
        }
    }

    /** 4) 'yyyyMMdd' → 'yyyy.MM.dd' */
    public static String formatBasDt(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) return yyyymmdd != null ? yyyymmdd : "-";
        LocalDate d = LocalDate.parse(yyyymmdd, YMD_NOSEP);
        return d.format(YMD_DOTS);
    }
}
