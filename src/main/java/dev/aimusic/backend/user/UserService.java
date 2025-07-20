package dev.aimusic.backend.user;

import dev.aimusic.backend.user.dao.UserDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserDao userDao;

}
