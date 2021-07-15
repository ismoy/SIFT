package cl.tofcompany.sift.Model;

public class Driver {
    String id;
    String name;
    String email;
    String password;
    String password2;
    String vehicleBrand;
    String vehiclePlate;
    String image;

    public Driver() {
    }

    public Driver(String id, String name, String email, String password, String password2,String vehicleBrand, String vehiclePlate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.password2 = password2;
        this.vehicleBrand = vehicleBrand;
        this.vehiclePlate = vehiclePlate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }
}
