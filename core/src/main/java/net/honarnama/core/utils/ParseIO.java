package net.honarnama.core.utils;

import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * Created by reza on 8/8/15.
 */
public class ParseIO {

    public static ParseFile getParseFileFromFile(String name, File input) throws IOException {
        if (!input.canRead()) {
            throw new IOException("File not readable: " + input);
        }

        ByteArrayOutputStream ous = null;
        InputStream ios = null;

        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(input);
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            if (ous != null)
                ous.close();

            if (ios != null)
                ios.close();
        }

        ParseFile parseFile = new ParseFile(name, ous.toByteArray());
        return parseFile;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
