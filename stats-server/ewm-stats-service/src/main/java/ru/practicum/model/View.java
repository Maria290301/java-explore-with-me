package ru.practicum.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@EqualsAndHashCode(exclude = "hits")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class View {
    String app;
    String uri;
    Integer hits;

    public View(String app, String uri) {
        this.app = app;
        this.uri = uri;
    }
}