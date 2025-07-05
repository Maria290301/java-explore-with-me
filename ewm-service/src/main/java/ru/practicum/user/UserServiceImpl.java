package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addNewUser(NewUserRequest newUserRequest) {
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с email " + newUserRequest.getEmail() + " уже существует");
        }

        User user = userRepository.save(UserMapper.toUser(newUserRequest));
        return UserMapper.toUserDto(user);
    }


    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id= " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getListUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return (ids != null)
                ? userRepository.findByIdIn(ids, page).stream().map(UserMapper::toUserDto).collect(Collectors.toList())
                : userRepository.findAll(page).stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

}