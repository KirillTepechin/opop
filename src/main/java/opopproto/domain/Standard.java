package opopproto.domain;

import lombok.Data;

import java.util.Objects;

@Data
public class Standard {
    private String code;
    private String name;
    private LaborFunction laborFunction;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Standard standard = (Standard) o;
        return Objects.equals(code.trim().toLowerCase(), standard.code.trim().toLowerCase())
                && Objects.equals(name.trim().toLowerCase(), standard.name.trim().toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code.trim().toLowerCase(), name.trim().toLowerCase());
    }
}
