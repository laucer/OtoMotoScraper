package otomoto;

import models.Owner;
import models.OwnersLibrary;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ResultSaver {

    private static final Logger LOGGER = Logger.getLogger("OtoMotoScraper-firmy");

    public static void saveResult(String filename, OwnersLibrary ownersLibrary) throws IOException {
        List<String> lines = getLines(ownersLibrary);
        Path file = Paths.get(filename);
        Files.write(file, lines, Charset.forName("UTF-8"));
        LOGGER.info("Successfully saved into: " + filename);
    }

    private static List<String> getLines(OwnersLibrary ownersLibrary) {
        List<String> lines = new ArrayList<>();
        for (Owner owner : ownersLibrary.getOwners()) {
            lines.add(owner.toString());
        }
        return lines;
    }
}
