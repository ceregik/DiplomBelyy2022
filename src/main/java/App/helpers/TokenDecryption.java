package App.helpers;

public class TokenDecryption {

    private String email;
    private String token;

    public TokenDecryption(String allToken) {
        String[] words = allToken.split(" ");
        this.email = words[0];
        this.token = words[1];
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
