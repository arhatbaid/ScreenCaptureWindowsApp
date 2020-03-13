package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Utils {

    public static byte[] getFileBytes() {
        File file = new File("arhat.jpg");
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
