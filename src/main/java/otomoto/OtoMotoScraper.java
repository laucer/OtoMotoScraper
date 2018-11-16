package otomoto;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import models.Car;
import models.Owner;
import models.OwnerNumberInfo;
import models.OwnersLibrary;
import org.apache.commons.io.Charsets;
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
import java.util.logging.Logger;

public class OtoMotoScraper {

    private static final Logger LOGGER = Logger.getLogger("OtoMotoScraper");
    private static final String BASE_URL = "https://www.otomoto.pl/";
    private final OwnersLibrary ownersLibrary = new OwnersLibrary();
    private WebClient client = new WebClient();

    public OtoMotoScraper() {
        this.client.getOptions().setCssEnabled(false);
        this.client.getOptions().setJavaScriptEnabled(false);
        this.client.getCookieManager().setCookiesEnabled(true);
        this.client.getOptions().setTimeout(1000000);
    }

    public void traverse() throws IOException, InterruptedException, URISyntaxException {
        String mainPage = fetchMainPage(1);
        int lastPage = PageParser.parseLastPageNumber(mainPage);
        int currentPage = 1;
        while (currentPage <= lastPage) {
            final long startTime = System.currentTimeMillis();
            LOGGER.info("Parsing page number = " + currentPage);
            List<String> carUrls = PageParser.parseCarUrls(mainPage);
            fetchCarsInfo(carUrls);
            mainPage = fetchMainPage(++currentPage);
            // TODO: optimize it
            ResultSaver.saveResult("/home/seroz/IdeaProjects/seroz/src/main/resources/results" + (currentPage - 1) + ".txt", ownersLibrary);
            final long endTime = System.currentTimeMillis();
            LOGGER.info("It took: " + ((endTime - startTime) / 1000) + " seconds to parse a single page.");
        }
    }

    private String fetchMainPage(int pageNumber) throws IOException, InterruptedException, URISyntaxException {
        URL url = new URL(BASE_URL + "ajax/search/list/");
        List<NameValuePair> requestParameter = new ArrayList<>();
        NameValuePair parameter1 = new NameValuePair("search[category_id]", "29");
        NameValuePair parameter2 = new NameValuePair("search[city_id]", "17871");
        NameValuePair parameter3 = new NameValuePair("search[dist]", "600");
        NameValuePair parameter4 = new NameValuePair("page", String.valueOf(pageNumber));
        NameValuePair parameter5 = new NameValuePair("search[private_business]", "business");
        requestParameter.add(parameter1);
        requestParameter.add(parameter2);
        requestParameter.add(parameter3);
        requestParameter.add(parameter4);
        requestParameter.add(parameter5);
        WebRequest request = new WebRequest(url, HttpMethod.POST);
        request.setRequestParameters(requestParameter);
        request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded");
        String page = sendRequest(request);
        if (page.contains("Wyślij"))
            LOGGER.info("Successfully fetched main page");
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

    private String sendRequest(WebRequest request) throws IOException, URISyntaxException {
        String page = null;
        try {
            HttpHost proxy = new HttpHost("zproxy.lum-superproxy.io", 22225);
            Request apacheRequest = convertToApacheRequest(request);
            page = Executor.newInstance()
                    .auth(proxy, "ASK OWNER", "ASK OWNER")
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

