package App;

import App.data.BaseResponse;
import App.data.Error;
import App.data.hotel.add.AddHotelRequest;
import App.data.hotel.get.GetHotelResponse;
import App.data.hotel.get.GetHotelsResponse;
import App.data.hotel.get.Hotel;
import App.data.hotel.put.PutHotelRequest;
import App.data.hotel.put.PutHotelResponse;
import App.data.user.delete.DeleteResponse;
import App.db.FirebaseConnect;
import App.helpers.AdminKey;
import App.helpers.TokenDecryption;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final int CODE_SUCCESS = 100;
    private static final int LOGIN_FAILURE = 1;
    private static final int EMAIL_FAILURE = 2;
    private static final int TOKEN_FAILURE = 3;
    private static Firestore connect;
    private static String adminToken = null;

    static {
        try {
            adminToken = new AdminKey().getAdminToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            connect = new FirebaseConnect("diplom-belyy-pi-2022").getDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping()
    public BaseResponse getHotel(@RequestParam(value = "name") String name) throws ExecutionException, InterruptedException {
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("hotels").document(name);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            GetHotelResponse getHotelResponse = new GetHotelResponse();
            getHotelResponse.setAddress(document.getString("address"));
            getHotelResponse.setCity(document.getString("city"));
            getHotelResponse.setCost(document.getLong("cost"));
            getHotelResponse.setDescription(document.getString("description"));
            getHotelResponse.setName(document.getString("name"));
            response.setData(getHotelResponse);
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Отеля с таким названием не существует"));
        }
        return response;
    }

    @GetMapping("/hotels")
    public BaseResponse getHotels() throws ExecutionException, InterruptedException {
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        ApiFuture<QuerySnapshot> future = connect.collection("hotels").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Hotel> hotels = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            hotels.add(document.toObject(Hotel.class));
        }
        GetHotelsResponse getHotelsResponse = new GetHotelsResponse(hotels);
        response.setData(getHotelsResponse);
        return response;
    }

    @PostMapping()
    public BaseResponse addHotel(@RequestParam(value = "token") String token, @RequestBody AddHotelRequest request) throws ExecutionException, InterruptedException {

        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("hotels")
                .document(request.getName().toLowerCase(Locale.ROOT).replace(" ", "_"));
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (Objects.equals(adminToken, token)) {
            if (document.exists()) {
                response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
                response.setData(new Error("Отель с таким названием уже существует"));
            } else {
                Map<String, Object> data =
                        new ImmutableMap.Builder<String, Object>()
                                .put("name", request.getName())
                                .put("address", request.getAddress())
                                .put("city", request.getCity())
                                .put("cost", request.getCost())
                                .put("description", request.getDescription())
                                .build();
                ApiFuture<WriteResult> result = docRef.set(data);
                response.setData(request);
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
            response.setData(new Error("Неверный токен"));
        }
        return response;
    }

    @DeleteMapping()
    public BaseResponse deleteHotel(@RequestParam(value = "token") String token) throws ExecutionException, InterruptedException {
        TokenDecryption tokenDecryption = new TokenDecryption(token);
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("hotels").document(tokenDecryption.getFirst());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(adminToken, tokenDecryption.getToken())) {
                docRef.delete();
                response.setData(new DeleteResponse("Отель с названием " + tokenDecryption.getFirst() + " успешно удалён"));
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Отеля с таким названием не существует"));
        }
        return response;
    }

    @PutMapping()
    public BaseResponse putHotel(@RequestBody PutHotelRequest request) throws ExecutionException, InterruptedException {

        TokenDecryption tokenDecryption = new TokenDecryption(request.getToken());
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("hotels").document(tokenDecryption.getFirst());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(adminToken, tokenDecryption.getToken())) {
                Map<String, Object> data =
                        new ImmutableMap.Builder<String, Object>()
                                .put("address", request.getAddress())
                                .put("city", request.getCity())
                                .put("cost", request.getCost())
                                .put("description", request.getDescription())
                                .build();
                ApiFuture<WriteResult> result = docRef.set(data);
                PutHotelResponse putHotelResponse = new PutHotelResponse();
                putHotelResponse.setName(tokenDecryption.getFirst());
                putHotelResponse.setAddress(request.getAddress());
                putHotelResponse.setCity(request.getCity());
                putHotelResponse.setCost(request.getCost());
                putHotelResponse.setDescription(request.getDescription());
                response.setData(putHotelResponse);
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Отеля с таким названием не существует"));
        }
        return response;
    }
}