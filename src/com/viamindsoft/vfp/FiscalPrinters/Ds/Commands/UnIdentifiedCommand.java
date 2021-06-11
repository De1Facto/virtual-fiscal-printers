package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands;

import com.viamindsoft.vfp.ISLFiscalPrinter;

public class UnIdentifiedCommand implements Command {
    private final ISLFiscalPrinter printer;

    public UnIdentifiedCommand(ISLFiscalPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
