package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;


import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslBitwiseStatusCommand implements Command {
    private final ISLFiscalPrinter printer;

    public IslBitwiseStatusCommand(ISLFiscalPrinter printer) {
        this.printer = printer;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
