package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslPrintCommentCommand implements Command {
    private final String uniqueSaleNumber;
    private final String comment;
    private final ISLFiscalPrinter printer;

    private IslPrintCommentCommand(String uniqueSaleNumber, String comment, ISLFiscalPrinter printer) {
        this.uniqueSaleNumber = uniqueSaleNumber;
        this.comment = comment;
        this.printer = printer;
    }

    public static IslPrintCommentCommand fromString(String s, ISLFiscalPrinter printer) {
        if(s.length() < 21) throw new RuntimeException("INVALID COMMENT FORMAT");
        return new IslPrintCommentCommand(
                s.substring(0,21),
                s.substring(21),
                printer);
    }

    public String getUniqueSaleNumber() {
        return uniqueSaleNumber;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
