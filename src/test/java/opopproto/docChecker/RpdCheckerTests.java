package opopproto.docChecker;

import motherObject.rpd.RpdDataMotherObject;
import motherObject.syllabus.SyllabusTitleMotherObject;
import motherObject.syllabus.VolumeDataMotherObject;
import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.fos.FosData;
import opopproto.data.rpd.AppendixData;
import opopproto.data.rpd.RpdData;
import opopproto.data.syllabus.DisciplinesData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.data.syllabus.SyllabusTitle;
import opopproto.data.syllabus.VolumeData;
import opopproto.docChecker.characteristic.CharacteristicComplianceStateChecker;
import opopproto.docChecker.fos.FosComplianceStateChecker;
import opopproto.docChecker.rpd.RpdComplianceStateChecker;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.domain.VolumeSemester;
import opopproto.domain.VolumeTotal;
import opopproto.util.Documents;
import opopproto.util.StandardComparator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RpdCheckerTests {

    private RpdComplianceStateChecker rpdComplianceStateChecker =
            new RpdComplianceStateChecker(new Documents());

    private static SyllabusData syllabusData = new SyllabusData();
    private RpdData rpdData = new RpdData();

    @BeforeAll
    private static void loadData(){
        VolumeTotal volumeTotal = new VolumeTotal();
        volumeTotal.setTotal(30);
        VolumeData volumeData = new VolumeDataMotherObject()
                .withOverallVolume(volumeTotal)
                .withControlForm("зачет")
                .build();

        Discipline discipline = new Discipline("index", "disc");
        discipline.setVolumeData(volumeData);
        DisciplinesData disciplinesData = new DisciplinesData();
        disciplinesData.getBlock4DisciplineList().add(discipline);
        SyllabusTitle syllabusTitle = new SyllabusTitleMotherObject().build();
        syllabusData.setDisciplinesData(disciplinesData);
        syllabusData.setSyllabusTitle(syllabusTitle);
    }

    @Test
    public void shouldContainTable3TotalHoursError(){
        VolumeSemester volumeSemester = new VolumeSemester();
        volumeSemester.setContactWork(10);
        volumeSemester.setControl(10);

        RpdData rpdData = new RpdDataMotherObject()
                .withRpdName("index disc")
                .withVolumeTotal(volumeSemester)
                .build();

        assertTrue(rpdComplianceStateChecker.check(List.of(rpdData), syllabusData, null)
                .contains("<b>Ошибки в таблице содержания дисциплины (Таблица 3) РПД</b>.<br><br>В РПД документе '"+
                        rpdData.getRpdName() + "' количество <i>итоговых</i> часов не совпадает с планом. План - " +
                        30 + " ч"));
    }

    @Test
    public void shouldContainAppendixControlFormsError(){
        AppendixData appendixData = new AppendixData();
        appendixData.setControlForms(List.of("экзамен"));
        appendixData.setEqualsLevel("магистратура");
        appendixData.setEqualsQualification("магистр");
        appendixData.setTotalHours(30);
        appendixData.setEqualsDiscipline("disc");
        appendixData.setEqualsSpeciality("DEFAULT_SPECIALTY");
        appendixData.setEqualsProfile("DEFAULT_PROFILE");
        RpdData rpdData = new RpdDataMotherObject()
                .withRpdName("index disc")
                .withAppendixData(appendixData)
                .build();

        assertTrue(rpdComplianceStateChecker.check(List.of(rpdData), syllabusData, null)
                .contains("<b>Ошибки в Приложении А РПД</b>.<br><br>РПД документ '" + rpdData.getRpdName() +
                        "' не содержит форму промежуточной аттестации <i>зачет</i>"));
    }
}
