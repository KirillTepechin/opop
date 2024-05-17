package opopproto.docGenerator;

import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.domain.LaborFunction;
import opopproto.domain.ProfessionalArea;
import opopproto.domain.Standard;
import opopproto.model.Head;
import opopproto.repository.HeadRepository;
import opopproto.util.LaborFunctionComparator;
import opopproto.util.StandardComparator;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class CharacteristicGenerator extends AbstractGenerator{
    @Autowired
    private HeadRepository headRepository;
    @Autowired
    private StandardComparator standardComparator;
    @Autowired
    private LaborFunctionComparator laborFunctionComparator;
    @Override
    void generate(SyllabusData syllabusData) throws IOException {
        FileInputStream fis = new FileInputStream(documents.getCHARACTERISTIC_TEMPLATE_PATH());
        document = new XWPFDocument(fis);
        document.enforceUpdateFields();

        String syllabusSpeciality = syllabusData.getSyllabusTitle().getSpecialty();
        String syllabusProfile = syllabusData.getSyllabusTitle().getProfile();
        String syllabusQualification = syllabusData.getSyllabusTitle().getQualification();
        String syllabusEduForms = syllabusData.getSyllabusTitle().getEducationForm();
        Head head = headRepository.findByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        String headFio = head.getName().charAt(0) + ". " + head.getPatronymic().charAt(0)+ ". " + head.getSurname();
        String syllabusFgos = syllabusData.getSyllabusTitle().getFgos();
        String syllabusProfAreas = String.join(", ", syllabusData.getSyllabusTitle().getProfessionalAreas()
                .stream().map(ProfessionalArea::getName).toList()).toLowerCase();
        String syllabusTaskTypes = String.join(", ", syllabusData.getSyllabusTitle().getTaskTypes());

        String syllabusLevelWhat = getLevelByQualification(syllabusQualification).equals("магистратура")?
                "магистратуры":"бакалавриата";
        String syllabusLevelWhatU = getLevelByQualification(syllabusQualification).equals("магистратура")?
                "МАГИСТРАТУРЫ":"БАКАЛАВРИАТА";
        String syllabusLevelWhom = getLevelByQualification(syllabusQualification).equals("магистратура")?
                "магистров":"бакалавров";
        String syllabusLevel = getLevelByQualification(syllabusQualification);
        String syllabusPeriod = syllabusData.getSyllabusTitle().getPeriod();


        String syllabusPracticesU = String.join(", ", syllabusData.getDisciplinesData().getBlock2DisciplineList().stream()
                .filter(Discipline::isEduPractice).map(Discipline::getName).toList()).toLowerCase();
        String syllabusPracticesP = String.join(", ", syllabusData.getDisciplinesData().getBlock2DisciplineList().stream()
                .filter(disciplineExtended -> !disciplineExtended.isEduPractice()).map(Discipline::getName).toList()).toLowerCase();
        for (var paragraph: document.getParagraphs()) {
            String text = paragraph.getText();
            if (text.contains("#syllabusQualification")) {
                replaceTextInParagraph(paragraph,"#syllabusQualification", syllabusQualification );
                if(text.trim().equals("#syllabusQualification")){
                    paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                    paragraph.getRuns().get(0).setItalic(true);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
            }
            if (text.contains("#syllabusEduForms")) {
                replaceTextInParagraph(paragraph,"#syllabusEduForms", syllabusEduForms );
                if(text.trim().equals("#syllabusEduForms")){
                    paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                    paragraph.getRuns().get(0).setItalic(true);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
            }
            if (text.contains("headFIO")) {
                replaceTextInParagraphNoReformat(paragraph,"headFIO", headFio );
            }
            if (text.contains("#syllabusFgos")) {
                replaceTextInParagraph(paragraph,"#syllabusFgos", syllabusFgos );
            }
            if (text.contains("#syllabusProfAreas")) {
                replaceTextInParagraph(paragraph,"#syllabusProfAreas", syllabusProfAreas.toLowerCase() );
            }
            if (text.contains("#syllabusTaskTypes")) {
                replaceTextInParagraph(paragraph,"#syllabusTaskTypes", syllabusTaskTypes );
            }
            if (text.contains("syllabusLevelWhatU")) {
                replaceTextInParagraphNoReformat(paragraph,"syllabusLevelWhatU", syllabusLevelWhatU );
            }
            if (text.contains("#syllabusLevelWhat")) {
                replaceTextInParagraph(paragraph,"#syllabusLevelWhat", syllabusLevelWhat );
            }
            if (text.contains("#syllabusLevelWhom")) {
                replaceTextInParagraph(paragraph,"#syllabusLevelWhom", syllabusLevelWhom );
            }
            if (text.contains("#syllabusLevel")) {
                replaceTextInParagraph(paragraph,"#syllabusLevel", syllabusLevel );
            }
            if (text.contains("#syllabusSpeciality")) {
                replaceTextInParagraph(paragraph,"#syllabusSpeciality", syllabusSpeciality );
                if(text.trim().equals("#syllabusSpeciality")){
                    paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                    paragraph.getRuns().get(0).setItalic(true);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
                else {
                    paragraph.getRuns().forEach(run-> run.setFontSize(12));
                }
            }
            if (text.contains("#syllabusProfile")) {
                replaceTextInParagraph(paragraph,"#syllabusProfile", syllabusProfile );
                if(text.trim().equals("#syllabusProfile")){
                    paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                    paragraph.getRuns().get(0).setItalic(true);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
                else {
                    paragraph.getRuns().forEach(run-> run.setFontSize(12));
                }
            }
            if (text.contains("#syllabusPeriod")) {
                replaceTextInParagraph(paragraph,"#syllabusPeriod", syllabusPeriod );
            }
            if (text.contains("#syllabusPracticesU")) {
                replaceTextInParagraph(paragraph,"#syllabusPracticesU", syllabusPracticesU );
            }
            if (text.contains("#syllabusPracticesP")) {
                replaceTextInParagraph(paragraph,"#syllabusPracticesP", syllabusPracticesP );
            }
        }
        final String U_COMPETENCE_HEADER = "Категория универсальных компетенций";
        final String OP_COMPETENCE_HEADER = "Код и наименование общепрофессиональной компетенции";
        final String P_COMPETENCE_HEADER = "Код и наименование профессиональной компетенции";
        final String P_COMPETENCE_TASK_TYPES_HEADER = "Задача ПД";
        int ind = 1;

        List<Standard> standards = syllabusData.getSyllabusTitle().getProfessionalAreas().stream()
                .flatMap(professionalArea -> professionalArea.getStandards().stream()).toList();
        for (var table: document.getTables()) {
            //Если нашли таблицу сокращений
            if (table.getRow(0).getCell(1).getText().contains("зачетная единица")){
                for (var row: table.getRows()) {
                    if(row.getCell(0).getText().equals("ПООП")){
                        row.getCell(1).appendText(" "+syllabusSpeciality);
                    }
                    if(row.getCell(0).getText().equals("ФГОС ВО")){
                        row.getCell(1).appendText(" "+syllabusSpeciality);
                    }
                }
            }
            //Если нашли таблицу с обл. проф. деят.
            if (table.getRow(0).getCell(0)
                    .getText().contains("Область профессиональной деятельности")){
                table.getRow(0).getCell(1).setText(syllabusProfAreas);
                table.getRow(1).getCell(1).setText(syllabusTaskTypes);
            }
            //Если нашли таблицу УК
            else if (table.getRow(0).getCell(0).getText().equals(U_COMPETENCE_HEADER) ){
                List<Competence> uComps = syllabusData.getUCompetences();
                for (var comp:uComps) {
                    createUCompetenceRow(table, comp);
                }
            }
            //Если нашли таблицу ОПК
            else if(table.getRow(0).getCell(0).getText().contains(OP_COMPETENCE_HEADER)){
                List<Competence> opComps = syllabusData.getOpCompetences();
                for (var comp:opComps) {
                    createCompetenceRow(table, comp);
                }
            }
            //Если нашли таблицу ПК
            else if (table.getRow(0).getCell(0).getText().contains(P_COMPETENCE_HEADER)) {
                List<Competence> pComps = syllabusData.getPCompetences();
                for (var comp:pComps) {
                    createCompetenceRow(table, comp);
                }
            }
            //Если нашли таблицу соответствия компетенций и типов задач
            else if (table.getRow(0).getCell(0).getText().contains(P_COMPETENCE_TASK_TYPES_HEADER)) {
                List<Competence> pComps = syllabusData.getPCompetences();
                int taskTypeInd = 0;
                for (var taskType: syllabusData.getSyllabusTitle().getTaskTypes()) {
                    createCompetencesTaskTypeSubHeader(table, taskType);
                    for (var comp:pComps) {
                        createCompetenceTaskTypeRow(table, comp);
                    }
                    setStandardsAndMergeCells(table, standards, taskTypeInd, pComps.size());

                    taskTypeInd++;
                }
            }
            // Парсинг похожих таблиц с дисциплинами
            else if(table.getRow(0).getCell(0).getText().equals("Индекс")
                    && table.getRow(0).getCell(1).getText().equals("Наименование дисциплины")){
                if(ind==1){
                    List<Competence> comps = syllabusData.getCompetences();
                    for (var comp: comps) {
                        createMatrixRows(table, comp);
                    }
                    ind++;
                }
                else if(ind==2){
                    List<Discipline> disciplines = syllabusData.getDisciplinesData().getBaseDisciplines();
                    for (var disc: disciplines) {
                        createDisciplineRow(table, disc);
                    }
                    ind++;
                }
                else if(ind==3){
                    List<Discipline> disciplines = syllabusData.getDisciplinesData().getVaryDisciplines();
                    for (var disc: disciplines) {
                        createDisciplineRow(table, disc);
                    }
                    ind++;
                }
                else{
                    List<Discipline> disciplines = syllabusData.getDisciplinesData().getBlock4DisciplineList();
                    for (var disc: disciplines) {
                        createDisciplineRow(table, disc);
                    }
                }
            }
            //Приложение А
            else if (table.getRow(0).getCell(0).getText().contains("№ п/п")) {
                createAppendixA(table, standards);
            }
            //Приложение Б
            else if (table.getRow(0).getCell(0).getText()
                    .contains("Код и наименование профессионального стандарта")) {
                List<Standard> standardsBSyllabusByCompetences = syllabusData.getCompetences().stream()
                        .map(Competence::getStandards).flatMap(List::stream).toList();

                List<Standard> standardsBSyllabus = standards.stream().peek(standard -> standardsBSyllabusByCompetences.forEach(standardByComp -> {
                    if (standard.getCode().equals(standardByComp.getCode())) {
                        if(standard.getLaborFunction()==null){
                            standard.setLaborFunction(standardByComp.getLaborFunction());
                        }
                        standard.getLaborFunction().getLaborFunctions().addAll(standardByComp.getLaborFunction().getLaborFunctions());
                    }
                })).toList();
                createAppendixB(table, standardsBSyllabus, syllabusQualification);
            }

        }
        document.enforceUpdateFields();
        File file = new File(documents.getDOCS_GEN_PATH() + "/Характеристика ОПОП "+ syllabusData.getSyllabusTitle().getCode() + ".docx");
        FileOutputStream out = new FileOutputStream(file);
        document.write(out);
        out.close();

        document.close();
    }
    private XWPFTableRow copyRow(XWPFTable table, XWPFTableRow original ){

        CTRow ctrow;
        try {
            ctrow = CTRow.Factory.parse(original.getCtRow().newInputStream());
        } catch (XmlException | IOException e) {
            throw new RuntimeException(e);
        }
        return new XWPFTableRow(ctrow, table);
    }
    static void commitTableRows(XWPFTable table) {
        int rowNr = 0;
        for (XWPFTableRow tableRow : table.getRows()) {
            table.getCTTbl().setTrArray(rowNr++, tableRow.getCtRow());
        }
    }
    static void mergeCellVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for(int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            CTVMerge vmerge = CTVMerge.Factory.newInstance();
            if(rowIndex == fromRow){
                // The first merged cell is set with RESTART merge value
                vmerge.setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                vmerge.setVal(STMerge.CONTINUE);
                // and the content should be removed
                for (int i = cell.getParagraphs().size(); i > 0; i--) {
                    cell.removeParagraph(0);
                }
                cell.addParagraph();
            }
            // Try getting the TcPr. Not simply setting an new one every time.
            CTTcPr tcPr = cell.getCTTc().getTcPr();
            if (tcPr == null) tcPr = cell.getCTTc().addNewTcPr();
            tcPr.setVMerge(vmerge);
        }
    }
    private void createAppendixB(XWPFTable table, List<Standard> standards, String syllabusQualification) {
        standards = new ArrayList<>(standards);

        standards.sort(standardComparator);

        var firstStandard = standards.get(0);
        XWPFParagraph paragraphInd = table.getRow(1).getCell(0).getParagraphs().get(0);
        paragraphInd.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runFirstStandard = paragraphInd.createRun();
        runFirstStandard.setText(firstStandard.getCode() + " "+firstStandard.getName().charAt(0) +
                firstStandard.getName().substring(1).toLowerCase());

        XWPFTableRow rowStandard = copyRow(table, table.getRow(1));

        rowStandard.getCell(0).setText(null);
        rowStandard.getCell(1).setText(firstStandard.getLaborFunction().getCode());
        rowStandard.getCell(2).setText(firstStandard.getLaborFunction().getName());
        rowStandard.getCell(3).setText(syllabusQualification);
        rowStandard.getCell(4).setText("");

        XWPFTableRow newRow = copyRow(table, table.getRow(1));
        var subLaborFunctions = firstStandard.getLaborFunction()
                .getLaborFunctions().stream().toList().stream().sorted(laborFunctionComparator).toList();

        for (int i = 0; i < subLaborFunctions.size(); i++) {
            var subLaborFunc = subLaborFunctions.get(i);
            if(i==0){
                rowStandard.getCell(4).setText(subLaborFunc.getName());
                rowStandard.getCell(5).setText(subLaborFunc.getCode());
                rowStandard.getCell(6).setText(syllabusQualification);
                table.addRow(rowStandard);

            }
            else{
                newRow.getCell(0).setText(null);
                newRow.getCell(1).setText(null);
                newRow.getCell(2).setText(null);
                newRow.getCell(3).setText(null);
                newRow.getCell(4).setText(subLaborFunc.getName());
                newRow.getCell(5).setText(subLaborFunc.getCode());
                newRow.getCell(6).setText(syllabusQualification);
                table.addRow(newRow);
            }
        }

        mergeCellVertically(table, 0, 1, subLaborFunctions.size() + 1);
        mergeCellVertically(table, 1, 2, subLaborFunctions.size() + 1);
        mergeCellVertically(table, 2, 2, subLaborFunctions.size() + 1);
        mergeCellVertically(table, 3, 2, subLaborFunctions.size() + 1);
        commitTableRows(table);

        standards.remove(0);
        int startRow = subLaborFunctions.size() + 2;
        int endRow = 0;
        for (var standard: standards) {
            rowStandard = copyRow(table, table.getRow(1));

            rowStandard.getCell(0).setText(standard.getCode() + " "+standard.getName().charAt(0) +
                    standard.getName().substring(1).toLowerCase());
            rowStandard.getCell(1).setText(standard.getLaborFunction().getCode());
            rowStandard.getCell(2).setText(standard.getLaborFunction().getName());
            rowStandard.getCell(3).setText(syllabusQualification);
            rowStandard.getCell(4).setText("");

            newRow = copyRow(table, table.getRow(1));
            subLaborFunctions = standard.getLaborFunction()
                    .getLaborFunctions().stream().toList().stream().sorted(laborFunctionComparator).toList();
            endRow = startRow+subLaborFunctions.size()-1;
            for (int i = 0; i < subLaborFunctions.size(); i++) {
                var subLaborFunc = subLaborFunctions.get(i);
                if(i==0){
                    rowStandard.getCell(4).setText(subLaborFunc.getName());
                    rowStandard.getCell(5).setText(subLaborFunc.getCode());
                    rowStandard.getCell(6).setText(syllabusQualification);
                    table.addRow(rowStandard);

                }
                else{
                    newRow.getCell(0).setText(null);
                    newRow.getCell(1).setText(null);
                    newRow.getCell(2).setText(null);
                    newRow.getCell(3).setText(null);
                    newRow.getCell(4).setText(subLaborFunc.getName());
                    newRow.getCell(5).setText(subLaborFunc.getCode());
                    newRow.getCell(6).setText(syllabusQualification);
                    table.addRow(newRow);
                }
            }
            mergeCellVertically(table, 0, startRow, endRow);
            mergeCellVertically(table, 1, startRow, endRow);
            mergeCellVertically(table, 2, startRow, endRow);
            mergeCellVertically(table, 3, startRow, endRow);
            commitTableRows(table);
        }
    }

    private void createAppendixA(XWPFTable table, List<Standard> standards) {

        for (int i = 1; i < standards.size() + 1; i++) {
            var standard = standards.get(i-1);
            XWPFTableRow rowStandard = table.createRow();

            XWPFParagraph paragraphInd = rowStandard.getCell(0).getParagraphs().get(0);
            paragraphInd.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runInd = paragraphInd.createRun();
            runInd.setText(String.valueOf(i));

            XWPFParagraph paragraphCode = rowStandard.getCell(1).getParagraphs().get(0);
            paragraphCode.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun runCode = paragraphCode.createRun();
            runCode.setText(standard.getCode());

            XWPFParagraph paragraphName = rowStandard.getCell(2).getParagraphs().get(0);
            paragraphName.setAlignment(ParagraphAlignment.BOTH);
            XWPFRun runName = paragraphName.createRun();
            runName.setText(standard.getName().charAt(0) + standard.getName().substring(1).toLowerCase());
        }

        CTTblPr tblpro = table.getCTTbl().getTblPr();

        CTTblBorders borders = tblpro.addNewTblBorders();
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
    }

    private void createDisciplineRow(XWPFTable table, Discipline discipline){
        XWPFTableRow rowDisc = table.createRow();

        XWPFParagraph paragraphDiscIndex = rowDisc.getCell(0).getParagraphs().get(0);
        paragraphDiscIndex.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun runDiscIndex = paragraphDiscIndex.createRun();
        runDiscIndex.setText(discipline.getIndex());

        XWPFParagraph paragraphDiscName = rowDisc.getCell(1).getParagraphs().get(0);
        paragraphDiscName.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun runDiscName = paragraphDiscName.createRun();
        runDiscName.setText(discipline.getName());

        CTTblPr tblpro = table.getCTTbl().getTblPr();

        CTTblBorders borders = tblpro.addNewTblBorders();
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewTop().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
    }
    private void createMatrixRows(XWPFTable table, Competence comp) {
        XWPFTableRow rowComp = table.createRow();

        XWPFParagraph paragraphCompIndex = rowComp.getCell(0).getParagraphs().get(0);
        paragraphCompIndex.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun runCompIndex = paragraphCompIndex.createRun();
        runCompIndex.setText(comp.getIndex());

        XWPFParagraph paragraphCompName = rowComp.getCell(1).getParagraphs().get(0);
        paragraphCompName.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runCompName = paragraphCompName.createRun();
        runCompName.setText(comp.getName());

        for (var disc: comp.getDisciplines()) {
            XWPFTableRow rowDisc = table.createRow();

            XWPFParagraph paragraphDiscIndex = rowDisc.getCell(0).getParagraphs().get(0);
            paragraphDiscIndex.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun runDiscIndex = paragraphDiscIndex.createRun();
            runDiscIndex.setText(disc.getIndex());

            XWPFParagraph paragraphDiscName = rowDisc.getCell(1).getParagraphs().get(0);
            paragraphDiscName.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun runDiscName = paragraphDiscName.createRun();
            runDiscName.setText(disc.getName());
        }
    }

    private void createUCompetenceRow(XWPFTable table, Competence comp){
        XWPFTableRow row1 = table.createRow();

        XWPFParagraph paragraphComp = row1.getCell(1).getParagraphs().get(0);
        paragraphComp.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runComp = paragraphComp.createRun();
        runComp.setText(comp.getIndex()+". " + comp.getName());


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

        paragraphComp.setAlignment(ParagraphAlignment.LEFT);
        paragraphID1.setAlignment(ParagraphAlignment.LEFT);
        paragraphID2.setAlignment(ParagraphAlignment.LEFT);
        paragraphID3.setAlignment(ParagraphAlignment.LEFT);

    }

    private void createCompetenceRow(XWPFTable table, Competence comp){
        XWPFTableRow row1 = table.createRow();

        XWPFParagraph paragraphComp = row1.getCell(0).getParagraphs().get(0);
        paragraphComp.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runComp = paragraphComp.createRun();
        runComp.setText(comp.getIndex()+". " + comp.getName());

        XWPFParagraph paragraphID1 = row1.getCell(1).getParagraphs().get(0);
        paragraphID1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID1 = paragraphID1.createRun();
        runID1.setText("ИД-1 ");

        XWPFRun runID2 = paragraphID1.createRun();
        runID2.setText(comp.getIndex());
        runID2.setSubscript(VerticalAlign.SUBSCRIPT);

        XWPFTableRow row2 = table.createRow();

        XWPFParagraph paragraphID2 = row2.getCell(1).getParagraphs().get(0);
        paragraphID2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID11 = paragraphID2.createRun();
        runID11.setText("ИД-2 ");

        XWPFRun runID12 = paragraphID2.createRun();
        runID12.setText(comp.getIndex());
        runID12.setSubscript(VerticalAlign.SUBSCRIPT);

        XWPFTableRow row3 = table.createRow();

        XWPFParagraph paragraphID3 = row3.getCell(1).getParagraphs().get(0);
        paragraphID3.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID31 = paragraphID3.createRun();
        runID31.setText("ИД-3 ");

        XWPFRun runID32 = paragraphID3.createRun();
        runID32.setText(comp.getIndex());
        runID32.setSubscript(VerticalAlign.SUBSCRIPT);

        row1.getCell(0).getCTTc().addNewTcPr();
        row2.getCell(0).getCTTc().addNewTcPr();
        row3.getCell(0).getCTTc().addNewTcPr();

        CTVMerge vmerge = CTVMerge.Factory.newInstance();
        vmerge.setVal(STMerge.RESTART);
        row1.getCell(0).getCTTc().getTcPr().setVMerge(vmerge);

        CTVMerge vmerge1 = CTVMerge.Factory.newInstance();
        vmerge1.setVal(STMerge.CONTINUE);
        row2.getCell(0).getCTTc().getTcPr().setVMerge(vmerge1);

        CTVMerge vmerge2 = CTVMerge.Factory.newInstance();
        vmerge2.setVal(STMerge.CONTINUE);
        row3.getCell(0).getCTTc().getTcPr().setVMerge(vmerge2);

        paragraphComp.setAlignment(ParagraphAlignment.LEFT);
        paragraphID1.setAlignment(ParagraphAlignment.LEFT);
        paragraphID2.setAlignment(ParagraphAlignment.LEFT);
        paragraphID3.setAlignment(ParagraphAlignment.LEFT);
    }

    private void createCompetenceTaskTypeRow(XWPFTable table, Competence comp){
        XWPFTableRow row1 = table.createRow();

        XWPFParagraph paragraphComp = row1.getCell(2).getParagraphs().get(0);
        paragraphComp.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runComp = paragraphComp.createRun();
        runComp.setText(comp.getIndex()+". " + comp.getName());

        XWPFParagraph paragraphID1 = row1.getCell(3).getParagraphs().get(0);
        paragraphID1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID1 = paragraphID1.createRun();
        runID1.setText("ИД-1 ");

        XWPFRun runID2 = paragraphID1.createRun();
        runID2.setText(comp.getIndex());
        runID2.setSubscript(VerticalAlign.SUBSCRIPT);

        XWPFTableRow row2 = table.createRow();

        XWPFParagraph paragraphID2 = row2.getCell(3).getParagraphs().get(0);
        paragraphID2.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID11 = paragraphID2.createRun();
        runID11.setText("ИД-2 ");

        XWPFRun runID12 = paragraphID2.createRun();
        runID12.setText(comp.getIndex());
        runID12.setSubscript(VerticalAlign.SUBSCRIPT);

        XWPFTableRow row3 = table.createRow();

        XWPFParagraph paragraphID3 = row3.getCell(3).getParagraphs().get(0);
        paragraphID3.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun runID31 = paragraphID3.createRun();
        runID31.setText("ИД-3 ");

        XWPFRun runID32 = paragraphID3.createRun();
        runID32.setText(comp.getIndex());
        runID32.setSubscript(VerticalAlign.SUBSCRIPT);

        row1.getCell(2).getCTTc().addNewTcPr();
        row2.getCell(2).getCTTc().addNewTcPr();
        row3.getCell(2).getCTTc().addNewTcPr();

        CTVMerge vmerge = CTVMerge.Factory.newInstance();
        vmerge.setVal(STMerge.RESTART);
        row1.getCell(2).getCTTc().getTcPr().setVMerge(vmerge);

        CTVMerge vmerge1 = CTVMerge.Factory.newInstance();
        vmerge1.setVal(STMerge.CONTINUE);
        row2.getCell(2).getCTTc().getTcPr().setVMerge(vmerge1);

        CTVMerge vmerge2 = CTVMerge.Factory.newInstance();
        vmerge2.setVal(STMerge.CONTINUE);
        row3.getCell(2).getCTTc().getTcPr().setVMerge(vmerge2);

        paragraphComp.setAlignment(ParagraphAlignment.LEFT);
        paragraphID1.setAlignment(ParagraphAlignment.LEFT);
        paragraphID2.setAlignment(ParagraphAlignment.LEFT);
        paragraphID3.setAlignment(ParagraphAlignment.LEFT);
    }
    private void setStandardsAndMergeCells(XWPFTable table, List<Standard> standards,
                                           int taskTypeInd, int compSize){
        int standRowInd = 2 + (compSize*taskTypeInd) + (taskTypeInd+1);
        XWPFTableCell standardsCell = table.getRow(standRowInd).getCell(4);

        XWPFParagraph paragraphStandards = standardsCell.getParagraphs().get(0);
        paragraphStandards.setAlignment(ParagraphAlignment.LEFT);
        for (var standard: standards) {
            XWPFRun run = paragraphStandards.createRun();
            run.setText(standard.getCode()+" "+standard.getName().charAt(0) + standard.getName().substring(1).toLowerCase());
            run.addBreak();
            run.addBreak();
        }

        for (int i = 0; i < compSize * 3; i++) {
            var row = table.getRow(standRowInd + i);
            row.getCell(0).getCTTc().addNewTcPr();
            row.getCell(1).getCTTc().addNewTcPr();
            row.getCell(4).getCTTc().addNewTcPr();

            CTVMerge vmerge = CTVMerge.Factory.newInstance();
            if(i==0){
                vmerge.setVal(STMerge.RESTART);
            }
            else{
                vmerge.setVal(STMerge.CONTINUE);
            }
            row.getCell(0).getCTTc().getTcPr().setVMerge(vmerge);
            row.getCell(1).getCTTc().getTcPr().setVMerge(vmerge);
            row.getCell(4).getCTTc().getTcPr().setVMerge(vmerge);
        }
    }
    private void createCompetencesTaskTypeSubHeader(XWPFTable table, String header){
        XWPFTableRow row = table.createRow();

        XWPFParagraph paragraph = row.getCell(0).getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setItalic(true);
        run.setText(String.valueOf(header.charAt(0)).toUpperCase()+header.substring(1));

        XWPFTableCell cell = row.getCell(0);
        if (cell.getCTTc().getTcPr() == null) cell.getCTTc().addNewTcPr();
        if (cell.getCTTc().getTcPr().getGridSpan() == null) cell.getCTTc().getTcPr().addNewGridSpan();
        cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(5));
        row.removeCell(1);
        row.removeCell(1);
        row.removeCell(1);
        row.removeCell(1);
    }
}
