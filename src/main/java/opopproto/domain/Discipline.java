package opopproto.domain;

import lombok.*;
import opopproto.data.syllabus.VolumeData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"competences", "volumeData"})
public class Discipline {
    @NonNull
    private String index;
    @NonNull
    private String name;

    private List<Competence> competences = new ArrayList<>();

    @NonNull
    private VolumeData volumeData;

    public Discipline(@NonNull String index, @NonNull String name) {
        this.index = index;
        this.name = name;
    }

    public boolean isPractice(){
        Pattern pattern = Pattern.compile("\\(П\\)|\\(У\\)");
        Matcher matcher = pattern.matcher(index);

        return matcher.find();
    }
    public boolean isEduPractice(){
        Pattern pattern = Pattern.compile("\\(У\\)");
        Matcher matcher = pattern.matcher(index);

        return matcher.find();
    }

    public boolean isB3(){
        return index.startsWith("Б3");
    }

    public boolean isFTD(){
        return index.startsWith("ФТД");
    }

    public String getCorrectNameForTitle(){
        if(isPractice()){
            if(isEduPractice()){
                return "Учебная практика: " + name;
            }
            else {
               return "Производственная практика: " + name;
            }
        } else if (isB3()) {
            return "подготовке к процедуре защиты и защита выпускной квалификационной работы";
        }
        return name;
    }
    public List<Competence> getUCompetences(){
        return competences.stream().filter(competence -> competence.getIndex().trim().startsWith("УК-")).toList();
    }

    public List<Competence> getOpCompetences(){
        return competences.stream().filter(competence -> competence.getIndex().trim().startsWith("ОПК-")).toList();
    }
    public List<Competence> getPCompetences(){
        return competences.stream().filter(competence -> competence.getIndex().trim()
                .matches("^(УКи-\\d+|ОПКи-\\d+|ПК-\\d+)*")).toList();
    }
}
