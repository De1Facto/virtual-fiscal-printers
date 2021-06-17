package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class ISLInvoiceReceiverStandardTextFieldCommand implements Command {
    private final String text;
    private final ISLFiscalPrinter printer;

    private ISLInvoiceReceiverStandardTextFieldCommand(String name, ISLFiscalPrinter printer) {
        this.text = name;
        this.printer = printer;
    }

    public static ISLInvoiceReceiverStandardTextFieldCommand factory(String name, ISLFiscalPrinter printer) {
        if(name.length() < 1 || name.length() > 35)
            throw new RuntimeException("INVALID NAME LENGTH");
        return new ISLInvoiceReceiverStandardTextFieldCommand(name,printer);
    }

    @Override
    public void execute() {
        printer.execute(this);
    }

    public String getText() {
        return text;
    }
}
