package opopproto.data.syllabus;

import lombok.Data;
import opopproto.domain.Volume;
import opopproto.domain.VolumeSemester;
import opopproto.domain.VolumeTotal;

import java.util.*;

@Data
public class VolumeData {
    HashSet<Integer> semesters = new HashSet<>();
    List<String> controlForm = new ArrayList<>();
    private List<VolumeSemester> volumesBySemester = new ArrayList<>();
    private VolumeTotal overallVolume = new VolumeTotal();
}
