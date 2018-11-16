package models;

import otomoto.VoivodeshipsHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Owner {
    private final String name;
    private final String location;
    private final List<String> phoneNumbers;
    private final List<Car> cars = new ArrayList<>();

    public Owner(String name, String location, List<String> phoneNumbers) {
        this.name = name;
        this.location = location;
        this.phoneNumbers = phoneNumbers;
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<Car> getCars() {
        return cars;
    }

    private BigDecimal getTotalPrice() {
        BigDecimal total = new BigDecimal(0);
        for (Car car : cars) {
            total = total.add(car.getPrice());
        }
        return total;
    }

    private String printPhoneNumbers() {
        String numbers = "";
        for (String number : this.phoneNumbers) {
            numbers += number + ",";
        }
        return !numbers.isEmpty() ? numbers.substring(0, numbers.length() - 1) : numbers;
    }

    public boolean hasCarWithId(String id) {
        for (Car car : cars) {
            if (car.getId().equals(id))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return
                name + ';' +
                        location + ';' +
                        VoivodeshipsHelper.getVoivodeship(location) + ';' +
                        printPhoneNumbers() + ';' +
                        cars.size() + ';' +
                        getTotalPrice();
    }

}
