package com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.DataResponse;

import java.nio.charset.StandardCharsets;

public class IslDataResponseBuilder implements DataResponse {
    private String response = "";

    public IslDataResponseBuilder append(String s, char fillChar, int length, boolean frontPopulate) {
        response = response + fillStringWithCharsUpToLength(s,fillChar,length,frontPopulate);
        return this;
    }

    public IslDataResponseBuilder append(String s) {
        response = response + s;
        return this;
    }

    private String fillStringWithCharsUpToLength(String s, char fillChar, int length, boolean frontPopulate) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(fillChar).repeat(Math.max(0, (length - s.length()))));
        return frontPopulate ? sb + s : s + sb;
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
