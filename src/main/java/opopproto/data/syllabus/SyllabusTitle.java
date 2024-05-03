package opopproto.data.syllabus;

import lombok.Data;
import opopproto.domain.ProfessionalArea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SyllabusTitle {
    private String code;
    private String profile;
    private String specialty;
    private String qualification;
    private String educationForm;
    private String period;
    private String startYear;
    private String fgos;

    private List<ProfessionalArea> professionalAreas;
    private ArrayList<String> taskTypes = new ArrayList<>();
}
