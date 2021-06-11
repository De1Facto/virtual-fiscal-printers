package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslDailyReportCommand implements Command {
    private final Boolean noReset;
    private final Boolean shouldPrint;

    private IslDailyReportCommand(Boolean noReset, Boolean shouldPrint) {
        this.noReset = noReset;
        this.shouldPrint = shouldPrint;
    }

    public static IslDailyReportCommand fromString(String inputString) {
        if(inputString.length() < 1) throw new RuntimeException("INVALID DAILY REPORT FORMAT");
        boolean toPrint = true;
        if(inputString.length() > 1) toPrint = Boolean.parseBoolean(inputString.substring(1, 2));
        return new IslDailyReportCommand(
                Boolean.valueOf(inputString.substring(0,1)),
                toPrint
        );
    }

    public Boolean getNoReset() {
        return noReset;
    }

    public Boolean getShouldPrint() {
        return shouldPrint;
    }
}
