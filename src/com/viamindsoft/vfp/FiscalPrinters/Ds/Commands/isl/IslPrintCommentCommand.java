package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslPrintCommentCommand implements Command {
    private final String uniqueSaleNumber;
    private final String comment;

    private IslPrintCommentCommand(String uniqueSaleNumber, String comment) {
        this.uniqueSaleNumber = uniqueSaleNumber;
        this.comment = comment;
    }

    public static IslPrintCommentCommand fromString(String s) {
        if(s.length() < 21) throw new RuntimeException("INVALID COMMENT FORMAT");
        return new IslPrintCommentCommand(
                s.substring(0,21),
                s.substring(21)
        );
    }

    public String getUniqueSaleNumber() {
        return uniqueSaleNumber;
    }

    public String getComment() {
        return comment;
    }
}
