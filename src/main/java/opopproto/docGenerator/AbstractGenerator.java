package opopproto.docGenerator;

import opopproto.data.syllabus.SyllabusData;
import opopproto.util.Documents;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public abstract class AbstractGenerator {
    @Autowired
    protected Documents documents;
    XWPFDocument document = new XWPFDocument();
    private XWPFParagraph createParagraph(ParagraphAlignment alignment){
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(alignment);
        paragraph.setFirstLineIndent(708);
        paragraph.setSpacingBetween(1.15);
        paragraph.setSpacingAfter(0);
        return paragraph;
    }
    XWPFRun createSimpleParagraph(String text, ParagraphAlignment alignment){

        XWPFRun run = createParagraph(alignment).createRun();
        run.setText(text);
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");

        return run;
    }

    void createComplexParagraph(ParagraphAlignment alignment, String... text){
        XWPFParagraph paragraph = createParagraph(alignment);
        for (var txt: text) {
            XWPFRun run = paragraph.createRun();
            run.setText(txt);
            run.setFontSize(12);
            run.setFontFamily("Times New Roman");
        }
    }

    void createEmptyParagraph(){
        XWPFRun run = createParagraph(ParagraphAlignment.LEFT).createRun();
    }

    XWPFTable createSimpleTable(int rows, int column, LinkedHashMap<String, Integer> headers, int height){
        //
        XWPFTable table = document.createTable(rows, column);
        int i = 0;
        for (var key : headers.keySet()) {
            setCellText(table.getRow(0).getCell(i), key);
            i++;
        }
        setTableWidth(table, headers, height);

        return table;
    }

    private static void setCellText(XWPFTableCell cell, String text) {
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingBetween(1.15);
        paragraph.setSpacingAfter(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
    }

    private static void setTableWidth(XWPFTable table, Map<String, Integer> headers, int height) {
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        if (tblPr == null) tblPr = table.getCTTbl().addNewTblPr();

        CTTblWidth tblW = CTTblWidth.Factory.newInstance();
        tblW.setType(STTblWidth.DXA);
        tblW.setW(BigInteger.valueOf(headers.values().stream().mapToInt(Integer::valueOf).sum()));
        tblPr.setTblW(tblW);

        for (XWPFTableRow row : table.getRows()) {
            row.setHeight(height);//705
            int i = 0;
            for (var entrySet:headers.entrySet()) {
                row.getCell(i).setWidth(String.valueOf(entrySet.getValue())); //4235 3449 1553
                i++;
            }
        }
    }
    String getLevelByQualification(String qualification){
        if(qualification.equalsIgnoreCase("магистр")){
            return "магистратура";
        } else if (qualification.equalsIgnoreCase("бакалавр")) {
            return "бакалавриат";
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    void replaceTextInParagraph(XWPFParagraph paragraph, String originalText, String updatedText) {
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
    void replaceTextInParagraphNoReformat(XWPFParagraph paragraph, String originalText, String updatedText) {
        List<XWPFRun> runs = paragraph.getRuns();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null && text.contains(originalText)) {
                String updatedRunText = text.replace(originalText, updatedText);
                run.setText(updatedRunText, 0);
            }
        }
    }
    abstract void generate(SyllabusData syllabusData) throws IOException;
}
