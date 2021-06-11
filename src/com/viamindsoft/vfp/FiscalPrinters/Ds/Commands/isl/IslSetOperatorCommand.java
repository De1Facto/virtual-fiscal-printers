package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslSetOperatorCommand implements Command {
    private final String operatorName;
    private final Short operatorCode;
    private final ISLFiscalPrinter printer;

    private IslSetOperatorCommand(String operatorName, Short operatorCode, ISLFiscalPrinter printer) {
        this.operatorName = operatorName;
        this.operatorCode = operatorCode;
        this.printer = printer;
    }

    public static IslSetOperatorCommand fromString(String s,ISLFiscalPrinter printer) {
        if(s.length() < 14) throw new RuntimeException("INVALID SET OPERATOR FORMAT");
        return new IslSetOperatorCommand(
                s.substring(0,12),
                Short.parseShort(s.substring(12)),
                printer);
    }

    public String getOperatorName() {
        return operatorName;
    }

    public Short getOperatorCode() {
        return operatorCode;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
