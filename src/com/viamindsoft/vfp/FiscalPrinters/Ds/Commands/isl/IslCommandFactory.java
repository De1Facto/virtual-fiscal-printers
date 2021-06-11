package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.CommandFactory;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.UnIdentifiedCommand;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslCommandFactory implements CommandFactory {

    private final ISLFiscalPrinter receiver;

    public IslCommandFactory(ISLFiscalPrinter receiver) {
        this.receiver = receiver;
    }

    @Override
    public Command createCommand(String inputString)  {
        String stripped = stripToCommandAndData(inputString);
        return parseCommand(stripped);
    }

    private String stripToCommandAndData(String string) {
        return string.substring(5,string.length()-5);
    }

    private Command parseCommand(String strippedCommand) {
        String commandString = strippedCommand.substring(0,2);
        strippedCommand = strippedCommand.substring(2);
        switch (commandString) {
            case "00": return new IslPrintNumCommand(receiver);
            case "20": return IslOpenReversalReceiptCommand.fromString(strippedCommand,receiver);
            case "24": return IslItemReversalCommand.fromString(strippedCommand,receiver);
            case "44": return IslItemSaleCommand.fromString(strippedCommand,receiver);
            case "45": return IslVoidCommand.fromString(strippedCommand,receiver);
            case "46": return IslValueDiscountOrSurchargeCommand.fromString(strippedCommand,receiver);
            case "47": return IslPercentDiscountOrSurcharge.fromString(strippedCommand,receiver);
            case "48": return new IslSubtotalCommand(receiver);
            case "49": return IslPaymentAndFinishCommand.fromString(strippedCommand,receiver);
            case "50": return new UnIdentifiedCommand(receiver); //TODO MAKE INVOICE STUFF
            case "51": return IslDailyReportCommand.fromString(strippedCommand,receiver);
            case "55": return new UnIdentifiedCommand(receiver); //TODO MAKE FMREP
            case "56": return new UnIdentifiedCommand(receiver); //TODO MAKE FMREP
            case "61": return IslDepositOrWithdrawMoneyCommand.fromString(strippedCommand,receiver);
            case "73": return IslSetDateTimeCommand.fromString(strippedCommand,receiver);
            case "75": return IslSetOperatorCommand.fromString(strippedCommand,receiver);
            case "81": return IslPrintCommentCommand.fromString(strippedCommand,receiver);
            case "A8": return new IslKlenReportCommand(receiver);
            case "AA": return new IslLastReceiptCopyCommand(receiver);
            case "B2": return new IslClearErrorsCommand(receiver);
            case "F3": return new IslGetDateTimeCommand(receiver);
            case "F8": return parseFpStatusCommand(strippedCommand);
            default: return new UnIdentifiedCommand(receiver);
        }
    }

    private Command parseFpStatusCommand(String s) {
        switch (s.substring(0,2)) {
            case "00": return new IslKlenStatusCommand(receiver);
            case "01": return new IslLastReceiptNumberAndSubtotalCommand(receiver);
            case "02": return new IslLastKlenNumAndFMRecordCommand(receiver);
            case "03": return new IslRevenueByTaxGroupsCommand(receiver);
            case "04": return new IslFMTotalRevenueCommand(receiver);
            case "05": return new IslDiscountsAndSurchargesTotalCommand(receiver);
            case "06": return new UnIdentifiedCommand(receiver); // TODO IF NEEDED
            case "07": return new IslPrinterVersionCommand(receiver);
            case "08": return new IslBatteryStatusCommand(receiver);
            case "09": return new IslCurrentErrorCommand(receiver);
            case "0A": return new UnIdentifiedCommand(receiver);
            case "0B": return new IslCurrentReceiptCurrentTotalCommand(receiver);
            case "0C": return new IslBitwiseStatusCommand(receiver);
            default: return new UnIdentifiedCommand(receiver);
        }
    }

    @Override
    public Command createCommand(byte[] inputBytes) {
        return createCommand(convertToString(inputBytes));
    }
    private String convertToString(byte[] inputBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte inputByte : inputBytes) {
            sb.append((char) inputByte);
        }
        return sb.toString();
    }
}
