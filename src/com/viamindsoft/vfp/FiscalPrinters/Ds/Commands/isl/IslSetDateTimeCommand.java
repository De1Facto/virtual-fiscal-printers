package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

import java.time.LocalDateTime;
import java.time.Month;

public class IslSetDateTimeCommand implements Command {
    private final LocalDateTime dateTime;

    private IslSetDateTimeCommand(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public static IslSetDateTimeCommand fromString(String s) {
        if(s.length() < 14) throw new RuntimeException("INVALID DATE TIME FORMAT");
        return new IslSetDateTimeCommand(
                LocalDateTime.of(
                      Integer.parseInt(s.substring(4,8)),
                        Month.of(Integer.parseInt(s.substring(2,4))),
                        Integer.parseInt(s.substring(0,2)),
                        Integer.parseInt(s.substring(8,10)),
                        Integer.parseInt(s.substring(10,12)),
                        Integer.parseInt(s.substring(12))
                )
        );
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
