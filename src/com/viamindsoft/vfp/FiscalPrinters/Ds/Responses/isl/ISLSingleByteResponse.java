package com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.SingleByteResponse;

import java.nio.charset.StandardCharsets;

public class ISLSingleByteResponse implements SingleByteResponse {
    private static final char ACK = 0x06;
    private static final char NACK = 0x15;
    private static final char WAIT = 0x05;
    private final String response;

    private ISLSingleByteResponse(String response) {
        this.response = response;
    }

    public static SingleByteResponse ack() {
        return new ISLSingleByteResponse(String.valueOf(ACK));
    }

    public static SingleByteResponse nack() {
        return new ISLSingleByteResponse(String.valueOf(NACK));
    }

    public static SingleByteResponse waitResponse() {
        return new ISLSingleByteResponse(String.valueOf(WAIT));
    }

    @Override
    public String toString() {
        return response;
    }

    @Override
    public byte[] toBytes() {
        return response.getBytes(StandardCharsets.UTF_8);
    }
}
