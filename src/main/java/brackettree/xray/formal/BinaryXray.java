package brackettree.xray.formal;

import brackettree.writer.BracketTreeWriter;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

public class BinaryXray implements Xray {

    transient byte[] bytes;

    public BinaryXray(byte[] bytes) {
        this.bytes = bytes;
    }

    public BinaryXray() {
    }

    @Override
    public String toString(BracketTreeWriter writer) {
        return writer.escaped(utf8EncodePrintable(this.bytes));
    }

    @Override
    public String toString() {
        return super.toString() + "{" + Arrays.toString(bytes) + "}";
    }

    @Override
    public void writeExternal(ObjectOutput out) {}

    @Override
    public void readExternal(ObjectInput in) {}

    public static String utf8Encode(byte ... bytes) {
        if(bytes.length == 0) return "";
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        int shift = 1;
        byte lb = 0;

        while(i < bytes.length) {
            byte b = (byte)((((bytes[i] & 0xFF) >>> shift) | lb) & 0x7F);
            lb = (byte)((bytes[i] << (7 - shift)) & 0x7F);
            stringBuilder.append((char)b);
            ++shift;
            ++i;
            if(shift > 7) {
                shift = 1;
                stringBuilder.append((char)lb);
                lb = 0;
            }
        }
        if(shift > 1) {
            stringBuilder.append((char)lb);
        }

        return stringBuilder.toString();
    }

    public static byte[] utf8Decode(String str) {
        byte[] encodedBytes = str.getBytes();
        byte[] bytes = new byte[encodedBytes.length - (encodedBytes.length + 7) / 8];
        int i = 0;
        int e = 0;
        int shift = 1;

        while (i < bytes.length) {
            byte b = (byte)((encodedBytes[e] << shift) | encodedBytes[e + 1] >>> (7 - shift));
            ++shift;
            ++e;
            if(shift > 7) {
                shift = 1;
                ++e;
            }
            bytes[i++] = b;
        }
        return bytes;
    }

    static boolean isValidPrintableByte(byte b) {
        return ((b & 0x80) == 0) && ((b & 0x60) != 0) && ((b & 0xF0) != 0x30) && ((b & 0x7F) != 0x7F) &&
                (b != ' ') && (b != '\"') && (b != '[') && (b != ']');
    }

    public static String utf8EncodePrintable(byte ... bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for(var b : bytes) {
            if(isValidPrintableByte(b)) {
                stringBuilder.append((char)b);
            } else {
                stringBuilder.append((char)(0x30 | ((b & 0xF0) >> 4)));
                stringBuilder.append((char)(0x30 | (b & 0xF)));
            }
        }
        return stringBuilder.toString();
    }

    public static byte[] utf8DecodePrintable(String str) {
        List<Byte> bytes = new ArrayList<>();
        boolean partial = false;
        byte part = 0;
        for(var b : str.getBytes()) {
            if((b & 0xF0) != 0x30) {
                if(partial) {
                    bytes.add((byte)(((b & 0xF0) >> 4) | part));
                    part = (byte)((b & 0xF) << 4);
                } else {
                    bytes.add(b);
                }
            } else {
                if(partial) {
                    bytes.add((byte)((b & 0xF) | part));
                    partial = false;
                } else {
                    part = (byte)((b & 0xF) << 4);
                    partial = true;
                }
            }
        }

        byte[] bytesArray = new byte[bytes.size()];
        int i = 0;
        for(var b : bytes) {
            bytesArray[i++] = b;
        }
        return bytesArray;
    }
}
