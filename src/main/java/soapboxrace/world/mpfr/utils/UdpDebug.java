package soapboxrace.world.mpfr.utils;

import io.netty.buffer.ByteBuf;

public class UdpDebug {
    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(":", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder data = new StringBuilder();
        for (byte aB : b) {
            data.append(Integer.toHexString((aB >> 4) & 0xf));
            data.append(Integer.toHexString(aB & 0xf));
            data.append(":");
        }
        return data.toString();
    }
    
    public static byte[] byteBufToArray(ByteBuf buf) {
        byte[] bytes;
        int offset;
        int length = buf.readableBytes();

        if (buf.hasArray()) {
            bytes = buf.array();
            offset = buf.arrayOffset();
        } else {
            bytes = new byte[length];
            buf.getBytes(buf.readerIndex(), bytes);
            offset = 0;
        }
        
        return bytes;
    }
}