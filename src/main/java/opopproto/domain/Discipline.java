package opopproto.domain;

import lombok.*;
import opopproto.data.syllabus.VolumeData;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"competences", "volumeData"})
public class Discipline {
    @NonNull
    private String index;
    @NonNull
    private String name;

    private List<Competence> competences = new ArrayList<>();

    @NonNull
    private VolumeData volumeData;

    public Discipline(@NonNull String index, @NonNull String name) {
        this.index = index;
        this.name = name;
    }
}
