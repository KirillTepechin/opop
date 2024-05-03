package opopproto.domain;

import lombok.Data;

import java.util.*;

@Data
public class LaborFunction {
    private String code;
    private String name;
    private Set<LaborFunction> laborFunctions = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LaborFunction that = (LaborFunction) o;
        return Objects.equals(code.trim().toLowerCase(), that.code.trim().toLowerCase())
                && Objects.equals(name.trim().toLowerCase(), that.name.trim().toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code.trim().toLowerCase(), name.trim().toLowerCase());
    }
}
