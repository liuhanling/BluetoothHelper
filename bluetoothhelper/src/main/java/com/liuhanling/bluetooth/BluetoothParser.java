package com.liuhanling.bluetooth;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-06-10 17:31
 */
public class BluetoothParser {

    public static BluetoothData parseBluetoothName(byte[] record) {
        if (record == null) {
            return null;
        }

        String name = null;
        List<UUID> uuids = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(record).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte len = buffer.get();
            if (len == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len >= 2) {
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        len -= 2;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (len >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        len -= 16;
                    }
                    break;
                case 0x09:
                    byte[] nameBytes = new byte[len - 1];
                    buffer.get(nameBytes);
                    try {
                        name = new String(nameBytes, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    buffer.position(buffer.position() + len - 1);
                    break;
            }
        }
        return new BluetoothData(uuids, name);
    }

    public static class BluetoothData {

        private List<UUID> uuids;
        private String name;

        public BluetoothData(List<UUID> uuids, String name){
            this.uuids = uuids;
            this.name = name;
        }

        public List<UUID> getUuids(){
            return this.uuids;
        }

        public String getName(){
            return this.name;
        }
    }
}
