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
        if(s.length() < 14) throw new RuntimeException("INVALID DATE TIME FORMAT");
        return new IslSetDateTimeCommand(
                LocalDateTime.of(
                      Integer.parseInt(s.substring(4,8)),
                        Month.of(Integer.parseInt(s.substring(2,4))),
                        Integer.parseInt(s.substring(0,2)),
                        Integer.parseInt(s.substring(8,10)),
                        Integer.parseInt(s.substring(10,12)),
                        Integer.parseInt(s.substring(12))
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
