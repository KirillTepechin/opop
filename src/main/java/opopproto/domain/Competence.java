package opopproto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@ToString(exclude = {"disciplines", "standards", "ids"})
//@EqualsAndHashCode(exclude = {"disciplines", "standards", "ids"})
public class Competence {
    private String index;
    private String name;
    private List<Discipline> disciplines = new ArrayList<>();
    private List<Standard> standards = new ArrayList<>();
    private List<ID> ids = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Competence that = (Competence) o;
        return Objects.equals(index, that.index) && Objects.equals(trimName(name), trimName(that.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, trimName(name));
    }

    private String trimName(String name){
        return name.trim()
                .replaceAll("[^\\p{L}\\s]+$", "")
                .replaceAll("\n", " ")
                .replace("\u00a0","");
    }
}
