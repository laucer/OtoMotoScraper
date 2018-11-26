package otomoto;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import models.Car;
import models.Owner;
import models.OwnerNumberInfo;
import models.OwnersLibrary;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class OtoMotoScraper {

    private static final Logger LOGGER = Logger.getLogger("OtoMotoScraper");
    private static final String BASE_URL = "https://www.otomoto.pl/";
    private final OwnersLibrary ownersLibrary = new OwnersLibrary();
    private WebClient client = new WebClient();

    public OtoMotoScraper() throws IOException {
        this.client.getOptions().setCssEnabled(false);
        this.client.getOptions().setJavaScriptEnabled(false);
        this.client.getCookieManager().setCookiesEnabled(true);
        this.client.getOptions().setTimeout(1000000);
        FileHandler fileHandler = new FileHandler("scraper-firmy.log", true);
        LOGGER.addHandler(fileHandler);
    }

    public void traverse(MutableInt currentPage) throws IOException, InterruptedException, URISyntaxException {
        String mainPage = fetchMainPage(1);
        LOGGER.info("Number of pages to parse: " + PageParser.parseLastPageNumber(mainPage));
        while (!isLastPage(mainPage)) {
            final long startTime = System.currentTimeMillis();
            LOGGER.info("Parsing page number = " + currentPage.getValue());
            List<String> carUrls = PageParser.parseCarUrls(mainPage);
            fetchCarsInfo(carUrls);
            currentPage.setValue(currentPage.getValue() + 1);
            mainPage = fetchMainPage(currentPage.getValue());
            ResultSaver.saveResult("/home/results/firmy/" + (currentPage.getValue() - 1) + ".txt", ownersLibrary);
            final long endTime = System.currentTimeMillis();
            LOGGER.info("It took: " + ((endTime - startTime) / 1000) + " seconds to parse a single page.");
        }
        LOGGER.info("Parsed: " + ownersLibrary.totalCarsParsed());
    }

    private boolean isLastPage(String page) {
        return page.contains("No results for your search");
    }

    private String fetchMainPage(int pageNumber) throws IOException, URISyntaxException, InterruptedException {
        URL url = new URL("https://www.otomoto.pl/osobowe/uzywane/?search[private_business]=business&page=" + pageNumber);
        WebRequest request = new WebRequest(url, HttpMethod.GET);
        String page = sendRequest(request);
        return page;
    }


    private void fetchCarsInfo(List<String> carUrls) throws IOException, InterruptedException, URISyntaxException {
        for (String href : carUrls) {
            URL url = new URL(href);
            WebRequest request = new WebRequest(url, HttpMethod.GET);
            String carPage = sendRequest(request);
            if (carPage == null) {
                LOGGER.warning("Could not fetch cars info");
                return;
            }
            Car car = fetchCar(carPage);
            Owner owner = fetchOwner(carPage);
            owner.addCar(car);
            ownersLibrary.add(owner);
        }
    }


    private Car fetchCar(String carPage) {
        String id = PageParser.parseCarId(carPage);
        String price = PageParser.parseCarPrice(carPage);
        return new Car(id, price);
    }

    private Owner fetchOwner(String carPage) throws IOException, InterruptedException, URISyntaxException {
        String name = PageParser.parseOwnerName(carPage);
        String location = PageParser.parseOwnerLocation(carPage);
        Set<OwnerNumberInfo> ownerNunmberInfos = PageParser.parseOwnerNumbersInfos(carPage);
        List<String> phoneNumbers = fetchPhoneNumbers(ownerNunmberInfos);
        return new Owner(name, location, phoneNumbers);
    }

    private List<String> fetchPhoneNumbers(Set<OwnerNumberInfo> ownerNunmberInfos) throws IOException, InterruptedException, URISyntaxException {
        List<String> phoneNumbers = new ArrayList<>();
        for (OwnerNumberInfo ownerNumberInfo : ownerNunmberInfos) {
            URL url = new URL(BASE_URL + "ajax/misc/contact/multi_phone/" + ownerNumberInfo.getKey() + "/" + ownerNumberInfo.getIndex());
            WebRequest request = new WebRequest(url, HttpMethod.GET);
            String numberPage = sendRequest(request);
            String phoneNumber = numberPage != null ? PageParser.parsePhoneNumber(numberPage) : "NO NUMBER";
            phoneNumbers.add(phoneNumber);
        }
        if (phoneNumbers.size() < 1)
            LOGGER.warning("No numbers fetched");
        return phoneNumbers;
    }

    private String sendRequest(WebRequest request) throws IOException, URISyntaxException, InterruptedException {
        String page = null;
        try {
            HttpHost proxy = new HttpHost("zproxy.lum-superproxy.io", 22225);
            Request apacheRequest = convertToApacheRequest(request);
            page = Executor.newInstance()
                    .auth(proxy, "LUMINATI_ID", "LUMINATI_PASSWORD")
                    .execute(apacheRequest.viaProxy(proxy))
                    .returnContent().asString();
        } catch (FailingHttpStatusCodeException | NullPointerException ex) {
            return null;
        }
        return page;
    }

    private Request convertToApacheRequest(WebRequest webRequest) throws URISyntaxException {
        URL url = webRequest.getUrl();
        Request request = webRequest.getHttpMethod().equals(HttpMethod.GET) ? Request.Get(url.toURI()) : Request.Post(url.toURI());
        webRequest.getAdditionalHeaders().forEach(
                (key, value) -> {
                    request.addHeader(key, value);
                }
        );
        if (webRequest.getHttpMethod().equals(HttpMethod.GET))
            return request;
        List<org.apache.http.NameValuePair> params = new ArrayList<>();
        webRequest.getRequestParameters().forEach(
                (element) -> {
                    org.apache.http.NameValuePair nameValuePair = new BasicNameValuePair(element.getName(), element.getValue());
                    params.add(nameValuePair);
                }
        );
        request.bodyForm(params, Charsets.UTF_8);
        return request;
    }

}

