package opopproto.docChecker;

import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.characteristic.CharacteristicParagraphData;
import opopproto.data.characteristic.CharacteristicTableData;
import opopproto.data.fos.FosData;
import opopproto.data.rpd.RpdData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.docChecker.characteristic.CharacteristicComplianceStateChecker;
import opopproto.docChecker.fos.FosComplianceStateChecker;
import opopproto.docChecker.rpd.RpdComplianceStateChecker;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.domain.ProfessionalArea;
import opopproto.domain.Standard;
import opopproto.parser.WordParser;
import opopproto.util.Documents;
import opopproto.parser.ExcelParser;
import opopproto.util.StandardComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ComplianceStateChecker {
    @Autowired
    private Documents documents;

    @Autowired
    private ExcelParser excelParser;
    @Autowired
    private WordParser wordParser;
    @Autowired
    private CharacteristicComplianceStateChecker characteristicChecker;
    @Autowired
    private FosComplianceStateChecker fosComplianceStateChecker;
    @Autowired
    private RpdComplianceStateChecker rpdComplianceStateChecker;


    public ComplianceState check() throws IOException {
        //Парсинг учебного плана
        FileInputStream fisExcel = new FileInputStream(documents.getSyllabus());
        SyllabusData syllabusData = excelParser.parse(fisExcel, getExtension(documents.getSyllabus().getName()));
        fisExcel.close();

        //Парсинг характеристики
        FileInputStream fisWord = new FileInputStream(documents.getCharacteristics());
        CharacteristicData characteristicData = wordParser.parseCharacteristics(fisWord);

        String namingErrorFos = fosComplianceStateChecker.checkNaming(documents.getFos(),
                syllabusData.getDisciplinesData().getAllDisciplines());
        String namingErrorRpd = rpdComplianceStateChecker.checkNaming(documents.getRpd(),
                syllabusData.getDisciplinesData().getAllDisciplines());

        //Удаление Б3
        fosComplianceStateChecker.removeB3(documents.getFos());
        rpdComplianceStateChecker.removeB3(documents.getRpd());

        //Парсинг ФОС
        List<FosData> fosDataList = wordParser.parseFos(documents.getFos());

        //Парсинг РПД
        List<RpdData> rpdDataList = wordParser.parseRpd(documents.getRpd());

        ComplianceState complianceState = new ComplianceState();
        //Ошибки характеристики
        List<String> characteristicErrors = characteristicChecker.check(syllabusData, characteristicData);
        complianceState.setCharacteristicErrors(characteristicErrors);

        //Ошибки ФОС
        List<String> fosErrors = new ArrayList<>();
        if(namingErrorFos!=null)
            fosErrors.add(namingErrorFos);
        fosErrors.addAll(fosComplianceStateChecker.check(fosDataList, rpdDataList));
        complianceState.setFosErrors(fosErrors);

        //TODO:Чек ошибок РПД
        List<String> rpdErrors = new ArrayList<>();
        if(namingErrorRpd!=null)
            rpdErrors.add(namingErrorRpd);
        rpdErrors.addAll(rpdComplianceStateChecker.check(rpdDataList, syllabusData));
        complianceState.setRpdErrors(rpdErrors);

        return complianceState;
    }


    private String getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1)).get();
    }

}
