package opopproto.data.characteristic;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CharacteristicParagraphData {
    private String titleEqualsSpeciality;
    private String titleEqualsProfile;
    private String titleEqualsQualification;
    private String titleEqualsEducationForm;
    private String titleContainsSpeciality;
    private String titleContainsProfile;
    private String annotationContainsOverallZe;
    private String section11ContainsSpeciality;
    private String section12ContainsSpeciality;
    private String section12ContainsFgos;

    //TODO: Добавить проверку сферы проф деятельности
    //private String section21ContainsAllProfAreas;
    private List<String> section21ListOfTaskTypes = new ArrayList<>();
    private String section22ContainsSpeciality;

    private String section31ContainsSpeciality;
    private String section31ContainsProfile;
    private String section31ContainsAllTaskTypes;
    private String section32ContainsQualification;
    private String section34ContainsEducationForm;

    //TODO: добавить проверку срока обучения
    //private String section35ContainsPeriod;
    private String section541ContainsSpeciality;
    private String section541ContainsProfile;
    private String section545ContainsSpeciality;

    private String appendixAContainsSpeciality;
    private String appendixAContainsProfile;

    private String appendixBContainsSpeciality;
    private String appendixBContainsProfile;
}
