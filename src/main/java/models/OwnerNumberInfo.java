package models;

import java.util.Objects;

public class OwnerNumberInfo {

    private final String key;
    private final String index;

    public OwnerNumberInfo(String key, String index) {
        this.key = key;
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnerNumberInfo that = (OwnerNumberInfo) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, index);
    }
}
