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

    public void setEqualsSpeciality(String equalsSpeciality){
        this.equalsSpeciality = equalsSpeciality
                .replaceAll("\"", "")
                .replaceAll("«","")
                .replaceAll("»", "")
                .replaceAll("/", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    public void setEqualsDiscipline(String equalsDiscipline){
        this.equalsDiscipline = equalsDiscipline
                .replaceAll("\"", "")
                .replaceAll("«","")
                .replaceAll("»", "")
                .trim()
                .replaceAll("\\s+", " ");

        if(checkIndex(this.equalsDiscipline.split(" ")[0])){
            this.equalsDiscipline = equalsDiscipline.substring(equalsDiscipline.indexOf(" ") + 1);
        }
    }

    private boolean checkIndex(String index) {
        return index.matches("Б3\\.\\d{2}|" +
                "Б[123]\\.([ВО]\\.\\d{2}|В\\.ДВ\\.\\d{2}\\.\\d{2}|[ВО]\\.\\d{2}\\([ПУ]\\))|" +
                "ФТД\\.\\d{2}");
    }

}
