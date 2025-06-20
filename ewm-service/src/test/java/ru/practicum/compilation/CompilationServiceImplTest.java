package ru.practicum.compilation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private CompilationMapper compilationMapper;

    @InjectMocks
    private CompilationServiceImpl service;

    @Test
    void createCompilation_shouldSaveAndReturnDto() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("New Compilation");
        dto.setPinned(true);
        dto.setEvents(List.of(1L, 2L));

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        Compilation compilation = new Compilation();
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(10L);

        when(eventRepository.findAllById(dto.getEvents())).thenReturn(List.of(event1, event2));
        when(compilationMapper.toEntity(dto, Set.of(event1, event2))).thenReturn(compilation);
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        CompilationDto result = service.createCompilation(dto);

        assertEquals(10L, result.getId());
    }

    @Test
    void deleteCompilation_shouldThrowIfNotFound() {
        when(compilationRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.deleteCompilation(1L));
    }

    @Test
    void updateCompilation_shouldUpdateFields() {
        Compilation compilation = new Compilation();
        compilation.setTitle("Old Title");

        UpdateCompilationDto dto = new UpdateCompilationDto();
        dto.setTitle("Updated");
        dto.setPinned(true);
        dto.setEvents(List.of(1L));

        Event event = new Event();
        CompilationDto expectedDto = new CompilationDto();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(eventRepository.findAllById(dto.getEvents())).thenReturn(List.of(event));
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(expectedDto);

        CompilationDto result = service.updateCompilation(1L, dto);

        assertEquals(expectedDto, result);
    }

    @Test
    void getCompilationById_shouldReturnDto() {
        Compilation compilation = new Compilation();
        CompilationDto dto = new CompilationDto();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toDto(compilation)).thenReturn(dto);

        assertEquals(dto, service.getCompilationById(1L));
    }
}
