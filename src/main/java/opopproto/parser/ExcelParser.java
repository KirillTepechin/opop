package opopproto.parser;

import opopproto.data.syllabus.SyllabusData;
import opopproto.data.syllabus.SyllabusTitle;
import opopproto.data.syllabus.VolumeData;
import opopproto.domain.*;
import opopproto.data.syllabus.DisciplinesData;
import opopproto.exception.InvalidSyllabusException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Component
public class ExcelParser {
    private Workbook workbook;
    @Autowired
    private SyllabusData syllabusData;
    public SyllabusData parse(FileInputStream fis, String extension) throws IOException {
        //Проверяем расширение, библиотека использует разные реализации для разных расширений
        if(extension.equals("xlsx")){
            workbook = new XSSFWorkbook(fis);
        }
        else{
            workbook = new HSSFWorkbook(fis);
        }
        try {
            syllabusData.setSyllabusTitle(parseTitle());
            syllabusData.setDisciplinesData(parseDisciplines());
            syllabusData.setCompetences(parseCompetence2(parseCompetence()));
        }
        catch (Exception e){
            throw new InvalidSyllabusException();
        }

        return syllabusData;
    }
    private SyllabusTitle parseTitle(){
        SyllabusTitle syllabusTitle = new SyllabusTitle();
        Sheet titleSheet = workbook.getSheetAt(0);

        syllabusTitle.setCode(titleSheet.getRow(16).getCell(2).getStringCellValue());
        syllabusTitle.setSpecialty(titleSheet.getRow(18).getCell(2).getStringCellValue());
        syllabusTitle.setProfile(titleSheet.getRow(19).getCell(2).getStringCellValue());
        syllabusTitle.setQualification(titleSheet.getRow(29).getCell(1).getStringCellValue().split(": ")[1]);
        syllabusTitle.setStartYear(titleSheet.getRow(29).getCell(20).getStringCellValue());
        syllabusTitle.setEducationForm(titleSheet.getRow(31).getCell(1).getStringCellValue().split(": ")[1]);
        syllabusTitle.setFgos(titleSheet.getRow(31).getCell(20).getStringCellValue());
        syllabusTitle.setPeriod(titleSheet.getRow(32).getCell(1).getStringCellValue().split(": ")[1]);


        //Ищем определенную строку
        int rowIndex = 36; // Начальная позиция строки, с которой начнем поиск
        Row currentRow = null;
        List<ProfessionalArea> professionalAreas = new ArrayList<>();
        ProfessionalArea professionalArea = null;
        //Парсим компетенции и сферы проф деятельности
        do {
            currentRow = titleSheet.getRow(rowIndex);
            // Проверяем, что строка не пустая
            if (currentRow.getCell(2) != null) {
                //Если это сфера проф деятельности
                if(currentRow.getCell(1).getStringCellValue().length()<3){
                    if(rowIndex!=36){
                        professionalAreas.add(professionalArea);
                    }
                    professionalArea = new ProfessionalArea();
                    String areaCode = currentRow.getCell(1).getStringCellValue();
                    String areaName = currentRow.getCell(2).getStringCellValue();
                    professionalArea.setName(areaName);
                    professionalArea.setCode(areaCode);
                }
                else{
                    Standard competence = new Standard();
                    String competenceCode = currentRow.getCell(1).getStringCellValue();
                    String competenceName = currentRow.getCell(2).getStringCellValue();
                    competence.setName(competenceName);
                    competence.setCode(competenceCode);

                    professionalArea.getStandards().add(competence);
                }
            }
            rowIndex++;
        } while (currentRow.getCell(2)!=null);  // Пока строка не пустая
        //Добавляем последнюю
        professionalAreas.add(professionalArea);
        syllabusTitle.setProfessionalAreas(professionalAreas);

        rowIndex+=1;

        ArrayList<String> taskTypes = new ArrayList<>();
        //Парсим типы задач
        do {
            currentRow = titleSheet.getRow(rowIndex);
            if(currentRow.getCell(1) != null){
                // Проверяем, что строка не пустая
                if (!currentRow.getCell(1).getCellType().equals(CellType.BLANK)) {
                    taskTypes.add(currentRow.getCell(2).getStringCellValue());
                }
            }
            rowIndex+=1;
        } while (currentRow.getCell(1)!=null);  // Пока строка не пустая

        syllabusTitle.setTaskTypes(taskTypes);

        return syllabusTitle;
    }
    private DisciplinesData parseDisciplines(){
        Sheet disciplinesSheet = workbook.getSheetAt(3);
        DisciplinesData disciplinesData = new DisciplinesData();
        //Ищем определенную строку
        int rowIndex = 5; // Начальная позиция строки, с которой начнем поиск
        Row currentRow = null;
        //Парсим дисциплины
        int parsingStagesCount = 6;
        boolean byChoiceFlag = false;
        for (int i = 0; i < parsingStagesCount; i++) {
            do {
                currentRow = disciplinesSheet.getRow(rowIndex);
                if(currentRow==null){
                    //Дошли до конца документа
                    break;
                }
                if (currentRow.getCell(0).getStringCellValue().equals("+") ||
                        currentRow.getCell(0).getStringCellValue().equals("-")) {
                    //Если парсим 1 блок обязательные дисциплины
                    if(i==0){
                        disciplinesData.getBlock1DisciplineList().add(
                                new DisciplineExtended(currentRow.getCell(2).getStringCellValue(),
                                        currentRow.getCell(4).getStringCellValue(), parseVolume(currentRow, false))
                        );
                        disciplinesData.setOverallZe(disciplinesData.getOverallZe()
                                + Integer.parseInt(currentRow.getCell(16).getStringCellValue()));
                    }
                    //Если парсим 1 блок вариативные дисциплины
                    else if (i==1) {
                        //Если встретили "Дисциплины по выбору"
                        if(currentRow.getCell(4).getStringCellValue().startsWith("Дисциплины по выбору")){
                            byChoiceFlag = true;
                        }
                        //Если дисциплина по выбору
                        else if (byChoiceFlag) {
                            disciplinesData.getBlock1DisciplineList().add(
                                    new DisciplineExtended(currentRow.getCell(1).getStringCellValue(),
                                            currentRow.getCell(3).getStringCellValue(),parseVolume(currentRow, true) , false, true)
                            );
                            if(currentRow.getCell(0).getStringCellValue().equals("+")){
                                disciplinesData.setOverallZe(disciplinesData.getOverallZe()
                                        + Integer.parseInt(currentRow.getCell(15).getStringCellValue()));
                            }
                        }
                        //Дисциплина не по выбору
                        else {
                            disciplinesData.getBlock1DisciplineList().add(
                                    new DisciplineExtended(currentRow.getCell(2).getStringCellValue(),
                                            currentRow.getCell(4).getStringCellValue(),parseVolume(currentRow, false) , false, false));
                            disciplinesData.setOverallZe(disciplinesData.getOverallZe()
                                    + Integer.parseInt(currentRow.getCell(16).getStringCellValue()));
                        }
                    }
                    //Если парсим блок 2 обязательные дисциплины
                    else if (i==2) {
                        disciplinesData.getBlock2DisciplineList().add(
                                new DisciplineExtended(currentRow.getCell(2).getStringCellValue(),
                                        currentRow.getCell(4).getStringCellValue(), parseVolume(currentRow, false))
                        );
                        disciplinesData.setOverallZe(disciplinesData.getOverallZe()
                                + Integer.parseInt(currentRow.getCell(16).getStringCellValue()));
                    }
                    //Если парсим блок 2 вариативные дисциплины
                    else if (i==3) {
                        disciplinesData.getBlock2DisciplineList().add(
                                new DisciplineExtended(currentRow.getCell(2).getStringCellValue(),
                                        currentRow.getCell(4).getStringCellValue(),parseVolume(currentRow, false)  ,false, false)
                        );
                        disciplinesData.setOverallZe(disciplinesData.getOverallZe()
                                + Integer.parseInt(currentRow.getCell(16).getStringCellValue()));
                    }
                    //Если парсим блок 3
                    else if (i==4) {
                        disciplinesData.getBlock3DisciplineList().add(
                                new Discipline(currentRow.getCell(2).getStringCellValue(),
                                        currentRow.getCell(4).getStringCellValue(), parseVolume(currentRow, false))
                        );
                        disciplinesData.setOverallZe(disciplinesData.getOverallZe()
                                + Integer.parseInt(currentRow.getCell(16).getStringCellValue()));
                    }
                    //Если парсим факультативы
                    else {
                        disciplinesData.getBlock4DisciplineList().add(
                                new Discipline(currentRow.getCell(2).getStringCellValue(),
                                        currentRow.getCell(4).getStringCellValue(), parseVolume(currentRow, false))
                        );
                    }

                }
                rowIndex++;
            } while (currentRow.getCell(0).getStringCellValue().equals("+")||
                    currentRow.getCell(0).getStringCellValue().equals("-"));
            //Перескакиваем
            if(i==1){
                rowIndex++;
            }
        }

        return disciplinesData;
    }

    private VolumeData parseVolume(Row row, boolean byChoice){
        int ind = 0;
        if(byChoice) ind = 1;
        HashSet<Integer> semesters = new HashSet<>();
        List<String> controlForm = new ArrayList<>();
        Cell ekzCell = row.getCell(6-ind);
        Cell zachetCell = row.getCell(8-ind);
        Cell zachetWithCell = row.getCell(10-ind);
        Cell kpCell = row.getCell(12-ind);
        Cell krCell = row.getCell(14-ind);

        if(ekzCell != null && !ekzCell.getCellType().equals(CellType.BLANK)){
            semesters.add(Integer.parseInt(ekzCell.getStringCellValue()));
            controlForm.add("экзамен");
        }
        if(zachetCell != null && !zachetCell.getCellType().equals(CellType.BLANK)){
            semesters.add(Integer.parseInt(zachetCell.getStringCellValue()));
            controlForm.add("зачет");
        }
        if(zachetWithCell != null && !zachetWithCell.getCellType().equals(CellType.BLANK)){
            semesters.add(Integer.parseInt(zachetWithCell.getStringCellValue()));
            controlForm.add("зачет с оценкой");
        }
        if(kpCell != null && !kpCell.getCellType().equals(CellType.BLANK)){
            semesters.add(Integer.parseInt(kpCell.getStringCellValue()));
            controlForm.add("курсовой проект");
        }
        if(krCell != null && !krCell.getCellType().equals(CellType.BLANK)){
            semesters.add(Integer.parseInt(krCell.getStringCellValue()));
            controlForm.add("курсовая работа");
        }

        VolumeTotal overallVolume = new VolumeTotal();
        overallVolume.setZeCount(Integer.parseInt(row.getCell(16-ind).getStringCellValue()));
        overallVolume.setTotal(Integer.parseInt(row.getCell(22-ind).getStringCellValue()));
        overallVolume.setIw(Integer.parseInt(row.getCell(28-ind).getStringCellValue()));
        try {
            overallVolume.setControl(Integer.parseInt(row.getCell(30-ind).getStringCellValue()));
        }
        catch (NumberFormatException ignored){
        }

        List<VolumeSemester> volumeSemesters = new ArrayList<>();
        if(semesters.contains(1)){
            VolumeSemester volumeSemester1 = new VolumeSemester();
            if(!row.getCell(32-ind).getCellType().equals(CellType.BLANK))
                volumeSemester1.setZeCount(Integer.parseInt(row.getCell(32-ind).getStringCellValue()));
            if(!row.getCell(34-ind).getCellType().equals(CellType.BLANK))
                volumeSemester1.setLectures(Integer.parseInt(row.getCell(34-ind).getStringCellValue()));
            if(!row.getCell(36-ind).getCellType().equals(CellType.BLANK))
                volumeSemester1.setLw(Integer.parseInt(row.getCell(36-ind).getStringCellValue()));
            if(!row.getCell(38-ind).getCellType().equals(CellType.BLANK))
                volumeSemester1.setPw(Integer.parseInt(row.getCell(38-ind).getStringCellValue()));
            if(!row.getCell(40-ind).getCellType().equals(CellType.BLANK))
                volumeSemester1.setIw(Integer.parseInt(row.getCell(40-ind).getStringCellValue()));
            if(!row.getCell(42-ind).getCellType().equals(CellType.BLANK))
                volumeSemester1.setControl(Integer.parseInt(row.getCell(42-ind).getStringCellValue()));

            volumeSemester1.setSemester(1);
            volumeSemesters.add(volumeSemester1);
        }
        if(semesters.contains(2)){
            VolumeSemester volumeSemester2 = new VolumeSemester();
            if(!row.getCell(44-ind).getCellType().equals(CellType.BLANK))
                volumeSemester2.setZeCount(Integer.parseInt(row.getCell(44-ind).getStringCellValue()));
            if(!row.getCell(46-ind).getCellType().equals(CellType.BLANK))
                volumeSemester2.setLectures(Integer.parseInt(row.getCell(46-ind).getStringCellValue()));
            if(!row.getCell(48-ind).getCellType().equals(CellType.BLANK))
                volumeSemester2.setLw(Integer.parseInt(row.getCell(48-ind).getStringCellValue()));
            if(!row.getCell(50-ind).getCellType().equals(CellType.BLANK))
                volumeSemester2.setPw(Integer.parseInt(row.getCell(50-ind).getStringCellValue()));
            if(!row.getCell(52-ind).getCellType().equals(CellType.BLANK))
                volumeSemester2.setIw(Integer.parseInt(row.getCell(52-ind).getStringCellValue()));
            if(!row.getCell(54-ind).getCellType().equals(CellType.BLANK))
                volumeSemester2.setControl(Integer.parseInt(row.getCell(54-ind).getStringCellValue()));

            volumeSemester2.setSemester(2);
            volumeSemesters.add(volumeSemester2);
        }
        if(semesters.contains(3)){
            VolumeSemester volumeSemester3 = new VolumeSemester();
            if(!row.getCell(56-ind).getCellType().equals(CellType.BLANK))
                volumeSemester3.setZeCount(Integer.parseInt(row.getCell(56-ind).getStringCellValue()));
            if(!row.getCell(58-ind).getCellType().equals(CellType.BLANK))
                volumeSemester3.setLectures(Integer.parseInt(row.getCell(58-ind).getStringCellValue()));
            if(!row.getCell(60-ind).getCellType().equals(CellType.BLANK))
                volumeSemester3.setLw(Integer.parseInt(row.getCell(60-ind).getStringCellValue()));
            if(!row.getCell(62-ind).getCellType().equals(CellType.BLANK))
                volumeSemester3.setIw(Integer.parseInt(row.getCell(62-ind).getStringCellValue()));
            if(!row.getCell(64-ind).getCellType().equals(CellType.BLANK))
                volumeSemester3.setControl(Integer.parseInt(row.getCell(64-ind).getStringCellValue()));

            volumeSemester3.setSemester(3);
            volumeSemesters.add(volumeSemester3);
        }
        if(semesters.contains(4)){
            VolumeSemester volumeSemester4 = new VolumeSemester();
            if(!row.getCell(66-ind).getCellType().equals(CellType.BLANK))
                volumeSemester4.setZeCount(Integer.parseInt(row.getCell(66-ind).getStringCellValue()));
            if(!row.getCell(68-ind).getCellType().equals(CellType.BLANK))
                volumeSemester4.setIw(Integer.parseInt(row.getCell(68-ind).getStringCellValue()));
            if(!row.getCell(70-ind).getCellType().equals(CellType.BLANK))
                volumeSemester4.setControl(Integer.parseInt(row.getCell(70-ind).getStringCellValue()));

            volumeSemester4.setSemester(4);
            volumeSemesters.add(volumeSemester4);
        }
        VolumeData volumeData = new VolumeData();
        volumeData.setSemesters(semesters);
        volumeData.setControlForm(controlForm);
        volumeData.setOverallVolume(overallVolume);
        volumeData.setVolumesBySemester(volumeSemesters);

        return volumeData;
    }
    private List<Competence> parseCompetence(){
        Sheet competenceSheet = workbook.getSheetAt(4);
        List<Competence> competences = new ArrayList<>();
        //Ищем определенную строку
        int rowIndex = 2; // Начальная позиция строки, с которой начнем поиск
        Row currentRow = null;

        //Парсим компетенции
        do {
            currentRow = competenceSheet.getRow(rowIndex);
            // Проверяем, что строка не пустая
            if (currentRow.getCell(1) != null) {
                Competence competence = new Competence();
                competence.setIndex(currentRow.getCell(1).getStringCellValue());
                competence.setName(currentRow.getCell(3).getStringCellValue());

                rowIndex++;
                currentRow = competenceSheet.getRow(rowIndex);
                while(currentRow!=null && !currentRow.getCell(2).getCellType().equals(CellType.BLANK)){
                    Row finalCurrentRow = currentRow;
                    Discipline discipline = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                            .filter(disc -> disc.getIndex().equals(finalCurrentRow.getCell(2).getStringCellValue()))
                            .toList().get(0);

                    competence.getDisciplines().add(discipline);
                    //Связываем дисциплины и компетенции
                    discipline.getCompetences().add(competence);
                    rowIndex++;
                    currentRow = competenceSheet.getRow(rowIndex);
                }
                competences.add(competence);

            }
        } while (currentRow!=null);  // Пока строка не пустая


        return competences;
    }
    private List<Competence> parseCompetence2(List<Competence> competences){
        Sheet competenceSheet = workbook.getSheetAt(7);
        //Ищем определенную строку
        int rowIndex = 1; // Начальная позиция строки, с которой начнем поиск
        Row currentRow = null;
        Competence findedCompetence = null;
        //Парсим компетенции
        do {
            currentRow = competenceSheet.getRow(rowIndex);
            // Проверяем, что строка не пустая
            if (currentRow.getCell(5) != null) {

                //Если на этой строке начинается компетенция
                if(!currentRow.getCell(0).getCellType().equals(CellType.BLANK)) {

                    String compIndex = currentRow.getCell(0).getStringCellValue();

                    findedCompetence = competences.stream()
                            .filter(competence -> competence.getIndex().equals(compIndex)).toList().get(0);
                    currentRow = competenceSheet.getRow(++rowIndex);
                    //Пока не встретили след компетенцию или закончился документ
                    while (currentRow != null && currentRow.getCell(0)
                            .getCellType().equals(CellType.BLANK)) {
                        currentRow = competenceSheet.getRow(rowIndex);
                        //Если встретили стандарт, то парсим его
                        if (!currentRow.getCell(1).getCellType().equals(CellType.BLANK)) {

                            Standard standard = new Standard();
                            standard.setCode(currentRow.getCell(1).getStringCellValue());
                            standard.setName(currentRow.getCell(5).getStringCellValue());
                            currentRow = competenceSheet.getRow(++rowIndex);

                            LaborFunction laborFunction = new LaborFunction();
                            laborFunction.setCode(currentRow.getCell(2).getStringCellValue());
                            laborFunction.setName(currentRow.getCell(5).getStringCellValue());
                            standard.setLaborFunction(laborFunction);

                            currentRow = competenceSheet.getRow(++rowIndex);
                            while(!currentRow.getCell(3).getCellType().equals(CellType.BLANK)
                                    || !currentRow.getCell(4).getCellType().equals(CellType.BLANK)){
                                if(!currentRow.getCell(3).getCellType().equals(CellType.BLANK)){
                                    LaborFunction subFunction = new LaborFunction();
                                    subFunction.setCode(currentRow.getCell(3).getStringCellValue());
                                    subFunction.setName(currentRow.getCell(5).getStringCellValue());
                                    laborFunction.getLaborFunctions().add(subFunction);
                                }
                                currentRow = competenceSheet.getRow(++rowIndex);
                                if(currentRow==null){
                                    break;
                                }
                            }
                            currentRow = competenceSheet.getRow(--rowIndex);

                            findedCompetence.getStandards().add(standard);

                        }
                        currentRow = competenceSheet.getRow(++rowIndex);
                    }
                }

            }
        } while (currentRow!=null);  // Пока строка не пустая

        return competences;
    }

}
