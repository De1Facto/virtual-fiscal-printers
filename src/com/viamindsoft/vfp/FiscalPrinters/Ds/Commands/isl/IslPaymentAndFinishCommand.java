package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslPaymentAndFinishCommand implements Command {
    private final Short payment;
    private final Long amountGiven;
    private final Boolean flag;
    private final ISLFiscalPrinter printer;

    private IslPaymentAndFinishCommand(Short payment, Long amountGiven, Boolean flag, ISLFiscalPrinter printer) {
        this.payment = payment;
        this.amountGiven = amountGiven;
        this.flag = flag;
        this.printer = printer;
    }

    public static IslPaymentAndFinishCommand fromString(String inputString,ISLFiscalPrinter printer) {
        if(inputString.length() == 1)
            return defaultFinish(inputString,printer);
        if(inputString.length() < 13) throw new RuntimeException("INVALID FORMAT");
        return new IslPaymentAndFinishCommand(
                Short.parseShort(inputString.substring(0,1)),
                Long.parseLong(inputString.substring(1,12)),
                Boolean.parseBoolean(inputString.substring(12,13)),
                printer);
    }
    private static IslPaymentAndFinishCommand defaultFinish(String string,ISLFiscalPrinter printer) {
        return new IslPaymentAndFinishCommand(Short.parseShort(string),0L,false, printer);
    }

    public Short getPayment() {
        return payment;
    }

    public Long getAmountGiven() {
        return amountGiven;
    }

    public Boolean getFlag() {
        return flag;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
