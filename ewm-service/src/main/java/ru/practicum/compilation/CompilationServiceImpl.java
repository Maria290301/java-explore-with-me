package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UncorrectedParametersException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        compilation.setPinned(Optional.ofNullable(compilation.getPinned()).orElse(false));

        Set<Long> compEventIds = (compilationDto.getEvents() != null) ? compilationDto.getEvents() : Collections.emptySet();
        List<Long> eventIds = new ArrayList<>(compEventIds);
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        Set<Event> eventsSet = new HashSet<>(events);
        compilation.setEvents(eventsSet);

        Compilation compilationAfterSave = compilationRepository.save(compilation);
        return CompilationMapper.toDto(compilationAfterSave);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto update) {
        Compilation compilation = checkCompilation(compId);

        Set<Long> eventIds = update.getEvents();

        if (eventIds != null) {
            List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));
            Set<Event> eventSet = new HashSet<>(events);
            compilation.setEvents(eventSet);
        }

        compilation.setPinned(Optional.ofNullable(update.getPinned()).orElse(compilation.getPinned()));
        if (compilation.getTitle().isBlank()) {
            throw new UncorrectedParametersException("Title не может состоять из пробелов");
        }
        compilation.setTitle(Optional.ofNullable(update.getTitle()).orElse(compilation.getTitle()));

        return CompilationMapper.toDto(compilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        checkCompilation(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        }

        return compilations.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public CompilationDto findByIdCompilation(Long compId) {
        return CompilationMapper.toDto(checkCompilation(compId));
    }

    private Compilation checkCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Compilation с id = " + compId + " не найден"));
    }
}