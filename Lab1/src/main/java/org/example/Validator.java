package org.example;

import org.jsoup.nodes.Element;
public class Validator {
    public static String validateLink(Element element, String attr) {
        return element != null ? element.attr(attr) : "Unknown";
    }
    public static String validateText(Element element) {
        return element != null ? element.text() : "Unknown";
    }
    public static String validatePrice(Element priceElement) {
        if (priceElement != null) {
            Element oldPriceElement = priceElement.selectFirst("span.price-old");
            if (oldPriceElement != null) {
                oldPriceElement.remove();
            }
            String s = priceElement.text().replaceAll("\\D+", "");
            return isValidInteger(s) ? s : "0";
        }
        return "Unknown";
    }
    public static boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}