package motherObject.rpd;

import opopproto.data.rpd.AppendixData;
import opopproto.data.rpd.EvaluateCompetences;
import opopproto.data.rpd.RpdData;
import opopproto.data.rpd.RpdTitle;
import opopproto.domain.Competence;
import opopproto.domain.VolumeSemester;

import java.util.ArrayList;
import java.util.List;

public class RpdDataMotherObject {
    private String rpdName = "DEFAULT_RPD_NAME";
    private RpdTitle rpdTitle = new RpdTitle();
    private List<VolumeSemester> volumeSemesters = new ArrayList<>();
    private String section3ContainsDiscipline1 = "DEFAULT_SECTION";
    private List<Competence> competences = new ArrayList<>();
    private VolumeSemester volumeTotal = new VolumeSemester();
    private List<EvaluateCompetences> evaluateCompetences = new ArrayList<>();
    private AppendixData appendixData = null;

    public RpdDataMotherObject withRpdName(String rpdName) {
        this.rpdName = rpdName;
        return this;
    }

    public RpdDataMotherObject withRpdTitle(RpdTitle rpdTitle) {
        this.rpdTitle = rpdTitle;
        return this;
    }

    public RpdDataMotherObject withVolumeSemesters(List<VolumeSemester> volumeSemesters) {
        this.volumeSemesters = volumeSemesters;
        return this;
    }

    public RpdDataMotherObject withSection3ContainsDiscipline1(String section) {
        this.section3ContainsDiscipline1 = section;
        return this;
    }

    public RpdDataMotherObject withCompetences(List<Competence> competences) {
        this.competences = competences;
        return this;
    }

    public RpdDataMotherObject withVolumeTotal(VolumeSemester volumeTotal) {
        this.volumeTotal = volumeTotal;
        return this;
    }

    public RpdDataMotherObject withEvaluateCompetences(List<EvaluateCompetences> evaluateCompetences) {
        this.evaluateCompetences = evaluateCompetences;
        return this;
    }

    public RpdDataMotherObject withAppendixData(AppendixData appendixData) {
        this.appendixData = appendixData;
        return this;
    }

    public RpdData build() {
        RpdData rpdData = new RpdData();
        rpdData.setRpdName(rpdName);
        rpdData.setRpdTitle(rpdTitle);
        rpdData.setVolumeSemesters(volumeSemesters);
        rpdData.setSection3ContainsDiscipline1(section3ContainsDiscipline1);
        rpdData.setCompetences(competences);
        rpdData.setVolumeTotal(volumeTotal);
        rpdData.setEvaluateCompetences(evaluateCompetences);
        rpdData.setAppendixData(appendixData);
        return rpdData;
    }
}

