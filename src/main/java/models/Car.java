package models;

import java.math.BigDecimal;
import java.math.MathContext;

public class Car {

    private final String price;
    private String id;

    public Car(String id, String price) {
        this.id = id;
        this.price = price;
    }

    public BigDecimal getPrice() {
        return new BigDecimal(price);
    }

    public String getId() {
        return id;
    }
}

