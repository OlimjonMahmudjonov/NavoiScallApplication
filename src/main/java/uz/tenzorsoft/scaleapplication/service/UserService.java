package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.UserEntity;
import uz.tenzorsoft.scaleapplication.domain.request.UserRequest;
import uz.tenzorsoft.scaleapplication.domain.response.UserResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.UserSendResponse;
import uz.tenzorsoft.scaleapplication.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements BaseService<UserEntity, UserResponse, UserRequest> {

    private final UserRepository userRepository;


    public UserResponse validateUser(String phoneNumber, String password) {
        UserEntity user = userRepository.findByPhoneNumberAndPassword(phoneNumber, password)
                .orElse(null);
        if (user == null) {
            return null;
        }
        System.out.println("Instances.currentUser = " + Instances.currentUser);
        Instances.currentUser = user;
        System.out.println("Instances.currentUser = " + Instances.currentUser);
        return entityToResponse(user);
    }

    public List<UserSendResponse> getNotSentData() {
        List<UserSendResponse> result = new ArrayList<>();
        List<UserEntity> notSentData = userRepository.findTop10ByIsSentToCloud(false);
        for (UserEntity user : notSentData) {
            UserSendResponse response = new UserSendResponse(
                    user.getPhoneNumber(), user.getUsername(), user.getPassword()
            );
            response.setId(user.getId());
            response.setIdOnServer(user.getIdOnServer());
            result.add(response);
        }
        return result;
    }

    public void dataSent(List<UserSendResponse> notSentData, Map<Long, Long> userMap) {
        if (userMap == null || userMap.isEmpty()) {
            return;
        }
        notSentData.forEach(user -> {
            UserEntity entity = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException(user.getId() + " is not found from database"));
            entity.setIsSentToCloud(true);
            entity.setIdOnServer(userMap.get(entity.getId()));
            userRepository.save(entity);
        });

    }

    @Override
    public UserResponse entityToResponse(UserEntity entity) {
        return new UserResponse(entity.getUsername(), entity.getPassword(), entity.getPhoneNumber());
    }

    @Override
    public UserEntity requestToEntity(UserRequest request) {
        return null;
    }

    public UserResponse save(UserEntity userEntity) {
        return entityToResponse(userRepository.save(userEntity));
    }

    public UserEntity findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    public void create(String phoneNumber, String password) {
        UserEntity user = new UserEntity(phoneNumber, password, phoneNumber, null, null);
        userRepository.save(user);
        System.out.println("Instances.currentUser = " + Instances.currentUser);
        Instances.currentUser = user;
        System.out.println("Instances.currentUser = " + Instances.currentUser);
    }
}

