package ru.practicum.compilation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.dto.EventDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompilationMapperTest {

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private CompilationMapper compilationMapper;

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Test Compilation");
        dto.setPinned(true);
        Set<Event> events = Set.of(new Event());

        Compilation result = compilationMapper.toEntity(dto, events);

        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.isPinned());
        assertEquals(events, result.getEvents());
    }

    @Test
    void toDto_shouldMapFieldsCorrectly() {
        Event event = new Event();
        Compilation compilation = new Compilation();
        compilation.setId(1L);
        compilation.setTitle("Compilation Title");
        compilation.setPinned(false);
        compilation.setEvents(Set.of(event));

        EventDto eventDto = new EventDto();
        eventDto.setId(1L);

        when(eventMapper.toDto(event)).thenReturn(eventDto);

        CompilationDto dto = compilationMapper.toDto(compilation);

        assertEquals(1L, dto.getId());
        assertEquals("Compilation Title", dto.getTitle());
        assertFalse(dto.isPinned());
        assertEquals(1, dto.getEvents().size());
        assertEquals(1L, dto.getEvents().get(0).getId());
    }
}
