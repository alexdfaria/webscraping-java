package unit_fundamentals;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.impl.client.SystemDefaultHttpClient;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketOption;
import java.net.URLEncoder;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AmazonNL {

    // All sponsored products paths
    private final static String pathNameS = "]/div/div/div/div/div/div/div[2]/div[1]/h2/a/span/text()";
    private final static String pathPriceS = "]/div/div/div/div/div/div/div[2]/div[3]/div/a/span/span[2]/span[2]/text()";
    private final static String pathCurrencyS = "]/div/div/div/div/div/div/div[2]/div[3]/div/a/span/span[2]/span[1]/text()";
    private final static String pathPromoPriceS = "]/div/div/div/div/div/div/div[2]/div[2]/div[1]/a/span/span[2]/span[2]/text()";
    private final static String pathPromoCurrencyS = "]/div/div/div/div/div/div/div[2]/div[2]/div[1]/a/span/span[2]/span[1]/text()";

    // Without RATING && without Promotion
    private final static String pathPriceNoRatingS = "]/div/div/div/div/div/div/div[2]/div[2]/div[1]/a/span/span[2]/span[2]/text()";
    private final static String pathCurrencyNoRatingS = "]/div/div/div/div/div/div/div[2]/div[2]/div[1]/a/span/span[2]/span[1]/text()";

    // Paths of Rating, Promotion, Sponsor and Link labels
    private final static String pathRatingS = "]/div/div/div/div/div/div/div[2]/div[2]/div/span[1]/span/a/i[1]/span/text()";
    private final static String pathLinkS = "]/div/div/div/div/div/div/div[2]/div[1]/h2//a/@href";

    private final static String pathPromotion = "]/div/div/div/div/div/div/div[2]/div[2]/div[2]/span/text()";
    private final static String pathSponsor = "]/div/div/div/div/div/div/div[2]/div[1]/div/span/a/span[1]/span/text()";

    // All non-sponsored products paths
    //TODO Non-sponsored promotion products paths cases
    private final static String pathName = "]/div/div/div/div/div[2]/div[1]/h2/a/span/text()"; //OK

    // NORMAL  (with RATING, without Promotion)
    private final static String pathPrice = "]/div/div/div/div/div[2]/div[3]/div/a/span/span[2]/span[2]/text()"; //OK
    private final static String pathCurrency = "]/div/div/div/div/div[2]/div[3]/div/a/span/span[2]/span[1]/text()"; //OK
    // Without Rating && without Promotion
    private final static String pathPriceNoRating = "]/div/div/div/div/div[2]/div[2]/div/a/span/span[2]/span[2]/text()"; //OK
    private final static String pathCurrencyNoRating = "]/div/div/div/div/div[2]/div[2]/div/a/span/span[2]/span[1]/text()"; //OK
    // Products with Promotion
    private final static String pathPromoPrice = "]/div/div/div/div/div/div/div[2]/div[2]/div[1]/a/span/span[2]/span[2]/text()"; //TODO
    private final static String pathPromoCurrency = "]/div/div/div/div/div/div/div[2]/div[2]/div[1]/a/span/span[2]/span[1]/text()"; //TODO

    // Paths of Rating and Link labels
    private final static String pathRating = "]/div/div/div/div/div[2]/div[2]/div/span[1]/span/a/i[1]/span/text()"; //OK
    private final static String pathLink = "]/div/div/div/div/div[2]/div[1]/h2//a/@href"; //OK

    public static void amazonNL() {
        String searchQuery = "jacuzzi";

        Scanner scanner = new Scanner(System.in);

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            //String searchUrl = "https://www.amazon.nl/s?k=" + URLEncoder.encode(searchQuery, "UTF-8");

            String searchUrl = "https://www.amazon.nl/s?k=jacuzzi";
            HtmlPage page = client.getPage(searchUrl);

            CSVWriter writer = new CSVWriter(new FileWriter("export.csv"));
            List<String[]> writeRows = new ArrayList<>();

            // Write the header on CSV file
            writeHeader(writeRows);

            // Save a list of HTML Elements of all the divs that contains the s-result-item class
            List<HtmlElement> items = page.getByXPath("//div[contains(@class, 's-result-item')]");

            // OLD PATH:
            //String pathPrefix = "//*[@id='search']/div[1]/div[1]/div/span[3]/div[2]/div[";
            String pathPrefix = "//*[@id='search']/div[1]/div[1]/div/span[1]/div[1]/div[";


            // Check if there are
            if (items.isEmpty()) {
                System.out.println("No items found! :(");
            } else {
                System.out.println("Items found: " + items.size());

                //String imageURL = "*[@id='landingImage']";

                String[] productData;
                String name = "";
                String price = "";
                int count = 1;
                int ignoredItems = 2;
                int numItems = getNumItems(scanner);

                System.out.println("You choose a total of " + numItems + " items.");
                System.out.println("----------- ");


                // For each product on the first page
                for (HtmlElement htmlItem : items) {

                    // Check if it already got the number of items pretended
                    // Plus 2 because the first 2 elements queried aren't actually products
                    if (count + 2 > numItems + ignoredItems) {
                        break;
                    }

                    // If dataAsin is empty it skips the product and doesn't increment the counter
                    if (!checkDataAsin(htmlItem, count)) {

                        System.out.println("IGNORED " + count);
                        System.out.println("----------- ");
                        count++;
                        ignoredItems++;
                    } else {

                        //System.out.println(count + " ");

                        // Sponsored product:
                        if (!getLabel(htmlItem, count, pathSponsor).equals("")) {

                            //System.out.println("Rating: "+  htmlItem.getFirstByXPath(pathPrefix + count + pathRatingS));
                            //System.out.println("Promo: "+  htmlItem.getFirstByXPath(pathPrefix + count + pathPromotionS));

                            // Get name and price of the product the sponsored product
                            name = getLabel(htmlItem, count, pathNameS);
                            price = getPrice(htmlItem, count, true);

                            System.out.println("Sponsored product:\nName: " + name + "\nPrice: " + price);

                            productData = linkGenerator(client, htmlItem, pathPrefix, count, pathLinkS);

                            System.out.println("-------------");

                        }
                        // Non-sponsor items:
                        else {

                            name = getLabel(htmlItem, count, pathName);
                            price = getPrice(htmlItem, count, false);


                            System.out.println("Non-sponsored product:\nName: " + name + "\nPrice: " + price);

                            productData = linkGenerator(client, htmlItem, pathPrefix, count, pathLink);

                            System.out.println("-------------");

                        }

                        // Save a new row of the product to print on the CSV File
                        String[] newrow = new String[]{name, price, productData[0], productData[1], productData[2]};
                        writeRows.add(newrow);

                        count++;
                    }
                }

                // Print the rows on the CSV File
                writer.writeAll(writeRows);
                writer.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getNumItems(Scanner scanner) {
        System.out.print("Insert the number of items you want to web scrap: ");
        int numItems = scanner.nextInt();

        return numItems;
    }

    private static String getLabel(HtmlElement htmlItem, int count, String path) {

        String pathPrefix = "//*[@id='search']/div[1]/div[1]/div/span[1]/div[1]/div[";
        String label = "";

        if (htmlItem.getFirstByXPath(pathPrefix + count + path) != null) {
            label = htmlItem.getFirstByXPath(pathPrefix + count + path).toString();
        }

        return label;
    }

    private static boolean checkDataAsin(HtmlElement htmlItem, int count) {
        //System.out.println(getLabel(htmlItem, count, "]/@data-asin"));

        String eachProduct = getLabel(htmlItem, count, "]/@data-asin");
        eachProduct = eachProduct.substring(29, eachProduct.length() - 1);

        if (!eachProduct.equals("")) {

            //String eachProduct = htmlItem.getFirstByXPath(pathPrefix + count + "]/@data-asin").toString();
            //String eachProduct = getLabel(htmlItem, count, "]/@data-asin");

            //System.out.println("ASIN: " + eachProduct);

            return true;

        } else {
            System.out.println("Error getting data-asin.");
            return false;
        }
    }

    public static String getPrice(HtmlElement htmlItem, int count, boolean sponsor){

        String price = "";

        if(sponsor){
            // Different paths alternatives for prices
            if (!getLabel(htmlItem, count, pathPriceS).equals("")) {
                price = getLabel(htmlItem, count, pathPriceS) + " " + getLabel(htmlItem, count, pathCurrencyS);
                //System.out.println("Price: " + htmlItem.getFirstByXPath(pathPrefix + count + pathPriceS) + " " + htmlItem.getFirstByXPath(pathPrefix + count + pathCurrencyS));
            } else if (!getLabel(htmlItem, count, pathPromotion).equals("")) {
                price = getLabel(htmlItem, count, pathPromoPriceS) + " " + getLabel(htmlItem, count, pathPromoCurrencyS);
                //System.out.println("PricePromo: " + htmlItem.getFirstByXPath(pathPrefix + count + pathPromoPriceS) + " " + htmlItem.getFirstByXPath(pathPrefix + count + pathPromoCurrencyS));
            } else if (getLabel(htmlItem, count, pathRatingS).equals("")) {
                price = getLabel(htmlItem, count, pathPriceNoRatingS) + " " + getLabel(htmlItem, count, pathCurrencyNoRatingS);
                //System.out.println("PriceNoRating: " + htmlItem.getFirstByXPath(pathPrefix + count + pathPriceNoRatingS) + " " + htmlItem.getFirstByXPath(pathPrefix + count + pathCurrencyNoRatingS));
            } else {
                System.out.println("ERROR on sponsored price.");
            }
        }else{
            // Different paths alternatives for prices
            if (!getLabel(htmlItem, count, pathPrice).equals("")) {
                price = getLabel(htmlItem, count, pathPrice) + " " + getLabel(htmlItem, count, pathCurrency);
            } else if (!getLabel(htmlItem, count, pathPromotion).equals("")) {
                price = getLabel(htmlItem, count, pathPromoPrice) + " " + getLabel(htmlItem, count, pathPromoCurrency);
            } else if (getLabel(htmlItem, count, pathRating).equals("")) {
                price = getLabel(htmlItem, count, pathPriceNoRating) + " " + getLabel(htmlItem, count, pathCurrencyNoRating);
            } else {
                System.out.println("ERROR on non-sponsored price.");
            }

        }

        return price;
    }
    public static String[] linkGenerator(WebClient client, HtmlElement html, String prefix, int count, String link) throws IOException {

        String[] productData = new String[3];
        String description = "";
        String imageURL;

        // Get URL of the product
        if (html.getFirstByXPath(prefix + count + link) != null) {
            String fullLink = html.getFirstByXPath(prefix + count + link).toString();
            fullLink = fullLink.substring(24, fullLink.length() - 1);

            String productUrl = "https://www.amazon.nl" + fullLink;
            System.out.println("Link: " + productUrl);

            HtmlPage pageProduct = client.getPage(productUrl);

            // Get description
            if (pageProduct.getFirstByXPath("//div[contains(@id, 'productDescription')]/p/text()") != null) {

                for (int l = 1; pageProduct.getFirstByXPath("//div[contains(@id, 'productDescription')]/p//span[" + l + "]//text()") != null; l++) {
                    for (int p = 1; pageProduct.getFirstByXPath("//div[contains(@id, 'productDescription')]/p//span[" + l + "]//text()[" + p + "]") != null; p++) {

                        description += pageProduct.getFirstByXPath("//div[contains(@id, 'productDescription')]/p//span[" + l + "]//text()[" + p + "]").toString();
                    }
                }
                System.out.println("Description: " + description);

            } else {
                description = "No description";
                System.out.println("Product doesn't have description.");
            }

            // Get Image URL
            if (pageProduct.getFirstByXPath("//img[contains(@id, 'landingImage')]") != null) {

                imageURL = pageProduct.getFirstByXPath("//img[contains(@id, 'landingImage')]/@src").toString();
                imageURL = imageURL.substring(23, imageURL.length() - 1);

                System.out.println("Img URL: " + imageURL);
            } else {
                imageURL = "No image URL found";
                System.out.println("No image URL found.");
            }

            // Save link, description and imageURL into Array
            productData[0] = productUrl;
            productData[1] = description;
            productData[2] = imageURL;


        } else {
            System.out.println("Something went wrong.");
            productData[0] = "ERROR";
            productData[1] = "ERROR";
            productData[2] = "ERROR";
        }

        return productData;

    }

    public static void writeHeader(List<String[]> writeRows) {
        String[] header = new String[]{"Name", "Price", "Link", "Description", "Image URL"};
        writeRows.add(header);
    }


}
