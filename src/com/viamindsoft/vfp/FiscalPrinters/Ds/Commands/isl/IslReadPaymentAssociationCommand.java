package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslReadPaymentAssociationCommand implements Command {
    private final int paymentType;
    private final ISLFiscalPrinter printer;

    private IslReadPaymentAssociationCommand(int paymentType, ISLFiscalPrinter printer) {
        this.paymentType = paymentType;
        this.printer = printer;
    }

    public static IslReadPaymentAssociationCommand fromString(String s,ISLFiscalPrinter printer ) {
        return new IslReadPaymentAssociationCommand(Integer.parseInt(s),printer);
    }

    public int getPaymentType() {
        return paymentType;
    }
    @Override
    public void execute() {
        printer.execute(this);
    }
}
