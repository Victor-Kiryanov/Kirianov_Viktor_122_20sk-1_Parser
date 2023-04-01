package com.example.parserKirianov.servises;

import com.example.parserKirianov.model.Info;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WebsiteParser {

    WebDriver driver;
    public void establishConnection(WebDriver drivers) {
        driver = drivers;
    }

    public WebDriver getestablishConnection(){
        return driver;
    }

    private WebDriver searchQueryOnSite(String whatSearch) throws InterruptedException {

        WebDriver connectDriver = getestablishConnection();

        connectDriver.get("https://rozetka.com.ua/");

        // Пошук поля для введення пошукового запиту та кнопки пошуку
        WebElement searchInput = connectDriver.findElement(By.cssSelector("input[name='search']"));
        WebElement searchButton = connectDriver.findElement(By.cssSelector("button[class='button button_color_green button_size_medium search-form__submit ng-star-inserted']"));

        // Введення пошукового запиту та натискання кнопки пошуку
        searchInput.sendKeys(whatSearch);
        searchButton.click();

        // Очікування завантаження сторінки з результатами пошуку
        Thread.sleep(1000);

        return connectDriver;
    }

    private int countPage(WebDriver connectDriver) {

        List<WebElement> elements = connectDriver.findElements(By.className("pagination__item"));

        if (elements.size() == 0) {
            return 0;
        } else {
            return Integer.parseInt(elements.get(elements.size() - 1).getText());
        }
    }

    private List<Info> addInformationToListFromPage(Elements adElements, List<Info> infoList, int pageNumber, String whatSearch) {

        for (Element productElement : adElements) {
            Info searchInfo = new Info();
            searchInfo.setPageNumber(pageNumber);
            searchInfo.setSearch(whatSearch);
            searchInfo.setInternalNumber(productElement.select("div.g-id.display-none").text());
            searchInfo.setShortDescription(productElement.select("a.goods-tile__heading").text());
            String price = productElement.select("span.goods-tile__price-value").text();
            searchInfo.setPrice(price.isEmpty()? "На сайті не вказано ціну!" : price);
            searchInfo.setProductLink(productElement.select("a.goods-tile__heading").attr("href"));
            searchInfo.setAvailability(productElement.select("div.goods-tile__availability").text());
            infoList.add(searchInfo);
        }

        return infoList;
    }

    private List<Info> informationFromOtherPages(List<Info> infoPage, WebDriver connectDriver, int pageNumber, String whatSearch, int pageCount) {
        double percent = percentGenerated(pageCount);
        while (true) {
            try {
                WebElement nextButton = connectDriver.findElement(By.cssSelector("a.pagination__direction--forward"));

                if (nextButton.getAttribute("class").contains("disabled")) {
                    break;
                }
                nextButton.click();

                Thread.sleep(1000);

                Document doc = Jsoup.parse(connectDriver.getPageSource());
                Elements elementPage = doc.select("div.goods-tile");

                Thread.sleep(1000);

                pageNumber = pageNumber + 1;

                infoPage = addInformationToListFromPage(elementPage, infoPage, pageNumber, whatSearch);

                infoAboutWorkParser(percent += percentGenerated(pageCount));

            } catch (Exception e) {
                break;
            }
        }
        return infoPage;
    }

    private double percentGenerated(int CountPage) {
        return 100 / CountPage;
    }

    private void displayInfoForServer(int pageCount) {
        System.out.println("Found pages: " + (pageCount == 0 ? 1 : pageCount));
        System.out.println("Data collection and Excel generation is going on.");
    }

    private void infoAboutWorkParser(double percent) {
        String result = String.format("%.2f", percent);
        System.out.println("Сгенерировано " + result + "%");
    }

    public void disconnectBrowserConnection(WebDriver connectDriver) {
        connectDriver.quit();
    }

    public List<Info> parsingPerRequestOnRozetka(String whatSearch) throws InterruptedException {

        int pageNumber = 1, pageCount;

        List<Info> info = new ArrayList<>();

        WebDriver driver = searchQueryOnSite(whatSearch);

        Document doc = Jsoup.parse(driver.getPageSource());
        Elements element = doc.select("div.goods-tile");

        if (!element.isEmpty()) {
            pageCount = countPage(driver);
            displayInfoForServer(pageCount);
            info = addInformationToListFromPage(element, info, pageNumber, whatSearch);
            infoAboutWorkParser(pageCount == 0 ? 100 : percentGenerated(pageCount));
            if (pageCount > 0) {
                info = informationFromOtherPages(info, driver, pageNumber, whatSearch, pageCount);
            }
        } else {
            Info searchInfo = new Info();
            searchInfo.setSearch(whatSearch);
            searchInfo.setShortDescription("За Вашим запитом нічого не знайдено на сайті. Спробуйте сформулувати по іншому!");
            info.add(searchInfo);
        }

        return info;
    }
}
