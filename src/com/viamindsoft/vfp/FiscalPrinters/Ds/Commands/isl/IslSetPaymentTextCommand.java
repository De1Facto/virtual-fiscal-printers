package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslSetPaymentTextCommand implements Command {
    private final int paymentType;
    private final String paymentName;
    private final ISLFiscalPrinter printer;

    private IslSetPaymentTextCommand(int paymentType, String paymentName, ISLFiscalPrinter printer) {
        this.paymentType = paymentType;
        this.paymentName = paymentName;
        this.printer = printer;
    }

    public static IslSetPaymentTextCommand fromString(String s, ISLFiscalPrinter printer) {
        return new IslSetPaymentTextCommand(
                Integer.parseInt(s.substring(0,1)),
                s.substring(1),
                printer
        );
    }


    @Override
    public void execute() {
        printer.execute(this);
    }

    public int getPaymentType() {
        return paymentType;
    }

    public String getPaymentName() {
        return paymentName;
    }
}
