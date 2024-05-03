package opopproto.data.syllabus;

import lombok.Data;
import opopproto.domain.Volume;
import opopproto.domain.VolumeSemester;
import opopproto.domain.VolumeTotal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class VolumeData {
    private Map<Integer, String> semesterControlForm = new HashMap<>();
    private List<VolumeSemester> volumesBySemester = new ArrayList<>();
    private VolumeTotal overallVolume = new VolumeTotal();
}
