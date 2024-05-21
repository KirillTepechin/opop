package opopproto.docChecker;

import motherObject.fos.FosDataMotherObject;
import opopproto.data.fos.FosData;
import opopproto.data.syllabus.DisciplinesData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.docChecker.fos.FosComplianceStateChecker;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.util.Documents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FosCheckerTests {
    private FosComplianceStateChecker fosComplianceStateChecker =
            new FosComplianceStateChecker(new Documents());

    private static SyllabusData syllabusData = new SyllabusData();
    private FosData fosData = new FosData();

    @BeforeAll
    private static void loadData(){
        Discipline disciplineFtd = new Discipline("ФТД.01", "disc");
        Discipline disciplinePractice = new Discipline("Б2.О.01(У)", "Какая то практика");
        Competence uCompetence = new Competence();
        uCompetence.setIndex("УК-1");
        uCompetence.setName("Компетенция");
        disciplinePractice.getCompetences().add(uCompetence);
        disciplineFtd.getCompetences().add(uCompetence);
        uCompetence.getDisciplines().add(disciplineFtd);
        uCompetence.getDisciplines().add(disciplinePractice);
        syllabusData.setCompetences(new ArrayList<>(List.of(uCompetence)));

        DisciplinesData disciplinesData = new DisciplinesData();
        disciplinesData.getBlock4DisciplineList().add(disciplineFtd);
        disciplinesData.getBlock4DisciplineList().add(disciplinePractice);
        syllabusData.setDisciplinesData(disciplinesData);
    }

    @Test
    public void shouldContainAppendixError(){
        fosData = new FosDataMotherObject()
                .withAppendixesExisting(Map.of("1", false))
                .withFosName("ФТД.01 disc")
                .build();
        assertTrue(fosComplianceStateChecker.check(List.of(fosData), List.of(), syllabusData, null)
                .contains("<b>Отсутствующие приложения в ФОС документах:</b><br><br>" +
                        "В документе ФТД.01 disc, Приложения : '1'"));
    }

    @Test
    public void shouldNotContainCorrectPracticeError(){
        fosData = new FosDataMotherObject()
                .withFosName("Б2.О.01(У) Какая то практика")
                .withTitleEqualsDiscipline("Учебная практика: какая то практика")
                .build();

        assertFalse(fosComplianceStateChecker.check(List.of(fosData), List.of(), syllabusData, null)
                .contains("<b>Неправильное название дисциплины в титульном листе в документах:</b><br><br>" +
                        fosData.getFosName()));
    }
}
