package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

import java.time.LocalDateTime;
import java.time.Month;

public class IslSetDateTimeCommand implements Command {
    private final LocalDateTime dateTime;
    private final ISLFiscalPrinter printer;

    private IslSetDateTimeCommand(LocalDateTime dateTime, ISLFiscalPrinter printer) {
        this.dateTime = dateTime;
        this.printer = printer;
    }

    public static IslSetDateTimeCommand fromString(String s, ISLFiscalPrinter printer) {
        if(s.length() < 12) throw new RuntimeException("INVALID DATE TIME FORMAT");
        int year = 2000 + Integer.parseInt(s.substring(4,6));
        return new IslSetDateTimeCommand(
                LocalDateTime.of(
                      year,
                        Month.of(Integer.parseInt(s.substring(2,4))),
                        Integer.parseInt(s.substring(0,2)),
                        Integer.parseInt(s.substring(6,8)),
                        Integer.parseInt(s.substring(8,10)),
                        Integer.parseInt(s.substring(10))
                ),
                printer);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
