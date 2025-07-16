package dev.aimusic.backend.user.transformer;

import dev.aimusic.backend.user.dao.UserModel;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserTransformer {

    public static UserResponse toUserResponse(UserModel user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
