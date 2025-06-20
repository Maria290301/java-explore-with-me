package ru.practicum.compilation;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto dto);

    void deleteCompilation(Long id);

    CompilationDto updateCompilation(Long id, UpdateCompilationDto dto);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long id);
}
