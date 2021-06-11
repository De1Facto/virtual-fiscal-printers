package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslDailyReportCommand implements Command {
    private final Boolean noReset;
    private final Boolean shouldPrint;
    private final ISLFiscalPrinter printer;

    private IslDailyReportCommand(Boolean noReset, Boolean shouldPrint, ISLFiscalPrinter printer) {
        this.noReset = noReset;
        this.shouldPrint = shouldPrint;
        this.printer = printer;
    }

    public static IslDailyReportCommand fromString(String inputString,ISLFiscalPrinter printer) {
        if(inputString.length() < 1) throw new RuntimeException("INVALID DAILY REPORT FORMAT");
        boolean toPrint = true;
        if(inputString.length() > 1) toPrint = Boolean.parseBoolean(inputString.substring(1, 2));
        return new IslDailyReportCommand(
                Boolean.valueOf(inputString.substring(0,1)),
                toPrint,
                printer);
    }

    public Boolean getNoReset() {
        return noReset;
    }

    public Boolean getShouldPrint() {
        return shouldPrint;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
