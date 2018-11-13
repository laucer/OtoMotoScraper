package otomoto;

import models.OwnerNumberInfo;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PageParser {

    private static final Logger LOGGER = Logger.getLogger("PageParser");

    public static int parseLastPageNumber(String page) {
        return Integer.parseInt(Jsoup.parse(page).select(".row").last().select(".page").last().text());
    }

    public static List<String> parseCarUrls(String page) {
        Elements elements = Jsoup.parse(page).getElementsByClass("offers list").select("article");
        List<String> carUrls = new ArrayList<>();
        for (Element el : elements) {
            String url = StringUtils.substringBetween(el.toString(), "data-href=\"", "\"> ");
            carUrls.add(url);
        }
        LOGGER.info("Successfully parsed: " + carUrls.size() + " links to cars.");
        return carUrls;
    }

    public static String parseCarPrice(String carPage) {
        try {
            String price = Jsoup.parse(carPage).toString();
            if (price == null || price.isEmpty()) {
                LOGGER.warning("Parsed price was not parsed. Setting price value to 0");
                return "0";
            }
            return StringUtils.substringBetween(price, "\"ad_price\":", ",\"price_currency\":");
        } catch (NullPointerException ex) {
            return "0";
        }
    }

    public static String parseOwnerName(String carPage) {
        try {
            return StringUtils.substringBetween(Jsoup.parse(carPage).toString(), "\"Kontakt ", "\">");
        } catch (NullPointerException ex) {
            return "NO OWNER NAME";
        }
    }

    public static String parseCarId(String carPage) {
        try {
            return StringUtils.substringAfter(Jsoup.parse(carPage).select(".offer-meta").first().text(), "ID: ");
        } catch (NullPointerException ex) {
            return "NO CAR ID";
        }
    }

    public static String parseOwnerLocation(String carPage) {
        try {
            return Jsoup.parse(carPage).select(".seller-box__seller-address").first().text();
        } catch (NullPointerException ex) {
            return "NO OWNER LOCATION";
        }
    }


    public static Set<OwnerNumberInfo> parseOwnerNumbersInfos(String carPage) {
        // TODO: it should be optimized
        Set<OwnerNumberInfo> ownerNumbersInfo = new HashSet<>();
        Elements elements;
        try {
            elements = Jsoup.parse(carPage).select(".number-box");
        } catch (NullPointerException ex) {
            return ownerNumbersInfo;
        }
        for (Element element : elements) {
            String key = StringUtils.substringBetween(element.toString(), "data-id=\"", "\"");
            String index = StringUtils.substringBetween(element.toString(), "data-index=\"", "\"");
            OwnerNumberInfo ownerNumberInfo = new OwnerNumberInfo(key, index);
            ownerNumbersInfo.add(ownerNumberInfo);
        }
        return ownerNumbersInfo;
    }

    public static String parsePhoneNumber(String phonePage) {
        return StringUtils.substringBetween(phonePage, "{\"value\":\"", "\"}");
    }

}
