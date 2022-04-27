package App;

import App.data.*;
import App.data.Error;
import App.data.user.delete.DeleteResponse;
import App.data.user.get.UserResponse;
import App.data.user.login.LoginRequest;
import App.data.user.login.LoginResponse;
import App.data.user.put.UpdateRequest;
import App.data.user.put.UpdateResponse;
import App.data.user.register.RegisterRequest;
import App.data.user.register.RegisterResponse;
import App.db.FirebaseConnect;
import App.helpers.AdminKey;
import App.helpers.TokenDecryption;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

    @GetMapping("/user")
    public BaseResponse getUser(@RequestParam(value = "token") String token) throws ExecutionException, InterruptedException {
        TokenDecryption tokenDecryption = new TokenDecryption(token);
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("users").document(tokenDecryption.getFirst());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(document.getString("token"), token)||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                UserResponse userResponse = new UserResponse(Math.toIntExact(document.getLong("age")),
                        document.getString("firstName"),
                        document.getString("lastName"),
                        document.getString("phoneNumber"),
                        document.getString("male"),
                        tokenDecryption.getFirst(),
                        document.getString("token"));
                response.setData(userResponse);
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Пользователя с таким email не существует"));
        }
        return response;

    }

    @PostMapping("/register")
    public BaseResponse register(@RequestBody RegisterRequest request) throws ExecutionException, InterruptedException {

        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("users").document(request.getEmail());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Пользователь с таким email уже существует"));
        } else {
            UUID uuid = UUID.randomUUID();
            String randomUUIDString = request.getEmail() + " " + uuid.toString();
            Map<String, Object> data =
                    new ImmutableMap.Builder<String, Object>()
                            .put("firstName", request.getFirstName())
                            .put("lastName", request.getLastName())
                            .put("age", request.getAge())
                            .put("phoneNumber", request.getPhoneNumber())
                            .put("male", request.getMale())
                            .put("password", request.getPassword())
                            .put("token", randomUUIDString)
                            .build();
            RegisterResponse registerResponse = new RegisterResponse(request.getAge(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    request.getMale(),
                    request.getEmail(),
                    randomUUIDString);

            ApiFuture<WriteResult> result = docRef.set(data);
            System.out.println("Update time : " + result.get().getUpdateTime());
            response.setData(registerResponse);
        }
        return response;
    }

    @PostMapping("/login")
    public BaseResponse login(@RequestBody LoginRequest request) throws ExecutionException, InterruptedException {

        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("users").document(request.getEmail());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (request.getPassword().equals(document.getString("password"))) {
                response.setData(new LoginResponse(document.getString("token")));
            } else {
                response = new BaseResponse(ERROR_STATUS, LOGIN_FAILURE);
                response.setData(new Error("Неверный пароль"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Пользователя с таким email не существует"));
        }
        return response;
    }

    @DeleteMapping("/user")
    public BaseResponse deleteUser(@RequestParam(value = "token") String token) throws ExecutionException, InterruptedException {

        TokenDecryption tokenDecryption = new TokenDecryption(token);
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("users").document(tokenDecryption.getFirst());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(document.getString("token"), token)||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                docRef.delete();
                response.setData(new DeleteResponse("Пользователь с email " + tokenDecryption.getFirst() + " успешно удалён"));
            }else{
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Пользователя с таким email не существует"));
        }
        return response;
    }

    @PutMapping("/user")
    public BaseResponse putUser(@RequestBody UpdateRequest request) throws ExecutionException, InterruptedException {

        TokenDecryption tokenDecryption = new TokenDecryption(request.getToken());
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        DocumentReference docRef = connect.collection("users").document(tokenDecryption.getFirst());
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            if (Objects.equals(document.getString("token"), request.getToken())||
                    (Objects.equals(adminToken, tokenDecryption.getToken()))) {
                Map<String, Object> data =
                        new ImmutableMap.Builder<String, Object>()
                                .put("firstName", request.getFirstName())
                                .put("lastName", request.getLastName())
                                .put("age", request.getAge())
                                .put("phoneNumber", request.getPhoneNumber())
                                .put("male", request.getMale())
                                .put("password", request.getPassword())
                                .put("token", request.getToken())
                                .build();
                ApiFuture<WriteResult> result = docRef.set(data);
                System.out.println("Update time : " + result.get().getUpdateTime());
                UpdateResponse updateResponse = new UpdateResponse();
                updateResponse.setAge(request.getAge());
                updateResponse.setFirstName(request.getFirstName());
                updateResponse.setMale(request.getMale());
                updateResponse.setLastName(request.getLastName());
                updateResponse.setPhoneNumber(request.getPhoneNumber());
                response.setData(updateResponse);
            } else {
                response = new BaseResponse(ERROR_STATUS, TOKEN_FAILURE);
                response.setData(new Error("Неверный токен"));
            }
        } else {
            response = new BaseResponse(ERROR_STATUS, EMAIL_FAILURE);
            response.setData(new Error("Пользователя с таким email не существует"));
        }
        return response;
    }
}