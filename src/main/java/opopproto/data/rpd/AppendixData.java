package opopproto.data.rpd;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AppendixData {
    String equalsDiscipline;
    String equalsLevel;
    String equalsQualification;
    String equalsSpeciality;
    String equalsProfile;
    List<String> competencesIndexes = new ArrayList<>();
    Integer zeCount;
    Integer totalHours;
    List<String> controlForms;
}
