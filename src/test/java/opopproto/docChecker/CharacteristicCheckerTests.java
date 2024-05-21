package opopproto.docChecker;

import motherObject.characteristic.CharacteristicParagraphDataMotherObject;
import motherObject.characteristic.CharacteristicTableDataMotherObject;
import motherObject.syllabus.SyllabusTitleMotherObject;
import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.characteristic.CharacteristicParagraphData;
import opopproto.data.characteristic.CharacteristicTableData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.data.syllabus.SyllabusTitle;
import opopproto.docChecker.characteristic.CharacteristicComplianceStateChecker;
import opopproto.domain.Competence;
import opopproto.util.StandardComparator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharacteristicCheckerTests {

    private CharacteristicComplianceStateChecker characteristicComplianceStateChecker =
            new CharacteristicComplianceStateChecker(new StandardComparator());

    private static SyllabusData syllabusData = new SyllabusData();
    private CharacteristicData characteristicData = new CharacteristicData();
    @BeforeAll
    private static void loadData(){
        SyllabusTitle syllabusTitle = new SyllabusTitleMotherObject()
                .withProfile("Правильный профиль")
                .withTaskType("научно-исследовательский")
                .build();
        syllabusData.setSyllabusTitle(syllabusTitle);

        Competence uCompetence = new Competence();
        uCompetence.setIndex("УК-1");
        uCompetence.setName("Компетенция");
        Competence pCompetence = new Competence();
        pCompetence.setIndex("ПК-2");
        pCompetence.setName("Компетенция");
        syllabusData.setCompetences(new ArrayList<>(List.of(uCompetence, pCompetence)));
    }
    @Test
    public void shouldContainTitleProfileErrorMessage() {
        CharacteristicParagraphData characteristicParagraphData = new CharacteristicParagraphDataMotherObject()
                .withTitleEqualsProfile("Другой профиль")
                .build();
        characteristicData.setParagraphData(characteristicParagraphData);

        assertTrue(characteristicComplianceStateChecker.check(syllabusData, characteristicData)
                .contains("В титульном листе не указана/неправильно указана программа подготовки"));
    }

    @Test
    public void shouldContainSection31TaskTypesErrorMessage() {
        CharacteristicParagraphData characteristicParagraphData = new CharacteristicParagraphDataMotherObject()
                .withSection31ContainsAllTaskTypes("педагогический")
                .build();
        characteristicData.setParagraphData(characteristicParagraphData);

        assertTrue(characteristicComplianceStateChecker.check(syllabusData, characteristicData)
                .contains("В разделе 3.1 не указаны/неправильно указаны типы задач профессиональной деятельности"));
    }

    @Test
    public void shouldNotContainUCompetencesErrorMessage() {
        Competence competence = new Competence();
        competence.setIndex("УК-1");
        competence.setName("Компетенция");

        CharacteristicTableData characteristicTableData = new CharacteristicTableDataMotherObject()
                .withUCompetence(competence)
                .build();

        characteristicData.setTableData(characteristicTableData);
        assertFalse(characteristicComplianceStateChecker.check(syllabusData, characteristicData)
                .contains("<b>Ошибка в таблице универсальных компетенций." +
                        "</b><br>Наименование или индекс компетенций в УК-1 не совпадают с учебным планом." +
                        " Возможен неправильный порядок компетенций или отсутствие пробелов между словами." +
                        "<br>Компетенции с ошибками:<br>УК-1 Компетенция"));
    }

    @Test
    public void shouldContainPCompetencesErrorMessage() {
        Competence competence = new Competence();
        competence.setIndex("ПК-1");
        competence.setName("Компетенция");

        CharacteristicTableData characteristicTableData = new CharacteristicTableDataMotherObject()
                .withPCompetence(competence)
                .build();

        characteristicData.setTableData(characteristicTableData);
        assertTrue(characteristicComplianceStateChecker.check(syllabusData, characteristicData)
                .contains("<b>Ошибка в таблице профессиональных компетенций." +
                        "</b><br>Наименование или индекс компетенций в ПК-1 не совпадают с учебным планом." +
                        " Возможен неправильный порядок компетенций или отсутствие пробелов между словами." +
                        "<br>Компетенции с ошибками:<br>ПК-1 Компетенция"));
    }
}
