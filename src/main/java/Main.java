import org.apache.commons.lang3.mutable.MutableInt;
import otomoto.OtoMotoScraper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger("Main-firmy");

    public static void main(String[] args) throws InterruptedException, IOException {
        FileHandler fileHandler = new FileHandler("main-firmy.log", true);
        LOGGER.addHandler(fileHandler);
        MutableInt lastParsedPage = new MutableInt(1);
        boolean success = false;
        OtoMotoScraper otoMotoScraper = new OtoMotoScraper();
        while (!success) {
            try {
                otoMotoScraper.traverse(lastParsedPage);
                success = true;
                LOGGER.info("Parsing completed. Paged parsed: " + lastParsedPage.getValue());
            } catch (Exception ex) {
                LOGGER.warning(ex.toString());
                Thread.sleep(1000);
            }
        }
    }

}
