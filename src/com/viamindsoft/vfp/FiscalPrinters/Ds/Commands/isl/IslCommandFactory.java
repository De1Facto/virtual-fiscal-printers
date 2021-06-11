package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.CommandFactory;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.UnIdentifiedCommand;

public class IslCommandFactory implements CommandFactory {

    @Override
    public Command createCommand(String inputString)  {
        String stripped = stripToCommandAndData(inputString);
        return parseCommand(stripped);
    }

    private String stripToCommandAndData(String string) {
        return string.substring(5,string.length()-1);
    }

    private Command parseCommand(String strippedCommand) {
        String commandString = strippedCommand.substring(0,2);
        strippedCommand = strippedCommand.substring(2);
        switch (commandString) {
            case "00": return new IslPrintNumCommand();
            case "20": return IslOpenReversalReceiptCommand.fromString(strippedCommand);
            case "24": return IslItemReversalCommand.fromString(strippedCommand);
            case "44": return IslItemSaleCommand.fromString(strippedCommand);
            case "45": return IslVoidCommand.fromString(strippedCommand);
            case "46": return IslValueDiscountOrSurchargeCommand.fromString(strippedCommand);
            case "47": return IslPercentDiscountOrSurcharge.fromString(strippedCommand);
            case "48": return new IslSubtotalCommand();
            case "49": return IslPaymentAndFinishCommand.fromString(strippedCommand);
            case "50": return new UnIdentifiedCommand(); //TODO MAKE INVOICE STUFF
            case "51": return IslDailyReportCommand.fromString(strippedCommand);
            case "55": return new UnIdentifiedCommand(); //TODO MAKE FMREP
            case "56": return new UnIdentifiedCommand(); //TODO MAKE FMREP
            case "61": return IslDepositOrWithdrawMoneyCommand.fromString(strippedCommand);
            case "73": return IslSetDateTimeCommand.fromString(strippedCommand);
            case "75": return IslSetOperatorCommand.fromString(strippedCommand);
            case "81": return IslPrintCommentCommand.fromString(strippedCommand);
            case "A8": return new IslKlenReportCommand();
            case "AA": return new IslLastReceiptCopyCommand();
            case "B2": return new IslClearErrorsCommand();
            case "F3": return new IslGetDateTimeCommand();
            case "F8": return parseFpStatusCommand(strippedCommand);
            default: return new UnIdentifiedCommand();
        }
    }

    private Command parseFpStatusCommand(String s) {
        switch (s.substring(0,2)) {
            case "00": return new IslKlenStatusCommand();
            case "01": return new IslLastReceiptNumberAndSubtotalCommand();
            case "02": return new IslLastKlenNumAndFMRecordCommand();
            case "03": return new IslRevenueByTaxGroupsCommand();
            case "04": return new IslFMTotalRevenueCommand();
            case "05": return new IslDiscountsAndSurchargesTotalCommand();
            case "06": return new UnIdentifiedCommand(); // TODO IF NEEDED
            case "07": return new IslPrinterVersionCommand();
            case "08": return new IslBatteryStatusCommand();
            case "09": return new IslCurrentErrorCommand();
            case "0A": return new UnIdentifiedCommand();
            case "0B": return new IslCurrentReceiptCurrentTotalCommand();
            case "0C": return new IslBitwiseStatusCommand();
            default: return new UnIdentifiedCommand();
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
