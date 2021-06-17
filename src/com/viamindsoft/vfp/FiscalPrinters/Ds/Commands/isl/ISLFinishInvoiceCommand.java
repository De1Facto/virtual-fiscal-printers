package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class ISLFinishInvoiceCommand implements Command {

    private final ISLFiscalPrinter printer;

    public ISLFinishInvoiceCommand(ISLFiscalPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
