package im.status.applet_installer_test.appletinstaller.apducommands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import im.status.applet_installer_test.appletinstaller.APDUCommand;
import im.status.applet_installer_test.appletinstaller.HexUtils;

public class Load {
    public static final int CLA = 0x80;
    public static final int INS = 0xE8;

    private static String[] fileNames = {"Header", "Directory", "Import", "Applet", "Class", "Method", "StaticField", "Export", "ConstantPool", "RefLocation"};

    private String path;
    private int offset;
    private int count;
    private byte[] fullData;

    public Load(String path) throws FileNotFoundException, IOException {
        this.path = path;
        this.offset = 0;
        this.count = 0;
        Map<String, byte[]> files = this.loadFiles(this.path);
        this.fullData = this.getCode(files);
    }

    public Map<String, byte[]> loadFiles(String path) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        InputStream in = new FileInputStream(this.path);
        ZipInputStream zip = new ZipInputStream(in);

        ZipEntry entry = zip.getNextEntry();

        while(entry != null) {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int count;
            while ((count = zip.read(buf)) != -1) {
                data.write(buf, 0, count);
            }
            String name = baseName(entry.getName());
            files.put(name, data.toByteArray());
            entry = zip.getNextEntry();
        }

        return files;
    }

    private String baseName(String path) {
        String[] parts = path.split("[/.]");
        return parts[parts.length - 2];
    }

    public APDUCommand getCommand() {
        int blockSize = 247; // 255 - 8 bytes for MAC
        if (this.offset >= this.fullData.length) {
            return null;
        }

        int rangeEnd = this.offset + blockSize;
        if (rangeEnd >= this.fullData.length) {
            rangeEnd = this.fullData.length;
        }

        int size = rangeEnd - offset;
        byte[] data = new byte[size];
        System.arraycopy(this.fullData, this.offset, data, 0, size);

        if (this.count == 30) {
            System.out.printf("-- OFFSET %d %n", this.offset);
            System.out.printf("-- FULL LENGTH %d %n", this.fullData.length);
            System.out.printf("-- RANGE END %d %n", rangeEnd);
            System.out.printf("-- SIZE %d %n", size);
        }


        boolean isLast = this.offset + size >= this.fullData.length;
        int p1 = isLast ? 0x80 : 0;
        APDUCommand cmd = new APDUCommand(CLA, INS, p1, this.count, data);

        this.offset += size;
        this.count++;

        return cmd;
    }

    private byte[] encodeFullLength(int length) {
        if (length < 0x80) {
            return new byte[]{(byte) length};
        } else if (length < 0xFF) {
            return new byte[]{(byte) 0x81, (byte) length};
        } else if (length < 0xFFFF) {
            return new byte[]{
                    (byte) 0x82,
                    (byte) ((length & 0xFF00) >> 8),
                    (byte) (length & 0xFF),
            };
        } else {
            return new byte[]{
                    (byte) 0x83,
                    (byte) ((length & 0xFF0000) >> 16),
                    (byte) ((length & 0xFF00) >> 8),
                    (byte) (length & 0xFF),
            };
        }
    }

    public byte[] getCode(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

        for (String name : fileNames) {
            byte[] fileData = files.get(name);
            if (fileData == null) {
                continue;
            }

            dataStream.write(fileData);
        }

        byte[] data = dataStream.toByteArray();
        byte[] encodedFullLength = encodeFullLength(data.length);
        byte[] fullData = new byte[1 + encodedFullLength.length + data.length];

        fullData[0] = (byte) 0xC4;
        System.arraycopy(encodedFullLength, 0, fullData, 1, encodedFullLength.length);
        System.arraycopy(data, 0, fullData, 1 + encodedFullLength.length, data.length);

        return fullData;
    }
}
