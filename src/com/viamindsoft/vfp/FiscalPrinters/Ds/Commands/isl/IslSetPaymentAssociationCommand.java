package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslSetPaymentAssociationCommand implements Command {
    private final int paymentType;
    private final int association;
    private final ISLFiscalPrinter printer;

    private IslSetPaymentAssociationCommand(int paymentType, int association, ISLFiscalPrinter printer) {
        this.paymentType = paymentType;
        this.association = association;
        this.printer = printer;
    }

    public static IslSetPaymentAssociationCommand fromString(String s, ISLFiscalPrinter printer) {
        return new IslSetPaymentAssociationCommand(
                Integer.parseInt(s.substring(0,2)),
                Integer.parseInt(s.substring(2,4)),
                printer
        );
    }

    public int getPaymentType() {
        return paymentType;
    }

    public int getAssociation() {
        return association;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
