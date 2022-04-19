package App.data.user.delete;

public class DeleteResponse {

    private String text;

    public DeleteResponse(String text) {
        this.text = text;
    }

    public DeleteResponse() {
    }

    public String getText() {
        return text;
    }

    public void setText(String token) {
        this.text = text;
    }
}