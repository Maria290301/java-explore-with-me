package ru.practicum.adminapi;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryCreateRequest;
import ru.practicum.category.dto.CategoryUpdateRequest;
import ru.practicum.event.dto.EventDto;

import ru.practicum.event.EventService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.CategoryService;
import ru.practicum.event.dto.EventUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;
    private final CategoryService categoryService;

    @Autowired
    public AdminEventController(EventService eventService, CategoryService categoryService) {
        this.eventService = eventService;
        this.categoryService = categoryService;
    }

    @PatchMapping("/{id}/publish")
    public EventDto publishEvent(@PathVariable Long id) {
        return eventService.publishEvent(id);
    }

    @PatchMapping("/{id}/reject")
    public EventDto rejectEvent(@PathVariable Long id) {
        return eventService.rejectEvent(id);
    }

    @PatchMapping("/{id}/update")
    public EventDto updateEvent(@PathVariable Long id, @RequestBody EventUpdateRequest request) {
        return eventService.updateEventByAdmin(id, request);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return categoryService.getAllCategories(from, size);
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryCreateRequest request) {
        return categoryService.createCategory(request);
    }

    @PatchMapping("/categories/{id}")
    public CategoryDto updateCategory(@PathVariable Long id,
                                      @RequestBody CategoryUpdateRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        List<EventDto> events = eventService.searchEvents(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{id}")
    public EventDto patchEvent(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        return eventService.processEventUpdate(id, request);
    }
}