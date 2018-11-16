import otomoto.OtoMotoScraper;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        OtoMotoScraper otoMotoScraper = new OtoMotoScraper();
        otoMotoScraper.traverse();
    }

}
