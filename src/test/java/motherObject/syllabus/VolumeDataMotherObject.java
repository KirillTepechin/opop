package motherObject.syllabus;

import opopproto.data.syllabus.VolumeData;
import opopproto.domain.VolumeSemester;
import opopproto.domain.VolumeTotal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class VolumeDataMotherObject {


    private HashSet<Integer> semesters = new HashSet<>();
    private List<String> controlForm = new ArrayList<>();
    private List<VolumeSemester> volumesBySemester = new ArrayList<>();
    private VolumeTotal overallVolume = new VolumeTotal();

    public VolumeDataMotherObject withSemester(int semester) {
        this.semesters.add(semester);
        return this;
    }

    public VolumeDataMotherObject withControlForm(String controlForm) {
        this.controlForm.add(controlForm);
        return this;
    }

    public VolumeDataMotherObject withVolumeBySemester(VolumeSemester volumeSemester) {
        this.volumesBySemester.add(volumeSemester);
        return this;
    }

    public VolumeDataMotherObject withOverallVolume(VolumeTotal overallVolume) {
        this.overallVolume = overallVolume;
        return this;
    }

    public VolumeData build() {
        VolumeData volumeData = new VolumeData();
        volumeData.setSemesters(semesters);
        volumeData.setControlForm(controlForm);
        volumeData.setVolumesBySemester(volumesBySemester);
        volumeData.setOverallVolume(overallVolume);
        return volumeData;
    }
}
