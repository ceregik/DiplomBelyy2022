package App.db;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
// [START firestore_deps]
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
// [END firestore_deps]
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableMap;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple Quick start application demonstrating how to connect to Firestore
 * and add and query documents.
 */
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

    void runQuery() throws Exception {
        // [START fs_add_query]
        // asynchronously query for all users born before 1900
        ApiFuture<QuerySnapshot> query =
                db.collection("users").whereLessThan("born", 1900).get();
        // ...
        // query.get() blocks on response
        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            System.out.println("User: " + document.getId());
            System.out.println("First: " + document.getString("first"));
            if (document.contains("middle")) {
                System.out.println("Middle: " + document.getString("middle"));
            }
            System.out.println("Last: " + document.getString("last"));
            System.out.println("Born: " + document.getLong("born"));
        }
        // [END fs_add_query]
    }

    void retrieveAllDocuments() throws Exception {
        // [START fs_get_all]
        // asynchronously retrieve all users
        ApiFuture<QuerySnapshot> query = db.collection("users").get();
        // ...
        // query.get() blocks on response
        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            System.out.println("User: " + document.getId());
            System.out.println("First: " + document.getString("first"));
            if (document.contains("middle")) {
                System.out.println("Middle: " + document.getString("middle"));
            }
            System.out.println("Last: " + document.getString("last"));
            System.out.println("Born: " + document.getLong("born"));
        }
        // [END fs_get_all]
    }

    void run() throws Exception {
        String[] docNames = {"alovelace", "aturing", "cbabbage"};
        // retrieve all users born before 1900
        System.out.println("########## users born before 1900 ##########");
        runQuery();

        // retrieve all users
        System.out.println("########## All users ##########");
        retrieveAllDocuments();
        System.out.println("###################################");
    }

    /**
     * A quick start application to get started with Firestore.
     *
     * @param args firestore-project-id (optional)
     */
//    public static void main(String[] args) throws Exception {
//        // default project is will be used if project-id argument is not available
//        String projectId = "diplom-belyy-pi-2022";
//        FirebaseConnect quickStart = (projectId != null) ? new FirebaseConnect(projectId) : new FirebaseConnect();
//        quickStart.run();
//        quickStart.close();
//    }

    /** Closes the gRPC channels associated with this instance and frees up their resources. */
    void close() throws Exception {
        db.close();
    }
}