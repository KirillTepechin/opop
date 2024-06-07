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
