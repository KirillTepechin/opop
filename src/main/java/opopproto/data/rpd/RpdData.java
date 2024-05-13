package opopproto.data.rpd;

import lombok.Data;
import opopproto.domain.Competence;
import opopproto.domain.VolumeSemester;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class RpdData {
    private String rpdName;
    private RpdTitle rpdTitle;
    //Обьем дисциплины Таблица1
    private List<VolumeSemester> volumeSemesters;
    private String section3ContainsDiscipline1;

    private List<Competence> competences;
    //Таблица3
    private VolumeSemester volumeTotal;
    private List<EvaluateCompetences> evaluateCompetences = new ArrayList<>();
    //TODO: список литературы
    //private List<BibliographyPair> bibliographyList = new ArrayList<>();
    private AppendixData appendixData;

    public boolean isPractice(){
        Pattern pattern = Pattern.compile("\\(П\\)|\\(У\\)");
        Matcher matcher = pattern.matcher(rpdName);

        return matcher.find();
    }
    public boolean isEduPractice(){
        Pattern pattern = Pattern.compile("\\(У\\)");
        Matcher matcher = pattern.matcher(rpdName);

        return matcher.find();
    }
}
