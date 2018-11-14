package models;

import java.math.BigDecimal;

public class Car {

    private final String price;
    private String id;

    public Car(String id, String price) {
        this.id = id;
        this.price = price;
    }

    public BigDecimal getPrice() {
        try {
            return new BigDecimal(price);
        } catch (NullPointerException ex) {
            return new BigDecimal(0);
        }
    }

    public String getId() {
        return id;
    }
}

