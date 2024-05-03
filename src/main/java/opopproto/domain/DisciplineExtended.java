package opopproto.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import opopproto.data.syllabus.VolumeData;

@EqualsAndHashCode(callSuper = true)
@Data
public class DisciplineExtended extends Discipline {
    private boolean base = true;
    private boolean byChoice = false;

    public DisciplineExtended(String index, String name, VolumeData volumeData) {
        super(index, name, volumeData);
    }
    public DisciplineExtended(String index, String name, VolumeData volumeData, boolean base, boolean byChoice) {
        super(index, name, volumeData);
        this.setBase(base);
        this.setByChoice(byChoice);
    }
}
