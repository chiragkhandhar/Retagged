package ml.chiragkhandhar.retagged;

import java.io.Serializable;

public class Explore implements Serializable {
    private String name;
    private String address;
    private int distance;
    private String type;
    private String photoURL;

    public Explore() {
        this.name = "";
        this.address = "";
        this.distance = 0;
        this.type = "";
        this.photoURL = "";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
