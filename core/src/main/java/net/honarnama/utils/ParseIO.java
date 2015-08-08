package net.honarnama.utils;

import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
