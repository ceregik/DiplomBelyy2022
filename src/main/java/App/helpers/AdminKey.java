package App.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class AdminKey {

    private final String adminToken;

    public AdminKey() throws IOException {
        String fileName = "adminKey.txt";
        Optional<String> line = Files.lines(Paths.get(fileName)).findFirst();
        this.adminToken = line.get();
    }

    public String getAdminToken() {
        return adminToken;
    }
}
