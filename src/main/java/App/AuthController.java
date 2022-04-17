package App;

import App.data.*;
import App.data.Error;
import App.data.login.LoginRequest;
import App.data.login.LoginResponse;
import App.data.register.RegisterRequest;
import App.data.register.RegisterResponse;
import App.db.FirebaseConnect;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.collect.ImmutableMap;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final String sharedKey = "SHARED_KEY";

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final int CODE_SUCCESS = 100;
    private static final int LOGIN_FAILURE = 1;
    private static final int EMAIL_FAILURE = 2;
    private static Firestore connect;

    static {
        try {
            connect = new FirebaseConnect("diplom-belyy-pi-2022").getDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping
    public BaseResponse showStatus() {
        BaseResponse response = new BaseResponse(SUCCESS_STATUS, 1);
        response.setData(new ResponseGet(new Date()));
        return response;
    }

    @PostMapping("/pay")
    public BaseResponse pay(@RequestParam(value = "key") String key, @RequestBody PaymentRequest request) {

        final BaseResponse response;

        if (sharedKey.equalsIgnoreCase(key)) {
            int userId = request.getUserId();
            String itemId = request.getItemId();
            double discount = request.getDiscount();
            // Process the request
            // ....
            // Return success response to the client.
            response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        } else {
            response = new BaseResponse(ERROR_STATUS, LOGIN_FAILURE);
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
            String randomUUIDString = uuid.toString();
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
            if (request.getPassword().equals(document.getString("password"))){
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
}