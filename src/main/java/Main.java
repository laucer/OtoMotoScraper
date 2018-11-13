import otomoto.OtoMotoScraper;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        OtoMotoScraper otoMotoScraper = new OtoMotoScraper();
        otoMotoScraper.traverse();

    }
}
