package cl.tofcompany.sift.Model;

public class Client {
    String id;
    String name;
    String email;
    String password;
    String password2;
    String image;
    public Client() {
    }

    public Client(String id, String name, String email, String password,String password2) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.password2 = password2;
    }

    public Client(String id, String name, String email, String password,String password2, String image) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.password2 = password2;
        this.image = image;
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
