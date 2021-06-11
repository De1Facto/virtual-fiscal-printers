package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslSetOperatorCommand implements Command {
    private final String operatorName;
    private final Short operatorCode;

    private IslSetOperatorCommand(String operatorName, Short operatorCode) {
        this.operatorName = operatorName;
        this.operatorCode = operatorCode;
    }

    public static IslSetOperatorCommand fromString(String s) {
        if(s.length() < 14) throw new RuntimeException("INVALID SET OPERATOR FORMAT");
        return new IslSetOperatorCommand(
                s.substring(0,12),
                Short.parseShort(s.substring(12))
        );
    }

    public String getOperatorName() {
        return operatorName;
    }

    public Short getOperatorCode() {
        return operatorCode;
    }
}
