package ru.practicum.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.category.Category;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private Compilation compilation;
    private CompilationDto compilationDto;
    private NewCompilationDto newCompilationDto;
    private UpdateCompilationDto updateCompilationDto;
    private Event event;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .id(1L)
                .name("Test category")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("testuser@example.com")
                .build();

        event = Event.builder()
                .id(1L)
                .category(category)
                .initiator(user)
                .build();

        compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(new HashSet<>(List.of(event)))
                .build();

        compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptySet())
                .build();

        newCompilationDto = NewCompilationDto.builder()
                .title("New Compilation")
                .pinned(true)
                .events(Set.of(1L))
                .build();

        updateCompilationDto = UpdateCompilationDto.builder()
                .title("Updated Compilation")
                .pinned(false)
                .events(Set.of(1L))
                .build();
    }

    @Test
    void addCompilation_ShouldSaveAndReturnDto() {
        when(eventRepository.findAllByIdIn(any())).thenReturn(List.of(event));
        when(compilationRepository.save(any())).thenReturn(compilation);

        CompilationDto result = compilationService.addCompilation(newCompilationDto);

        assertNotNull(result);
        assertEquals(compilation.getId(), result.getId());
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void updateCompilation_ShouldUpdateFields() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(eventRepository.findAllByIdIn(any())).thenReturn(List.of(event));

        CompilationDto result = compilationService.updateCompilation(1L, updateCompilationDto);

        assertNotNull(result);
        assertEquals(compilation.getId(), result.getId());
        assertEquals(updateCompilationDto.getTitle(), compilation.getTitle());
        assertEquals(updateCompilationDto.getPinned(), compilation.getPinned());
        verify(compilationRepository, never()).save(any());
    }

    @Test
    void getCompilations_ShouldReturnList() {
        when(compilationRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(compilation)));

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getCompilations_ShouldReturnFiltered() {
        when(compilationRepository.findAllByPinned(eq(true), any(PageRequest.class)))
                .thenReturn(List.of(compilation));

        List<CompilationDto> result = compilationService.getCompilations(true, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void findByIdCompilation_ShouldReturnDto() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.findByIdCompilation(1L);

        assertNotNull(result);
        assertEquals(compilation.getId(), result.getId());
    }

    @Test
    void findByIdCompilation_ShouldThrow_WhenNotFound() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> compilationService.findByIdCompilation(1L));
    }

    @Test
    void deleteCompilation_ShouldCallDelete() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        compilationService.deleteCompilation(1L);

        verify(compilationRepository).deleteById(1L);
    }

    @Test
    void deleteCompilation_ShouldThrow_WhenNotFound() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> compilationService.deleteCompilation(1L));
    }
}
