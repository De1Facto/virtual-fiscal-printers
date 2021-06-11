package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.ISLFiscalPrinter;

import java.time.LocalDateTime;
import java.time.Month;

public class IslOpenReversalReceiptCommand implements IslCommand {
    private final short reason;
    private final long docNumber;
    private final LocalDateTime dateTime;
    private final int fmNumber;
    private final String uniqueSaleNumber;
    private final long invoiceNumber;

    private final ISLFiscalPrinter printer;

    private IslOpenReversalReceiptCommand(
            short reason,
            long docNumber,
            LocalDateTime dateTime,
            int fmNumber,
            String uniqueSaleNumber,
            long invoiceNumber,
            ISLFiscalPrinter printer) {
        this.reason = reason;
        this.docNumber = docNumber;
        this.dateTime = dateTime;
        this.fmNumber = fmNumber;
        this.uniqueSaleNumber = uniqueSaleNumber;
        this.invoiceNumber = invoiceNumber;
        this.printer = printer;
    }

    public static IslOpenReversalReceiptCommand fromString(String inputString, ISLFiscalPrinter receiver) {
        return new IslOpenReversalReceiptCommand(
                parseReason(inputString),
                parseLong(inputString.substring(1),10),
                parseDate(inputString.substring(11)),
                (int) parseLong(inputString.substring(25),8),
                inputString.substring(33,54),
                parseLong(inputString.substring(54),10),
                receiver
        );
    }
    private static short parseReason(String string) {
        short reason = Short.parseShort(string.substring(0,1));
        if( reason > 2 || reason < 0) throw new RuntimeException("INVALID REASON");
        return reason;
    }

    private static long parseLong(String string,int length) {
        if(string.length() < length) return 0L;
        return Long.parseLong(string.substring(0,length));
    }

    private static LocalDateTime parseDate(String string) {
        return LocalDateTime.of(
                Integer.parseInt(string.substring(15,19)),
                Month.of(Integer.parseInt(string.substring(13,15))),
                Integer.parseInt(string.substring(11,13)),
                Integer.parseInt(string.substring(19,21)),
                Integer.parseInt(string.substring(21,23)),
                Integer.parseInt(string.substring(23,25))
        );
    }

    public short getReason() {
        return reason;
    }

    public long getDocNumber() {
        return docNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getFmNumber() {
        return fmNumber;
    }

    public String getUniqueSaleNumber() {
        return uniqueSaleNumber;
    }

    public long getInvoiceNumber() {
        return invoiceNumber;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
