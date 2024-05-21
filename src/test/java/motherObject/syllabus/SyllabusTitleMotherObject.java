package motherObject.syllabus;

import opopproto.data.syllabus.SyllabusTitle;
import opopproto.domain.ProfessionalArea;

import java.util.ArrayList;
import java.util.List;


public class SyllabusTitleMotherObject {

    private String code = "DEFAULT_CODE";
    private String profile = "DEFAULT_PROFILE";
    private String specialty = "DEFAULT_SPECIALTY";
    private String qualification = "магистр";
    private String educationForm = "DEFAULT_EDUCATION_FORM";
    private String period = "DEFAULT_PERIOD";
    private String startYear = "DEFAULT_START_YEAR";
    private String fgos = "DEFAULT_FGOS";

    private List<ProfessionalArea> professionalAreas = new ArrayList<>();
    private ArrayList<String> taskTypes = new ArrayList<>(List.of("DEFAULT_TASK_TYPE"));

    public SyllabusTitleMotherObject withCode(String code) {
        this.code = code;
        return this;
    }

    public SyllabusTitleMotherObject withProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public SyllabusTitleMotherObject withSpecialty(String specialty) {
        this.specialty = specialty;
        return this;
    }

    public SyllabusTitleMotherObject withQualification(String qualification) {
        this.qualification = qualification;
        return this;
    }

    public SyllabusTitleMotherObject withEducationForm(String educationForm) {
        this.educationForm = educationForm;
        return this;
    }

    public SyllabusTitleMotherObject withPeriod(String period) {
        this.period = period;
        return this;
    }

    public SyllabusTitleMotherObject withStartYear(String startYear) {
        this.startYear = startYear;
        return this;
    }

    public SyllabusTitleMotherObject withFgos(String fgos) {
        this.fgos = fgos;
        return this;
    }

    public SyllabusTitleMotherObject withProfessionalAreas(List<ProfessionalArea> professionalAreas) {
        this.professionalAreas = professionalAreas;
        return this;
    }

    public SyllabusTitleMotherObject withTaskType(String taskType) {
        this.taskTypes.add(taskType);
        return this;
    }

    public SyllabusTitle build() {
        SyllabusTitle syllabusTitle = new SyllabusTitle();
        syllabusTitle.setCode(code);
        syllabusTitle.setProfile(profile);
        syllabusTitle.setSpecialty(specialty);
        syllabusTitle.setQualification(qualification);
        syllabusTitle.setEducationForm(educationForm);
        syllabusTitle.setPeriod(period);
        syllabusTitle.setStartYear(startYear);
        syllabusTitle.setFgos(fgos);
        syllabusTitle.setProfessionalAreas(professionalAreas);
        syllabusTitle.setTaskTypes(taskTypes);
        return syllabusTitle;
    }
}
