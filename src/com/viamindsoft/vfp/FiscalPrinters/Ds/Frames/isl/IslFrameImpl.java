package com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.DataResponse;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.Response;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.Frame;

import java.nio.charset.StandardCharsets;

public class IslFrameImpl implements IslFrame {

    private final String frameString;

    private IslFrameImpl(String frameString) {
        this.frameString = frameString;
    }

    public static Frame fromResponse(Response response) {
        return response instanceof DataResponse
                ? new IslFrameImpl(format(response.toString()))
                : new IslFrameImpl(response.toString());
    }

    private static String format(String frame) {
        return addLengthAndChecksum(STX + frame) + ETX;
    }

    private static String addLengthAndChecksum(String frame) {
        if(frame.length() == 1) return frame;
        frame = addLength(frame);
        return addChecksum(frame);
    }

    private static String addLength(String frame) {
        return frame + makeTwoByteFormatForLengthOrChecksum(frame.length() + 5);
    }

    private static String addChecksum(String frame) {
        int sum = 2;
        for(var i = 0 ; i < frame.length(); i++) {
            sum += frame.charAt(0);
        }
        sum &= 255;
        return frame + makeTwoByteFormatForLengthOrChecksum(sum);
    }

    private static String makeTwoByteFormatForLengthOrChecksum(int target) {
        char firstByte = 0x30;
        char lastByte = 0x30;
        if(Integer.toString(target).length() > 1) {
            firstByte += Integer.toString(target).charAt(0);
            lastByte += Integer.toString(target).charAt(1);
        } else {
            lastByte += Integer.toString(target).charAt(0);
        }
        return String.valueOf(new char[]{firstByte,lastByte});
    }

    @Override
    public String toString() {
        return frameString;
    }

    @Override
    public byte[] toBytes() {
        return frameString.getBytes(StandardCharsets.UTF_8);
    }
}
