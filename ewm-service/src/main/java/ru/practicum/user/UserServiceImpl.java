package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserUpdateRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists.");
        }
        User user = userMapper.toEntity(request);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<User> users = ids == null || ids.isEmpty()
                ? userRepository.findAll(pageRequest).getContent()
                : userRepository.findAllById(ids);

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id = " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
            user.setName(updateRequest.getName());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            user.setEmail(updateRequest.getEmail());
        }

        user = userRepository.save(user);

        return userMapper.toDto(user);
    }
}
