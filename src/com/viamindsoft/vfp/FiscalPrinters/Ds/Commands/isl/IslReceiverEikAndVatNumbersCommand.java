package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

import javax.management.InvalidAttributeValueException;

public class IslReceiverEikAndVatNumbersCommand implements Command {
    private final String eik;
    private final String vatNumber;
    private final ISLFiscalPrinter printer;

    private IslReceiverEikAndVatNumbersCommand(String eik, String vatNumber, ISLFiscalPrinter printer) {
        this.eik = eik;
        this.vatNumber = vatNumber;
        this.printer = printer;
    }

    public static IslReceiverEikAndVatNumbersCommand factory(String eik, String vatNumber, ISLFiscalPrinter printer) {
        if(eik.length() != 12) throw new RuntimeException("EIK MUST BE EXACTLY 12 CHARS");
        if(vatNumber.length() != 13) throw new RuntimeException("EIK MUST BE EXACTLY 13 CHARS");
        return new IslReceiverEikAndVatNumbersCommand(eik,vatNumber,printer);
    }

    @Override
    public void execute() {
        printer.execute(this);
    }

    public String getEik() {
        return eik;
    }

    public String getVatNumber() {
        return vatNumber;
    }
}
