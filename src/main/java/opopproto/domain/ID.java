package opopproto.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ID {
    private String index;
    private String name;

    public String getName() {
        return name.trim()
                .replaceAll("[^\\p{L}\\s]+$", "")
                .replaceAll("\n", " ")
                .replace("\u00a0"," ");
    }
}
