package otomoto;

import org.apache.commons.lang3.StringUtils;

public class VoivodeshipsHelper {

    public static String getVoivodeship(String address) {
        if (StringUtils.containsAny(address.toLowerCase(), "dolnośląskie", "dolnoslaskie", "wrocław", "wroclaw"))
            return "Dolnośląskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "kujawsko-pomorskie", "kujawsko pomorskie", "bydgoszcz", "torun", "toruń"))
            return "Kujawsko-Pomorskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "lubelskie", "lublin"))
            return "Lubelskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "lubuskie", "gorzów wielkopolski", "gorzow wielkopolski", "zielona gora", "zielona góra"))
            return "Lubuskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "łódzkie", "lodzkie", "łódź", "lodz"))
            return "Łódzkie";
        else if (StringUtils.containsAny(address.toLowerCase(), "malopolskie", "małopolskie", "kraków", "krakow"))
            return "Małopolskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "mazowieckie", "warszawa"))
            return "Mazowieckie";
        else if (StringUtils.containsAny(address.toLowerCase(), "opolskie", "opole"))
            return "Opolskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "podkarpackie", "rzeszów", "rzeszow"))
            return "Podkarpackie";
        else if (StringUtils.containsAny(address.toLowerCase(), "podlaskie", "białystok", "bialystok"))
            return "Podlaskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "pomorskie", "gdańsk", "gdansk"))
            return "Pomorskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "śląskie", "slaskie",  "katowice"))
            return "Śląskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "świętokrzyskie", "swietokrzyskie", "kielce"))
            return "Świętokrzyskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "warmińsko-mazurskie", "warminsko-mazurskie", "warminsko mazurskie", "warmińsko mazurskie","olsztyn"))
            return "Warmińsko-Mazurskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "wielkopolskie", "poznań", "poznan"))
            return "Wielkopolskie";
        else if (StringUtils.containsAny(address.toLowerCase(), "zachodniopomorskie", "szczecin"))
            return "Zachodniopomorskie";
        return "Unrecognized";
    }
}
