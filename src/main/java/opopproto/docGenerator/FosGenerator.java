package opopproto.docGenerator;

import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.Discipline;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFNum;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class FosGenerator extends AbstractGenerator{

    @Override
    void generate(SyllabusData syllabusData) throws IOException {
        FileUtils.forceMkdir(new File(documents.getDOCS_GEN_PATH() + "/Оценочные средства (ФОС)"));

        List<Discipline> disciplines = syllabusData.getDisciplinesData().getAllDisciplines();
        List<String> appendixLetters = List.of("Е", "Ж", "З", "И");
        for (var discipline : disciplines) {
            FileInputStream fis;
            if(discipline.isPractice()){
                fis = new FileInputStream(documents.getFOS_PRACTICE_TEMPLATE_PATH());
            }
            else if (discipline.isB3()){
                fis = new FileInputStream(documents.getFOS_B3_TEMPLATE_PATH());
            }
            else{
                fis = new FileInputStream(documents.getFOS_TEMPLATE_PATH());
            }
            document = new XWPFDocument(fis);

            String disciplineName = discipline.getName();
            String correctPracticeName = discipline.getCorrectNameForTitle();
            for (var paragraph: document.getParagraphs()) {
                String text = paragraph.getText();
                if (text.contains("#disciplineName")) {
                    replaceTextInParagraph(paragraph,"#disciplineName", disciplineName );
                    if(text.trim().equals("#disciplineName")){
                        paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                        paragraph.getRuns().get(0).setFontSize(12);
                        paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                    }
                }
                else if (text.contains("#correctPracticeName")) {
                    replaceTextInParagraph(paragraph,"#correctPracticeName", correctPracticeName );
                    if(text.trim().equals("#correctPracticeName")){
                        paragraph.getRuns().get(0).setUnderline(UnderlinePatterns.SINGLE);
                        paragraph.getRuns().get(0).setFontSize(12);
                        paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                    }
                }
            }

            XWPFTable table = document.getTables().get(0);
            //Добавляем в шаблон новые строки
            if(!discipline.isB3() && !discipline.isPractice()){
                int pw = discipline.getVolumeData().getVolumesBySemester().get(0).getPw();
                int lw = discipline.getVolumeData().getVolumesBySemester().get(0).getLw();
                List<String> controlForms = discipline.getVolumeData().getControlForm();

                if(pw!=0){
                    XWPFTableRow row = table.createRow();

                    XWPFParagraph paragraph = row.getCell(0).getParagraphs().get(0);
                    row.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFRun runIndex = paragraph.createRun();
                    runIndex.setText("Практические работы");
                    paragraph.getRuns().get(0).setFontSize(12);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
                else if (lw != 0) {
                    XWPFTableRow row = table.createRow();

                    XWPFParagraph paragraph = row.getCell(0).getParagraphs().get(0);
                    row.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFRun runIndex = paragraph.createRun();
                    runIndex.setText("Лабораторные работы");
                    paragraph.getRuns().get(0).setFontSize(12);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
                for (var controlForm: controlForms) {
                    XWPFTableRow row = table.createRow();

                    XWPFParagraph paragraph = row.getCell(0).getParagraphs().get(0);
                    row.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFRun runIndex = paragraph.createRun();
                    runIndex.setText(controlForm.substring(0, 1).toUpperCase() + controlForm.substring(1));
                    paragraph.getRuns().get(0).setFontSize(12);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");
                }
            }
            //Заполняем
            int i = 0; //индекс номера приложения
            for (var row: table.getRows()) {
                if(row.getCell(0).getText().equals("Наименование оценочного средства")){
                    continue;
                }
                XWPFParagraph paragraphAppendix = row.getCell(2).getParagraphs().get(0);
                row.getCell(2).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                paragraphAppendix.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun runAppendix = paragraphAppendix.createRun();
                runAppendix.setText(appendixLetters.get(i));
                paragraphAppendix.getRuns().get(0).setFontSize(12);
                paragraphAppendix.getRuns().get(0).setFontFamily("Times New Roman");
                i++;
                row.getCell(1).removeParagraph(0);
                for (var comp : discipline.getCompetences()) {
                    XWPFParagraph paragraph = row.getCell(1).addParagraph();
                    paragraph.setSpacingAfter(0);
                    row.getCell(1).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    paragraph.setAlignment(ParagraphAlignment.CENTER);

                    XWPFRun runID1 = paragraph.createRun();
                    runID1.setText("ИД-1 ");

                    XWPFRun runIndex = paragraph.createRun();
                    runIndex.setText(comp.getIndex());
                    runIndex.setSubscript(VerticalAlign.SUBSCRIPT);

                    XWPFRun runID2 = paragraph.createRun();
                    runID2.setText(" ИД-2 ");

                    runIndex = paragraph.createRun();
                    runIndex.setText(comp.getIndex());
                    runIndex.setSubscript(VerticalAlign.SUBSCRIPT);

                    XWPFRun runID3 = paragraph.createRun();
                    runID3.setText(" ИД-3 ");

                    runIndex = paragraph.createRun();
                    runIndex.setText(comp.getIndex());
                    runIndex.setSubscript(VerticalAlign.SUBSCRIPT);

                    paragraph.getRuns().get(0).setFontSize(12);
                    paragraph.getRuns().get(0).setFontFamily("Times New Roman");

                    paragraph.getRuns().get(1).setFontSize(12);
                    paragraph.getRuns().get(1).setFontFamily("Times New Roman");

                    paragraph.getRuns().get(2).setFontSize(12);
                    paragraph.getRuns().get(2).setFontFamily("Times New Roman");

                    paragraph.getRuns().get(3).setFontSize(12);
                    paragraph.getRuns().get(3).setFontFamily("Times New Roman");

                    paragraph.getRuns().get(4).setFontSize(12);
                    paragraph.getRuns().get(4).setFontFamily("Times New Roman");

                    paragraph.getRuns().get(5).setFontSize(12);
                    paragraph.getRuns().get(5).setFontFamily("Times New Roman");

                }
            }

            XWPFAbstractNum abstractNum = document.getNumbering().getAbstractNums().stream()
                    .filter(an->an.getCTAbstractNum().getLvlArray()[0].getNumFmt().getVal()
                            .equals(STNumberFormat.UPPER_ROMAN)).toList().get(0);
            BigInteger abstractNumID = document.getNumbering().addAbstractNum(abstractNum);
            BigInteger numID = document.getNumbering().addNum(abstractNumID);


            //Создаем приложения
            List<String> evaluates = new ArrayList<>();
            if(!discipline.isB3() && !discipline.isPractice()){
                evaluates.add("Тест");
                int pw = discipline.getVolumeData().getVolumesBySemester().get(0).getPw();
                int lw = discipline.getVolumeData().getVolumesBySemester().get(0).getLw();
                List<String> controlForms = discipline.getVolumeData().getControlForm();
                if(pw!=0){
                    evaluates.add("Практические работы");
                }
                else if (lw != 0) {
                    evaluates.add("Лабораторные работы");
                }
                evaluates.addAll(controlForms.stream()
                        .map(cp->cp.substring(0, 1).toUpperCase() + cp.substring(1)).toList());
            }
            else if(discipline.isPractice()){
                evaluates.add("Письменный отчёт");
                evaluates.add("Практическое задание");
                evaluates.add("Зачёт с оценкой");
            }
            else{
                evaluates.add("Выпускная квалификационная работа");
                evaluates.add("Доклад по выпускной квалификационной работе и собеседование по результатам доклада");
            }
            for (int j = 0; j < i; j++) {
                // Создаем новую страницу

                XWPFParagraph pageBreak = document.createParagraph();
                pageBreak.createRun().addBreak(BreakType.PAGE);
                String eval = evaluates.get(j);
                if (eval.equals("Тест") || eval.equals("Практические работы") ||
                        eval.equals("Лабораторные работы") || eval.equals("Выпускная квалификационная работа") ||
                        eval.equals("Доклад по выпускной квалификационной работе и собеседование по результатам доклада") ||
                        eval.equals("Письменный отчёт") || eval.equals("Практическое задание") || eval.equals("Курсовая работа") ||
                        eval.equals("Курсовой проект")) {
                    if(j==0){
                        XWPFParagraph paragraph = document.createParagraph();
                        paragraph.setNumID(numID);
                        paragraph.setAlignment(ParagraphAlignment.LEFT);
                        XWPFRun run = paragraph.createRun();
                        run.setText("Текущий контроль");
                        run.setFontFamily("Times New Roman");
                        run.setFontSize(12);
                    }
                }
                else{
                    if(document.getNumbering().getNums().size()-document.getNumbering().getAbstractNums().size()<2){
                        XWPFParagraph paragraph = document.createParagraph();
                        paragraph.setNumID(numID);
                        paragraph.setAlignment(ParagraphAlignment.LEFT);
                        XWPFRun run = paragraph.createRun();
                        run.setText("Промежуточная аттестация");
                        run.setFontFamily("Times New Roman");
                        run.setFontSize(12);
                    }
                }


                // Приложение
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setAlignment(ParagraphAlignment.RIGHT);
                paragraph.setVerticalAlignment(TextAlignment.TOP);
                XWPFRun run = paragraph.createRun();
                run.setText("Приложение "+ appendixLetters.get(j));
                run.setFontFamily("Times New Roman");
                run.setFontSize(12);


                //Оцен средство
                XWPFParagraph paragraphEval = document.createParagraph();
                paragraphEval.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun runEval = paragraphEval.createRun();
                runEval.setText(eval);
                runEval.setFontFamily("Times New Roman");
                runEval.setFontSize(12);
            }

            File file = new File(documents.getDOCS_GEN_PATH() + "/Оценочные средства (ФОС)/"+ discipline.getIndex() + " "+ discipline.getName() + " ФОС.docx");
            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();
        }
        document.close();
    }
}
