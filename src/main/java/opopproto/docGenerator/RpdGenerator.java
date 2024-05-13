package opopproto.docGenerator;

import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.*;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Component
public class RpdGenerator extends AbstractGenerator{

    @Override
    void generate(SyllabusData syllabusData) throws IOException {
        FileUtils.forceMkdir(new File(documents.getDOCS_GEN_PATH()+"/Рабочие программы дисциплин"));

        List<Discipline> disciplines = syllabusData.getDisciplinesData().getAllDisciplinesWithoutB3();
        for (var discipline : disciplines) {
            FileInputStream fis;
            if(discipline.isPractice()){
                fis = new FileInputStream(documents.getRPD_PRACTICE_TEMPLATE_PATH());
            }
            else{
                fis = new FileInputStream(documents.getRPD_TEMPLATE_PATH());
            }
            document = new XWPFDocument(fis);


            String disciplineName = discipline.getName();
            String baseOrVary;
            String blockAndIndex;
            if(discipline.isFTD()){
                baseOrVary = "элективной части";
                blockAndIndex = "ФТД";
            }
            else{
                if(syllabusData.getDisciplinesData().getVaryDisciplines().contains(discipline)){
                    baseOrVary = "части, формируемой участниками образовательных отношений";
                }
                else{
                    baseOrVary = "обязательной части";
                }
                blockAndIndex = discipline.getIndex().charAt(0) + " " + discipline.getIndex().charAt(1);
            }

            String syllabusSpecialty = syllabusData.getSyllabusTitle().getSpecialty();
            String syllabusProfile = syllabusData.getSyllabusTitle().getProfile();
            String correctPracticeName = discipline.isPractice()?discipline.getCorrectNameForTitle():"";
            String practiceForm = discipline.isPractice()?discipline.isEduPractice()?"учебная":"производственная":"";
            String practiceType = discipline.isPractice()?discipline.getName().toLowerCase():"";

            for (var paragraph: document.getParagraphs()) {
                String text = paragraph.getText();
                if (text.contains("#disciplineName")) {
                    replaceTextInParagraph(paragraph,"#disciplineName", disciplineName );
                }
                if (text.contains("#syllabusSpecialty")) {
                    replaceTextInParagraph(paragraph,"#syllabusSpecialty", syllabusSpecialty );
                }
                if (text.contains("#baseOrVary")) {
                    replaceTextInParagraph(paragraph,"#baseOrVary", baseOrVary );
                }
                if (text.contains("#blockAndIndex")) {
                    replaceTextInParagraph(paragraph,"#blockAndIndex", blockAndIndex );
                }
                if (text.contains("#syllabusProfile")) {
                    replaceTextInParagraph(paragraph,"#syllabusProfile", syllabusProfile );
                }
                if (text.contains("#correctPracticeName")) {
                    replaceTextInParagraph(paragraph,"#correctPracticeName", correctPracticeName );
                    if(text.trim().equals("#correctPracticeName")){
                        paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                        paragraph.getRuns().get(0).setBold(true);
                        paragraph.getRuns().get(0).setFontSize(16);
                    }
                }
                if (text.contains("#practiceForm")) {
                    replaceTextInParagraph(paragraph,"#practiceForm", practiceForm );
                }
                if (text.contains("#practiceType")) {
                    replaceTextInParagraph(paragraph,"#practiceType", practiceType );
                }
            }
            String syllabusQualification = syllabusData.getSyllabusTitle().getQualification();
            String syllabusLevel = getLevelByQualification(syllabusQualification);
            List<VolumeSemester> volumesBySemester = discipline.getVolumeData().getVolumesBySemester();
            int disciplineLectures = volumesBySemester.stream().mapToInt(VolumeSemester::getLectures).sum();
            int disciplinePw = volumesBySemester.stream().mapToInt(VolumeSemester::getPw).sum();
            int disciplineLw = volumesBySemester.stream().mapToInt(VolumeSemester::getLw).sum();
            //TODO: здесь пока считаем СР как контроль+СР
            int disciplineIw = volumesBySemester.stream().mapToInt(value -> value.getControl()+value.getIw()).sum();
            int disciplineTotal = discipline.getVolumeData().getOverallVolume().getTotal();
            int disciplineZeCount = discipline.getVolumeData().getOverallVolume().getZeCount();
            String disciplineControlForms = String.join(", ", discipline.getVolumeData().getControlForm());
            for (var table : document.getTables()) {
                if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Дисциплина (модуль)") && table.getRows().size()==6) {
                    table.getRow(0).getCell(1).setText(disciplineName);
                    table.getRow(2).getCell(1).setText(syllabusLevel);
                    table.getRow(4).getCell(1).setText(syllabusQualification);
                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Уровень образования") && table.getRows().size()==4) {
                    table.getRow(0).getCell(1).setText(syllabusLevel);
                    table.getRow(2).getCell(1).setText(syllabusQualification);
                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("на кафедре")) {
                    table.getRow(2).getCell(1).setText(syllabusSpecialty);
                    table.getRow(3).getCell(1).setText(syllabusProfile);
                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Форма обучения")) {
                    if(discipline.isPractice()){
                        VolumeSemester volumeSemester = volumesBySemester.get(0);

                        setCellText(table.getRow(1).getCell(1),
                                String.valueOf(volumeSemester.getSemester()));
                        setCellText(table.getRow(3).getCell(1),
                                String.valueOf(volumeSemester.getIw()));
                        setCellText(table.getRow(7).getCell(1),
                                String.valueOf(volumeSemester.getControl()));
                        setCellText(table.getRow(8).getCell(1),
                                String.valueOf(volumeSemester.getTotal()));
                        setCellText(table.getRow(9).getCell(1),
                                String.valueOf(volumeSemester.getZeCount()));
                    }
                    else{
                        for (var volumeSemester:volumesBySemester) {
                            int ind = volumeSemester.getSemester();
                            setCellText(table.getRow(1).getCell(ind),
                                    String.valueOf(volumeSemester.getSemester()));
                            setCellText(table.getRow(2).getCell(ind),
                                    String.valueOf(volumeSemester.getContactWork()));
                            setCellText(table.getRow(4).getCell(ind),
                                    String.valueOf(volumeSemester.getLectures()));
                            setCellText(table.getRow(5).getCell(ind),
                                    String.valueOf(volumeSemester.getPw()));
                            setCellText(table.getRow(6).getCell(ind),
                                    String.valueOf(volumeSemester.getLw()));
                            setCellText(table.getRow(7).getCell(ind),
                                    String.valueOf(volumeSemester.getIw()));
                            setCellText(table.getRow(18).getCell(ind),
                                    String.valueOf(volumeSemester.getControl()));
                            setCellText(table.getRow(19).getCell(ind),
                                    String.valueOf(volumeSemester.getTotal()));
                            setCellText(table.getRow(20).getCell(ind),
                                    String.valueOf(volumeSemester.getZeCount()));
                        }
                    }
                }
                else if(table.getRow(0).getCell(0).getText()
                        .trim().equals("№")){
                    for (var row: table.getRows()) {
                        if(row.getCell(1).getText().trim().equals("Итого часов")){
                            setCellTextSmall(row.getCell(2),
                                    String.valueOf(disciplineLectures));
                            setCellTextSmall(row.getCell(3),
                                    String.valueOf(disciplinePw));
                            setCellTextSmall(row.getCell(4),
                                    String.valueOf(disciplineLw));
                            setCellTextSmall(row.getCell(5),
                                    String.valueOf(disciplineIw));
                            setCellTextSmall(row.getCell(6),
                                    String.valueOf(disciplineTotal));
                        }
                    }
                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Дисциплина (модуль)")){
                    if(discipline.isPractice()){
                        table.getRow(0).getCell(1).setText(correctPracticeName);
                    }
                    else {
                        table.getRow(0).getCell(1).setText(disciplineName);
                    }
                    table.getRow(1).getCell(1).setText(syllabusLevel);
                    table.getRow(2).getCell(1).setText(syllabusQualification);
                    table.getRow(3).getCell(1).setText(syllabusSpecialty);
                    table.getRow(4).getCell(1).setText(syllabusProfile);
                    table.getRow(5).getCell(1).setText(String.join("; ", discipline.getCompetences()
                            .stream().map(Competence::getIndex).toList()));
                    table.getRow(8).getCell(1).setText(disciplineZeCount + " з.е., " +disciplineTotal+ " ч.");
                    table.getRow(9).getCell(1).setText(disciplineControlForms);
                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Код компетенции")) {
                    List<Competence> uCompetences = discipline.getUCompetences();
                    List<Competence> opCompetences = discipline.getOpCompetences();
                    List<Competence> pCompetences = discipline.getPCompetences();
                    if(!uCompetences.isEmpty()){
                        createCompetencesSubHeader(table, "Универсальные");
                        for (var comp : uCompetences) {
                            createCompetenceRow(table, comp);
                        }
                    }
                    if(!opCompetences.isEmpty()){
                        createCompetencesSubHeader(table, "Общепрофессиональные");
                        for (var comp : opCompetences) {
                            createCompetenceRow(table, comp);
                        }
                    }
                    if(!pCompetences.isEmpty()){
                        createCompetencesSubHeader(table, "Профессиональные");
                        for (var comp : pCompetences) {
                            createCompetenceRow(table, comp);
                        }
                    }
                }
            }

            File file = new File(documents.getDOCS_GEN_PATH() + "/Рабочие программы дисциплин/" + discipline.getIndex() + " "+ discipline.getName() + ".docx");
            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();
        }

        document.close();
    }

    private void replaceTextInParagraph(XWPFParagraph paragraph, String originalText, String updatedText) {
        String paragraphText = paragraph.getParagraphText();
        if (paragraphText.contains(originalText)) {
            String updatedParagraphText = paragraphText.replace(originalText, updatedText);
            while (!paragraph.getRuns().isEmpty()) {
                paragraph.removeRun(0);
            }
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(updatedParagraphText);
        }
    }

    private String getLevelByQualification(String qualification){
        if(qualification.equalsIgnoreCase("магистр")){
            return "магистратура";
        } else if (qualification.equalsIgnoreCase("бакалавр")) {
            return "бакалавриат";
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    private static void setCellText(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(11);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
    }
    private static void setCellTextSmall(XWPFTableCell cell, String text) {
        //TODO: хз че дел, не умещается
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(10);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
    }

    private void createCompetencesSubHeader(XWPFTable table, String header){
        XWPFTableRow row = table.createRow();

        XWPFParagraph paragraph = row.getCell(0).getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setText(header);

        XWPFTableCell cell = row.getCell(0);
        if (cell.getCTTc().getTcPr() == null) cell.getCTTc().addNewTcPr();
        if (cell.getCTTc().getTcPr().getGridSpan() == null) cell.getCTTc().getTcPr().addNewGridSpan();
        cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(4));
        row.removeCell(1);
        row.removeCell(1);
        row.removeCell(1);
    }

    private void createCompetenceRow(XWPFTable table, Competence comp){
        XWPFTableRow row1 = table.createRow();

        XWPFParagraph paragraphIndex = row1.getCell(0).getParagraphs().get(0);
        paragraphIndex.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runIndex = paragraphIndex.createRun();
        runIndex.setText(comp.getIndex());
        runIndex.setBold(true);

        XWPFParagraph paragraphName = row1.getCell(1).getParagraphs().get(0);
        paragraphName.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun runName = paragraphName.createRun();
        runName.setText(comp.getName());

        XWPFParagraph paragraphID1 = row1.getCell(2).getParagraphs().get(0);
        paragraphID1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID1 = paragraphID1.createRun();
        runID1.setText("ИД-1 ");

        XWPFRun runID2 = paragraphID1.createRun();
        runID2.setText(comp.getIndex());
        runID2.setSubscript(VerticalAlign.SUBSCRIPT);

        XWPFTableRow row2 = table.createRow();

        XWPFParagraph paragraphID2 = row2.getCell(2).getParagraphs().get(0);
        paragraphID2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID11 = paragraphID2.createRun();
        runID11.setText("ИД-2 ");

        XWPFRun runID12 = paragraphID2.createRun();
        runID12.setText(comp.getIndex());
        runID12.setSubscript(VerticalAlign.SUBSCRIPT);

        XWPFTableRow row3 = table.createRow();

        XWPFParagraph paragraphID3 = row3.getCell(2).getParagraphs().get(0);
        paragraphID3.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID31 = paragraphID3.createRun();
        runID31.setText("ИД-3 ");

        XWPFRun runID32 = paragraphID3.createRun();
        runID32.setText(comp.getIndex());
        runID32.setSubscript(VerticalAlign.SUBSCRIPT);

        row1.getCell(0).getCTTc().addNewTcPr();
        row1.getCell(1).getCTTc().addNewTcPr();
        row2.getCell(0).getCTTc().addNewTcPr();
        row2.getCell(1).getCTTc().addNewTcPr();
        row3.getCell(0).getCTTc().addNewTcPr();
        row3.getCell(1).getCTTc().addNewTcPr();

        CTVMerge vmerge = CTVMerge.Factory.newInstance();
        vmerge.setVal(STMerge.RESTART);
        row1.getCell(0).getCTTc().getTcPr().setVMerge(vmerge);
        row1.getCell(1).getCTTc().getTcPr().setVMerge(vmerge);

        CTVMerge vmerge1 = CTVMerge.Factory.newInstance();
        vmerge1.setVal(STMerge.CONTINUE);
        row2.getCell(0).getCTTc().getTcPr().setVMerge(vmerge1);
        row2.getCell(1).getCTTc().getTcPr().setVMerge(vmerge1);

        CTVMerge vmerge2 = CTVMerge.Factory.newInstance();
        vmerge2.setVal(STMerge.CONTINUE);
        row3.getCell(0).getCTTc().getTcPr().setVMerge(vmerge2);
        row3.getCell(1).getCTTc().getTcPr().setVMerge(vmerge2);
    }
}
