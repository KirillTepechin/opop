package opopproto.docGenerator;

import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.Discipline;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class FosGenerator extends AbstractGenerator{

    public void generate(SyllabusData syllabusData) throws IOException {
        FileUtils.forceMkdir(new File(documents.getDOCS_GEN_PATH()+"/Оценочные средства (ФОС)"));

        List<Discipline> disciplines = syllabusData.getDisciplinesData().getAllDisciplines();
        for (var discipline : disciplines) {
            document = new XWPFDocument();
            createSimpleParagraph("Приложение Д", ParagraphAlignment.RIGHT);
            createEmptyParagraph();
            createSimpleParagraph("Паспорт", ParagraphAlignment.CENTER).setFontSize(14);
            createSimpleParagraph("оценочных материалов для проведения текущего контроля и",
                    ParagraphAlignment.CENTER);
            if(discipline.isPractice()){
                createSimpleParagraph("промежуточной аттестации обучающихся по практике",
                        ParagraphAlignment.CENTER);
            }
            else if (discipline.isB3()) {
                createSimpleParagraph("промежуточной аттестации обучающихся по",
                        ParagraphAlignment.CENTER);
            } else{
                createSimpleParagraph("промежуточной аттестации обучающихся по дисциплине (модулю)",
                        ParagraphAlignment.CENTER);
            }

            createSimpleParagraph(discipline.getCorrectNameForTitle(),
                    ParagraphAlignment.CENTER).setUnderline(UnderlinePatterns.SINGLE);
            createEmptyParagraph();
            createSimpleParagraph("Перечень оценочных материалов и индикаторов достижения компетенций," +
                    " сформированность которых они контролируют", ParagraphAlignment.BOTH);
            createEmptyParagraph();
            var headers = new LinkedHashMap<String, Integer>();
            headers.put("Наименование оценочного средства", 4235);
            headers.put("Коды индикаторов достижения формируемых компетенции", 3449);
            headers.put("Номер приложения", 1553);
            createSimpleTable(2, 3, headers, 705);

            createEmptyParagraph();
            createEmptyParagraph();
            createEmptyParagraph();
            createEmptyParagraph();
            createComplexParagraph(ParagraphAlignment.BOTH, "Разработал: _________________________________ ", "");
            createEmptyParagraph();
            createComplexParagraph(ParagraphAlignment.BOTH, "Утверждено на заседании кафедры «",
                    "", "»");
            createComplexParagraph(ParagraphAlignment.BOTH, "протокол №", "  ", " от «", "  ","» __", "  ", "____"," 20__ ", "года");
            createComplexParagraph(ParagraphAlignment.BOTH, "Заведующий кафедрой ________________ ", "  ");

            File file = new File(documents.getDOCS_GEN_PATH() + "/Оценочные средства (ФОС)/"+ discipline.getIndex() + " "+ discipline.getName() + " ФОС.docx");
            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();
        }
        document.close();
    }

}
