package otomoto;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import models.Car;
import models.Owner;
import models.OwnerNumberInfo;
import models.OwnersLibrary;

import java.io.IOException;
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

    public void traverse() throws IOException, InterruptedException {
        Page mainPage = fetchMainPage(1);
        int lastPage = PageParser.parseLastPageNumber(mainPage.getWebResponse().getContentAsString());
        int currentPage = 1;
        while (currentPage <= lastPage) {
            LOGGER.info("Parsing page number = " + currentPage);
            List<String> carUrls = PageParser.parseCarUrls(mainPage.getWebResponse().getContentAsString());
            fetchCarsInfo(carUrls);
            mainPage = fetchMainPage(++currentPage);
            // TODO: optimize it
            ResultSaver.saveResult("/home/seroz/IdeaProjects/seroz/src/main/resources/results" + (currentPage - 1) + ".txt", ownersLibrary);
        }
    }

    private Page fetchMainPage(int pageNumber) throws IOException, InterruptedException {
        URL url = new URL(BASE_URL + "ajax/search/list/");
        List<NameValuePair> requestParameter = new ArrayList<>();
        NameValuePair parameter1 = new NameValuePair("search[category_id]", "29");
        NameValuePair parameter2 = new NameValuePair("search[city_id]", "17871");
        NameValuePair parameter3 = new NameValuePair("search[dist]", "600");
        NameValuePair parameter4 = new NameValuePair("page", String.valueOf(pageNumber));
        requestParameter.add(parameter1);
        requestParameter.add(parameter2);
        requestParameter.add(parameter3);
        requestParameter.add(parameter4);
        WebRequest request = new WebRequest(url, HttpMethod.POST);
        request.setRequestParameters(requestParameter);
        request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded");
        Page page = sendRequest(1, request);
        if (page.getWebResponse().getContentAsString().contains("Wyślij"))
            LOGGER.info("Successfully fetched main page");
        return page;
    }


    private void fetchCarsInfo(List<String> carUrls) throws IOException, InterruptedException {
        for (String href : carUrls) {
            URL url = new URL(href);
            WebRequest request = new WebRequest(url, HttpMethod.GET);
            Page carPag = sendRequest(1, request);
            if (carPag == null) {
                LOGGER.warning("Could not fetch cars info");
                return;
            }
            String carPage = carPag.getWebResponse().getContentAsString();
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

    private Owner fetchOwner(String carPage) throws IOException, InterruptedException {
        String name = PageParser.parseOwnerName(carPage);
        String location = PageParser.parseOwnerLocation(carPage);
        Set<OwnerNumberInfo> ownerNunmberInfos = PageParser.parseOwnerNumbersInfos(carPage);
        List<String> phoneNumbers = fetchPhoneNumbers(ownerNunmberInfos);
        return new Owner(name, location, phoneNumbers);
    }

    private List<String> fetchPhoneNumbers(Set<OwnerNumberInfo> ownerNunmberInfos) throws IOException, InterruptedException {
        List<String> phoneNumbers = new ArrayList<>();
        for (OwnerNumberInfo ownerNumberInfo : ownerNunmberInfos) {
            URL url = new URL(BASE_URL + "ajax/misc/contact/multi_phone/" + ownerNumberInfo.getKey() + "/" + ownerNumberInfo.getIndex());
            WebRequest request = new WebRequest(url, HttpMethod.GET);
            Page numberPage = sendRequest(1, request);
            String phoneNumber = numberPage != null ? PageParser.parsePhoneNumber(numberPage.getWebResponse().getContentAsString()) : "NO NUMBER";
            phoneNumbers.add(phoneNumber);
        }
        if (phoneNumbers.size() < 1)
            LOGGER.warning("No numbers fetched");
        return phoneNumbers;
    }

    private Page sendRequest(int attend, WebRequest request) throws InterruptedException, IOException {
        Thread.sleep((long) (Math.random() * 1000));
        if (attend == 5) {
            LOGGER.warning("Last attend to connect to: " + request.getUrl());
            Thread.sleep((long) (Math.random() * 330000));
        }
        if (attend > 5) {
            LOGGER.warning("Could not send request to: " + request.getUrl());
            return null;
        }
        Page page = null;
        try {
            page = client.getPage(request);
            if (page != null && page.getWebResponse().getContentAsString().contains("Access Denied"))
                Thread.sleep((long) (Math.random() * 1800000) + 10000);
            if (page.getWebResponse().getStatusCode() != 200)
                return sendRequest(attend + 1, request);
        } catch (FailingHttpStatusCodeException | NullPointerException ex) {
            Thread.sleep((long) (Math.random() * 3000));
            sendRequest(++attend, request);
        }
        return page;
    }
}

