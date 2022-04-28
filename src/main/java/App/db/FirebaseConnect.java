package App.db;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.FileInputStream;

public class FirebaseConnect {

    private Firestore db;

    public FirebaseConnect(String projectId) throws Exception {
        FileInputStream serviceAccount = new FileInputStream("./diplom-belyy-pi-2022-firebase-adminsdk-hduuv-c21b8dcfde.json");
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(projectId)
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
        Firestore db = firestoreOptions.getService();
        this.db = db;
    }

    public Firestore getDb() {
        return db;
    }

}