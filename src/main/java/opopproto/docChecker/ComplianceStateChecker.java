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

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

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

        //Удаление Б3
        fosComplianceStateChecker.removeB3(documents.getFos());
        rpdComplianceStateChecker.removeB3(documents.getRpd());

        //Проверка составляющих файлов
        String namingErrorFos = fosComplianceStateChecker.checkNaming(documents.getFos(),
                syllabusData.getDisciplinesData().getAllDisciplines());
        String namingErrorRpd = rpdComplianceStateChecker.checkNaming(documents.getRpd(),
                syllabusData.getDisciplinesData().getAllDisciplines());


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
        fosErrors.addAll(fosComplianceStateChecker.check(fosDataList, rpdDataList, syllabusData, characteristicData));
        complianceState.setFosErrors(fosErrors);

        List<String> rpdErrors = new ArrayList<>();
        if(namingErrorRpd!=null)
            rpdErrors.add(namingErrorRpd);
        rpdErrors.addAll(rpdComplianceStateChecker.check(rpdDataList, syllabusData, characteristicData));
        complianceState.setRpdErrors(rpdErrors);

        return complianceState;
    }


}
