package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslVoidCommand implements Command {
    private final Boolean voidJustLast;
    private final Long quantity;
    private final ISLFiscalPrinter printer;

    private IslVoidCommand(Boolean voidJustLast, Long quantity, ISLFiscalPrinter printer) {
        this.voidJustLast = voidJustLast;
        this.quantity = quantity;
        this.printer = printer;
    }

    public static IslVoidCommand fromString(String inputString,ISLFiscalPrinter printer) {
        long quantity = Long.MAX_VALUE;
        if(inputString.length() > 1) {
            quantity = Long.parseLong(inputString.substring(1));
        }
        return new IslVoidCommand(
                Boolean.valueOf(inputString.substring(0,1)),
                quantity,
                printer
        );
    }

    public Boolean getVoidJustLast() {
        return voidJustLast;
    }

    public Long getQuantity() {
        return quantity;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
