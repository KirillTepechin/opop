package opopproto.data.syllabus;

import lombok.Data;
import opopproto.domain.Discipline;
import opopproto.domain.DisciplineExtended;

import java.util.ArrayList;
import java.util.List;
@Data
public class DisciplinesData {
    //Дисциплины
    private List<DisciplineExtended> block1DisciplineList = new ArrayList<>();
    //Практики
    private List<DisciplineExtended> block2DisciplineList = new ArrayList<>();
    //ГИА
    private List<Discipline> block3DisciplineList = new ArrayList<>();
    //Факультативы
    private List<Discipline> block4DisciplineList = new ArrayList<>();

    private int overallZe;

    public List<Discipline> getBaseDisciplines(){
        List<Discipline> baseDisciplines = new ArrayList<>();
        baseDisciplines.addAll(block1DisciplineList.stream().filter(DisciplineExtended::isBase).toList());
        baseDisciplines.addAll(block2DisciplineList.stream().filter(DisciplineExtended::isBase).toList());

        return baseDisciplines;
    }

    public List<Discipline> getVaryDisciplines(){
        List<Discipline> baseDisciplines = new ArrayList<>();
        baseDisciplines.addAll(block1DisciplineList.stream().filter(disciplineExtended -> !disciplineExtended.isBase()).toList());
        baseDisciplines.addAll(block2DisciplineList.stream().filter(disciplineExtended -> !disciplineExtended.isBase()).toList());

        return baseDisciplines;
    }

    public List<Discipline> getAllDisciplines(){
        List<Discipline> allDisciplines = new ArrayList<>();
        allDisciplines.addAll(block1DisciplineList);
        allDisciplines.addAll(block2DisciplineList);
        allDisciplines.addAll(block3DisciplineList);
        allDisciplines.addAll(block4DisciplineList);

        return allDisciplines;
    }

    public List<Discipline> getAllDisciplinesWithoutB3(){
        List<Discipline> allDisciplines = new ArrayList<>();
        allDisciplines.addAll(block1DisciplineList);
        allDisciplines.addAll(block2DisciplineList);
        allDisciplines.addAll(block4DisciplineList);

        return allDisciplines;
    }

}
