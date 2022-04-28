package App;

import App.data.BaseResponse;
import App.data.Error;
import App.data.booking.add.AddBookingRequest;
import App.data.booking.add.AddBookingResponse;
import App.data.booking.get.Booking;
import App.data.booking.get.GetBookingResponse;
import App.data.booking.get.GetBookingsResponse;
import App.data.booking.put.PutBookingRequest;
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
@RequestMapping("/booking")
public class BookingController {

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final int CODE_SUCCESS = 100;
    private static final int LOGIN_FAILURE = 1;
    private static final int EMAIL_FAILURE = 2;
    private static final int TOKEN_FAILURE = 3;
    private static final int HOTEL_FAILURE = 4;
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
    public BaseResponse getBooking(@RequestParam(value = "token") String token, @RequestParam(value = "id") String id) throws ExecutionException, InterruptedException {
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        TokenDecryption tokenDecryption = new TokenDecryption(token);
        DocumentReference docRef = connect.collection("bookings").document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(document.getString("token"), token) ||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                GetBookingResponse getBookingResponse = new GetBookingResponse();
                getBookingResponse.setEmail(document.getString("email"));
                getBookingResponse.setDateFrom(document.getString("dateFrom"));
                getBookingResponse.setToken(document.getString("token"));
                getBookingResponse.setDateTo(document.getString("dateTo"));
                getBookingResponse.setPerson(document.getString("person"));
                getBookingResponse.setNameHotel(document.getString("nameHotel"));
                getBookingResponse.setId(document.getString("id"));
                response.setData(getBookingResponse);
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Такого бронирования не существует"));
        }
        return response;
    }

    @GetMapping("/bookings")
    public BaseResponse getBookings(@RequestParam(value = "token") String token) throws
            ExecutionException, InterruptedException {
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        if (Objects.equals(adminToken, token)) {
            ApiFuture<QuerySnapshot> future = connect.collection("bookings").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Booking> bookings = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                bookings.add(document.toObject(Booking.class));
            }
            GetBookingsResponse getBookingsResponse = new GetBookingsResponse(bookings);
            response.setData(getBookingsResponse);
        } else {
            response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
            response.setData(new Error("Неверный токен"));
        }
        return response;
    }

    @PostMapping()
    public BaseResponse addBooking(@RequestBody AddBookingRequest request) throws
            ExecutionException, InterruptedException {

        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        TokenDecryption tokenDecryption = new TokenDecryption(request.getToken());
        String uuid = UUID.randomUUID().toString();
        DocumentReference docRef = connect.collection("bookings")
                .document(uuid);

        ApiFuture<DocumentSnapshot> futureForUser = connect.collection("users").document(tokenDecryption.getFirst()).get();
        DocumentSnapshot documentForUser = futureForUser.get();

        ApiFuture<DocumentSnapshot> futureForHotel = connect.collection("hotels").document(
                request.getNameHotel().toLowerCase(Locale.ROOT).replace(" ", "_")).get();
        DocumentSnapshot documentForHotel = futureForHotel.get();
        if (documentForUser.exists()) {
            if (Objects.equals(documentForUser.getString("token"), request.getToken()) ||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                if (documentForHotel.exists()) {
                    Map<String, Object> data =
                            new ImmutableMap.Builder<String, Object>()
                                    .put("email", tokenDecryption.getFirst())
                                    .put("nameHotel", request.getNameHotel())
                                    .put("dateFrom", request.getDateFrom())
                                    .put("dateTo", request.getDateTo())
                                    .put("person", request.getPerson())
                                    .put("token", request.getToken())
                                    .put("id", uuid)
                                    .build();
                    ApiFuture<WriteResult> result = docRef.set(data);
                    AddBookingResponse addBookingResponse = new AddBookingResponse();
                    addBookingResponse.setEmail(tokenDecryption.getFirst());
                    addBookingResponse.setDateFrom(request.getDateFrom());
                    addBookingResponse.setDateTo(request.getDateTo());
                    addBookingResponse.setPerson(request.getPerson());
                    addBookingResponse.setNameHotel(request.getNameHotel());
                    addBookingResponse.setId(uuid);
                    response.setData(addBookingResponse);
                } else {
                    response = new BaseResponse(ERROR_STATUS, HOTEL_FAILURE);
                    response.setData(new Error("Нет такого отеля"));
                    return response;
                }
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
                return response;
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Пользователя с таким email не существует"));
            return response;
        }
        return response;
    }

    @DeleteMapping()
    public BaseResponse deleteBooking(@RequestParam(value = "token") String token,
                                      @RequestParam(value = "id") String id) throws ExecutionException, InterruptedException {

        TokenDecryption tokenDecryption = new TokenDecryption(token);
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("bookings").document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(document.getString("token"), token) ||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                docRef.delete();
                response.setData(new DeleteResponse("Бронирование успешно удалёно"));
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Такого бронирования не существует"));
        }
        return response;
    }

    //done
    @PutMapping()
    public BaseResponse putBooking(@RequestParam(value = "id") String id, @RequestBody PutBookingRequest request) throws
            ExecutionException, InterruptedException {

        TokenDecryption tokenDecryption = new TokenDecryption(request.getToken());
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("bookings").document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(document.getString("token"), request.getToken()) ||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                Map<String, Object> data =
                        new ImmutableMap.Builder<String, Object>()
                                .put("email", tokenDecryption.getFirst())
                                .put("nameHotel", request.getNameHotel())
                                .put("dateFrom", request.getDateFrom())
                                .put("dateTo", request.getDateTo())
                                .put("person", request.getPerson())
                                .put("token", request.getToken())
                                .put("id", id)
                                .build();
                ApiFuture<WriteResult> result = docRef.set(data);
                AddBookingResponse addBookingResponse = new AddBookingResponse();
                addBookingResponse.setEmail(tokenDecryption.getFirst());
                addBookingResponse.setDateFrom(request.getDateFrom());
                addBookingResponse.setDateTo(request.getDateTo());
                addBookingResponse.setPerson(request.getPerson());
                addBookingResponse.setNameHotel(request.getNameHotel());
                addBookingResponse.setId(id);
                response.setData(addBookingResponse);
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Такого бронирования не существует"));
        }
        return response;
    }
}