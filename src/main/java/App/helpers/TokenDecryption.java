package App.helpers;

public class TokenDecryption {

    private String first;
    private String token;

    public TokenDecryption(String allToken) {
        String[] words = allToken.split(" ");
        this.first = words[0];
        this.token = words[1];
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
