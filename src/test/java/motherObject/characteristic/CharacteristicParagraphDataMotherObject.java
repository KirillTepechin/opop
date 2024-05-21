package motherObject.characteristic;

import opopproto.data.characteristic.CharacteristicParagraphData;

import java.util.ArrayList;
import java.util.List;

public class CharacteristicParagraphDataMotherObject {
    private String titleEqualsSpeciality = "DEFAULT_SPECIALITY";
    private String titleEqualsProfile = "DEFAULT_PROFILE";
    private String titleEqualsQualification = "DEFAULT_QUALIFICATION";
    private String titleEqualsEducationForm = "DEFAULT_EDUCATION_FORM";
    private String titleContainsSpeciality = "DEFAULT_SPECIALITY";
    private String titleContainsProfile = "DEFAULT_PROFILE";
    private String section11ContainsSpeciality = "DEFAULT_SPECIALITY";
    private String section12ContainsSpeciality = "DEFAULT_SPECIALITY";
    private String section12ContainsFgos = "DEFAULT_FGOS";
    private List<String> section21ListOfTaskTypes = new ArrayList<>();
    private String section22ContainsSpeciality = "DEFAULT_SPECIALITY";
    private String section31ContainsSpeciality = "DEFAULT_SPECIALITY";
    private String section31ContainsProfile = "DEFAULT_PROFILE";
    private String section31ContainsAllTaskTypes = "DEFAULT_TASK_TYPE";
    private String section32ContainsQualification = "DEFAULT_QUALIFICATION";
    private String section34ContainsEducationForm = "DEFAULT_EDUCATION_FORM";
    private String section541ContainsSpeciality = "DEFAULT_SPECIALITY";
    private String section541ContainsProfile = "DEFAULT_PROFILE";
    private String section545ContainsSpeciality = "DEFAULT_SPECIALITY";
    private String appendixAContainsSpeciality = "DEFAULT_SPECIALITY";
    private String appendixAContainsProfile = "DEFAULT_PROFILE";
    private String appendixBContainsSpeciality = "DEFAULT_SPECIALITY";
    private String appendixBContainsProfile = "DEFAULT_PROFILE";


    public CharacteristicParagraphDataMotherObject withTitleEqualsProfile(String titleEqualsProfile) {
        this.titleEqualsProfile = titleEqualsProfile;
        return this;
    }

    public CharacteristicParagraphDataMotherObject withSection31ContainsAllTaskTypes(String taskTypes) {
        this.section31ContainsAllTaskTypes += taskTypes;
        return this;
    }


    public CharacteristicParagraphData build() {
        CharacteristicParagraphData characteristicParagraphData = new CharacteristicParagraphData();
        characteristicParagraphData.setTitleEqualsSpeciality(titleEqualsSpeciality);
        characteristicParagraphData.setTitleEqualsProfile(titleEqualsProfile);
        characteristicParagraphData.setTitleEqualsQualification(titleEqualsQualification);
        characteristicParagraphData.setTitleEqualsEducationForm(titleEqualsEducationForm);
        characteristicParagraphData.setTitleContainsSpeciality(titleContainsSpeciality);
        characteristicParagraphData.setTitleContainsProfile(titleContainsProfile);
        characteristicParagraphData.setSection11ContainsSpeciality(section11ContainsSpeciality);
        characteristicParagraphData.setSection12ContainsSpeciality(section12ContainsSpeciality);
        characteristicParagraphData.setSection12ContainsFgos(section12ContainsFgos);
        characteristicParagraphData.setSection21ListOfTaskTypes(section21ListOfTaskTypes);
        characteristicParagraphData.setSection22ContainsSpeciality(section22ContainsSpeciality);
        characteristicParagraphData.setSection31ContainsSpeciality(section31ContainsSpeciality);
        characteristicParagraphData.setSection31ContainsProfile(section31ContainsProfile);
        characteristicParagraphData.setSection31ContainsAllTaskTypes(section31ContainsAllTaskTypes);
        characteristicParagraphData.setSection32ContainsQualification(section32ContainsQualification);
        characteristicParagraphData.setSection34ContainsEducationForm(section34ContainsEducationForm);
        characteristicParagraphData.setSection541ContainsSpeciality(section541ContainsSpeciality);
        characteristicParagraphData.setSection541ContainsProfile(section541ContainsProfile);
        characteristicParagraphData.setSection545ContainsSpeciality(section545ContainsSpeciality);
        characteristicParagraphData.setAppendixAContainsSpeciality(appendixAContainsSpeciality);
        characteristicParagraphData.setAppendixAContainsProfile(appendixAContainsProfile);
        characteristicParagraphData.setAppendixBContainsSpeciality(appendixBContainsSpeciality);
        characteristicParagraphData.setAppendixBContainsProfile(appendixBContainsProfile);
        return characteristicParagraphData;
    }
}
