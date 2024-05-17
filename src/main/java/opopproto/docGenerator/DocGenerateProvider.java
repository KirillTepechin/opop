package opopproto.docGenerator;

import opopproto.data.syllabus.SyllabusData;
import opopproto.parser.ExcelParser;
import opopproto.util.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Component
public class DocGenerateProvider {
    @Autowired
    private ExcelParser excelParser;
    @Autowired
    private Documents documents;
    @Autowired
    private FosGenerator fosGenerator;
    @Autowired
    private RpdGenerator rpdGenerator;
    @Autowired
    private CharacteristicGenerator characteristicGenerator;

    public void generate(MultipartFile multipartFile, Boolean charCheckbox, Boolean rpdCheckbox, Boolean fosCheckbox) throws IOException {
        File targetFile = new File(documents.getDOCS_GEN_PATH() + "/" + multipartFile.getOriginalFilename());
        targetFile.createNewFile();
        multipartFile.transferTo(targetFile.getAbsoluteFile());

        documents.setSyllabus(targetFile);
        //Парсинг учебного плана
        FileInputStream fisExcel = new FileInputStream(documents.getSyllabus());
        SyllabusData syllabusData = excelParser.parse(fisExcel, getExtension(documents.getSyllabus().getName()));
        fisExcel.close();

        if(charCheckbox){
            characteristicGenerator.generate(syllabusData);
        }
        if(rpdCheckbox){
            rpdGenerator.generate(syllabusData);
        }
        if(fosCheckbox){
            fosGenerator.generate(syllabusData);
        }
    }

}
