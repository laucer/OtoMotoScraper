package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class OwnersLibrary {

    private static final Logger LOGGER = Logger.getLogger("OwnersLibrary");
    private final List<Owner> owners = new ArrayList<>();

    public void add(Owner owner) {
        int isUserInDatabase = isUserInDatabase(owner);
        if (isUserInDatabase != -1) {
            LOGGER.info("User exists in database");
            addAllNotDuplicatedCars(owners.get(isUserInDatabase), owner);
        } else {
            owners.add(owner);
        }
    }

    private int isUserInDatabase(Owner owner) {
        for (int i = 0; i < owners.size(); ++i) {
            Owner databaseOwner = owners.get(i);
            if (!Collections.disjoint(owner.getPhoneNumbers(), databaseOwner.getPhoneNumbers()))
                return i;
        }
        return -1;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    private void addAllNotDuplicatedCars(Owner databaseOwner, Owner newOwner) {
        for (Car car : newOwner.getCars()) {
            if (!databaseOwner.hasCarWithId(car.getId())) {
                databaseOwner.addCar(car);
            }else{
                LOGGER.info("Wanted to add duplicated car");
            }
        }
    }

    public int totalCarsParsed() {
        int result = 0;
        for (Owner owner : owners) {
            result += owner.getCars().size();
        }
        return result;
    }

}
