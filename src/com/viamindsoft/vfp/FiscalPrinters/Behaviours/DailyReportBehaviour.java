package com.viamindsoft.vfp.FiscalPrinters.Behaviours;

import java.io.IOException;

public interface DailyReportBehaviour {
    void dailyReport(boolean resetStats) throws IOException;
}
