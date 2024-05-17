package opopproto.parser;

import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.characteristic.CharacteristicParagraphData;
import opopproto.data.characteristic.CharacteristicTableData;
import opopproto.data.fos.Evaluate;
import opopproto.data.fos.EvaluateType;
import opopproto.data.fos.FosData;
import opopproto.data.rpd.AppendixData;
import opopproto.data.rpd.EvaluateCompetences;
import opopproto.data.rpd.RpdData;
import opopproto.data.rpd.RpdTitle;
import opopproto.domain.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WordParser {
    private XWPFDocument document;
    private final CharacteristicData characteristicData = new CharacteristicData();
    public CharacteristicData parseCharacteristics(FileInputStream fis) throws IOException {
        document = new XWPFDocument(fis);
        CharacteristicTableData characteristicTableData = parseCharacteristicTables();
        CharacteristicParagraphData characteristicParagraphData = parseCharacteristicText();

        characteristicData.setTableData(characteristicTableData);
        characteristicData.setParagraphData(characteristicParagraphData);

        return characteristicData;
    }
    public List<RpdData> parseRpd(Map<String, File> rpd) throws IOException {
        List<RpdData> rpdDataList = new ArrayList<>();
        for (Map.Entry<String, File> entry : rpd.entrySet()) {
            RpdData rpdData = new RpdData();
            rpdData.setRpdName(entry.getKey().substring(0, entry.getKey().length()-5));
            FileInputStream fis = new FileInputStream(entry.getValue());
            document = new XWPFDocument(fis);
            RpdTitle rpdTitle = new RpdTitle();
            AppendixData appendixData = new AppendixData();
            List<XWPFTable> tables = document.getTables();
            for (var table: tables) {
                //Парсинг таблиц в титуле
                if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Дисциплина (модуль)") && table.getRows().size()==6) {
                    try {
                        rpdTitle.setTitleEqualsDiscipline(table.getRow(0).getCell(1).getText().trim());
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsDiscipline(null);
                    }
                    try {
                        rpdTitle.setTitleEqualsLevel(table.getRow(2).getCell(1).getText().trim());
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsLevel(null);
                    }
                    try {
                        rpdTitle.setTitleEqualsQualification(table.getRow(4).getCell(1).getText().trim());
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsQualification(null);
                    }
                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Уровень образования") && table.getRows().size()==4) {
                    try {
                        rpdTitle.setTitleEqualsLevel(table.getRow(0).getCell(1).getText().trim());
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsLevel(null);
                    }
                    try {
                        rpdTitle.setTitleEqualsQualification(table.getRow(2).getCell(1).getText().trim());
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsQualification(null);
                    }

                }
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("на кафедре")) {
                    try {
                        rpdTitle.setTitleEqualsSpeciality(table.getRow(2).getCell(1).getText().trim());
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsSpeciality(null);
                    }
                    try {
                        rpdTitle.setTitleEqualsProfile(table.getRow(3).getCell(1).getText().trim());;
                    }
                    catch (Exception e){
                        rpdTitle.setTitleEqualsProfile(null);;
                    }
                }
                //Компетенции
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Код компетенции")) {
                    try {
                        rpdData.setCompetences(parseRpdCompetencesTable(table));
                    }
                    catch (Exception e){
                        rpdData.setCompetences(null);
                    }
                }
                //Наимен. оцен средств
                else if (table.getRow(0).getCell(0).getText().trim().equals("№ п/п")
                        && table.getRow(0).getCell(1).getText().equals("Код формируемой компетенции")) {
                    String competence = "";
                    for (var row: table.getRows()) {
                        //Пропуск шапки
                        if(row.getCell(0).getText().trim().equals("№ п/п")){
                            continue;
                        }
                        //Если строка с индексом компетенции
                        try {
                            if(!row.getCell(1).getText().isBlank()){
                                competence = row.getCell(1).getText().trim().toUpperCase();
                            }
                            rpdData.getEvaluateCompetences().add(
                                    new EvaluateCompetences(competence, row.getCell(2).getText().trim().toUpperCase(),
                                            row.getCell(3).getText().trim())
                            );
                        }
                        catch (Exception ignored){}
                    }
                }
                //Приложение
                //TODO парсить по местоположению или по названию строк?
                else if (table.getRow(0).getCell(0).getText()
                        .trim().equals("Дисциплина (модуль)") ||
                        table.getRow(0).getCell(0).getText()
                                .trim().equals("Практика")) {
                    try {
                        appendixData.setEqualsDiscipline(table.getRow(0).getCell(1).getText().trim());
                        appendixData.setEqualsLevel(table.getRow(1).getCell(1).getText().trim());
                        appendixData.setEqualsQualification(table.getRow(2).getCell(1).getText().trim());
                        appendixData.setEqualsSpeciality(table.getRow(3).getCell(1).getText().trim());
                        appendixData.setEqualsProfile(table.getRow(4).getCell(1).getText().trim());
                        String competencesString = table.getRow(5).getCell(1).getText();
                        if(competencesString.contains(",")){
                            appendixData.setCompetencesIndexes(Arrays.stream(competencesString
                                    .split(",")).toList().stream().map(String::trim).toList());
                        }
                        else if (competencesString.contains(";")) {
                            appendixData.setCompetencesIndexes(Arrays.stream(competencesString
                                    .split(";")).toList().stream().map(String::trim).toList());
                        }
                        else {
                            appendixData.setCompetencesIndexes(List.of(competencesString.trim()));
                        }
                        appendixData.setZeCount(parseZe(table.getRow(8).getCell(1).getText()));
                        appendixData.setTotalHours(parseTotalHours(table.getRow(8).getCell(1).getText()));

                        List<String> controlForms = Arrays.stream(table.getRow(9).getCell(1).getText().trim()
                                        .split(","))
                                .map(cf -> cf.trim().toLowerCase())
                                .toList();
                        appendixData.setControlForms(controlForms);
                    }
                    catch (Exception ignored){
                        appendixData = null;
                    }

                }
                // Таблица 1 TODO: Попробовать парсить опираясь на текст в строках а не на положение?
                else if(table.getRow(0).getCell(0).getText()
                        .trim().equals("Форма обучения")){
                    List<VolumeSemester> volumeSemesters = new ArrayList<>();
                    if(rpdData.isPractice()){
                        VolumeSemester volumeSemester = new VolumeSemester();
                        try {
                            volumeSemester.setIw(tryParseInt(table.getRow(3).getCell(1).getText()));
                            volumeSemester.setControl(tryParseInt(table.getRow(7).getCell(1).getText()));
                            volumeSemester.setTotal(tryParseInt(table.getRow(8).getCell(1).getText()));
                            volumeSemester.setZeCount(tryParseInt(table.getRow(9).getCell(1).getText()));

                            volumeSemester.setSemester(Integer.parseInt(table.getRow(1).getCell(1).getText()));
                            volumeSemesters.add(volumeSemester);
                        }
                        catch (Exception ignored){}
                    }
                    else {
                        for (int i = 0; i < 4; i++) {
                            VolumeSemester volumeSemester = new VolumeSemester();
                            int index = 1 + i;
                            try {
                                if(tryParseInt(table.getRow(1).getCell(index).getText())==0){
                                    continue;
                                }
                                volumeSemester.setContactWork(tryParseInt(table.getRow(2).getCell(index).getText()));
                                volumeSemester.setLectures(tryParseInt(table.getRow(4).getCell(index).getText()));
                                volumeSemester.setPw(tryParseInt(table.getRow(5).getCell(index).getText()));
                                volumeSemester.setLw(tryParseInt(table.getRow(6).getCell(index).getText()));
                                volumeSemester.setIw(tryParseInt(table.getRow(7).getCell(index).getText()));
                                volumeSemester.setControl(tryParseInt(table.getRow(18).getCell(index).getText()));
                                volumeSemester.setTotal(tryParseInt(table.getRow(19).getCell(index).getText()));
                                volumeSemester.setZeCount(tryParseInt(table.getRow(20).getCell(index).getText()));

                                volumeSemester.setSemester(Integer.parseInt(table.getRow(1).getCell(index).getText()));
                            }
                            catch (Exception ignored){}

                            if(volumeSemester.getContactWork()!=0 || volumeSemester.getIw()!=0 ||
                                    volumeSemester.getPw()!=0|| volumeSemester.getLw()!=0 ||
                                    volumeSemester.getTotal()!=0 || volumeSemester.getLectures()!=0 ||
                                    volumeSemester.getControl()!=0 || volumeSemester.getZeCount()!=0){
                                volumeSemesters.add(volumeSemester);
                            }
                        }
                    }

                    rpdData.setVolumeSemesters(volumeSemesters);
                }
                // Таблица 3
                else if(table.getRow(0).getCell(0).getText()
                        .trim().equals("№")){
                    VolumeSemester volumeTotal = new VolumeSemester();
                    for (var row: table.getRows()) {
                        try {
                            if(row.getCell(1).getText().trim().equals("Итого часов")){
                                volumeTotal.setLectures(tryParseInt(row.getCell(2).getText()));
                                volumeTotal.setPw(tryParseInt(row.getCell(3).getText()));
                                volumeTotal.setLw(tryParseInt(row.getCell(4).getText()));
                                volumeTotal.setIw(tryParseInt(row.getCell(5).getText()));
                                volumeTotal.setTotal(tryParseInt(row.getCell(6).getText()));
                            }
                        }
                        catch (Exception ignored){}

                    }
                    rpdData.setVolumeTotal(volumeTotal);
                }
            }
            rpdData.setAppendixData(appendixData);
            rpdData.setRpdTitle(rpdTitle);
            rpdDataList.add(rpdData);

            Iterator<XWPFParagraph> iterator = document.getParagraphs().iterator();
//            List<BibliographyPair> bibliographyPairs = new ArrayList<>();

            while (iterator.hasNext()) {
                var paragraph = iterator.next();
                String text = paragraph.getText().trim();
                if (text.contains("Целью освоения дисциплины") || text.contains("Целью практики") || text.contains("Цель практики")) {
                    rpdData.setSection3ContainsDiscipline1(text);
                }
                else if (text.equalsIgnoreCase("Программа практики")) {
                    paragraph = iterator.next();
                    text = paragraph.getText().trim();
                    while (text.isEmpty()){
                        paragraph = iterator.next();
                        text = paragraph.getText().trim();
                    }
                    rpdData.getRpdTitle().setTitleEqualsDiscipline(text);
                }
//                Pattern pattern = Pattern.compile("^(\\d+\\..*\\d{4}\\.)");
//                Matcher matcher = pattern.matcher(text);
//                if (matcher.find()) {
//                    BibliographyPair bibliographyPair = new BibliographyPair();
//                    Pattern patternNum = Pattern.compile("^(\\d+\\.)");
//                    Matcher matcherNum = patternNum.matcher(text);
//                    if(matcherNum.find()){
//                        String group = matcherNum.group(1);
//                        bibliographyPair.setNum(Integer.parseInt(group.substring(0, group.length()-1)));
//                    }
//                    Pattern patternYear = Pattern.compile("(\\d{4}\\.)");
//                    Matcher matcherYear = patternYear.matcher(text);
//                    if(matcherYear.find()){
//                        String group = matcherYear.group(1);
//                        bibliographyPair.setYear(Integer.parseInt(group.substring(0, group.length()-1)));
//                        bibliographyPairs.add(bibliographyPair);
//                    }
//                }
//                else if (paragraph.getNumFmt()!=null) {
//                    if(paragraph.getNumFmt().equals("decimal")){
//                        System.out.println();
//                    }
//                }
            }
//            rpdData.setBibliographyList(bibliographyPairs);
        }
        return rpdDataList;
    }
    public List<FosData> parseFos(Map<String, File> fos) throws IOException{
        List<FosData> fosDataList = new ArrayList<>();
        for (Map.Entry<String, File> entry : fos.entrySet()) {
            FileInputStream fis = new FileInputStream(entry.getValue());
            document = new XWPFDocument(fis);
            for (var table: document.getTables()) {
                //Нашли таблицу
                if(table.getRow(0).getCell(0).getText()
                        .trim().equals("Наименование оценочного средства")){
                    fosDataList.add(parseFosData(table, entry.getKey()));
                }
            }
        }
        return fosDataList;
    }
    private List<Competence> parseRpdCompetencesTable(XWPFTable table){
        List<Competence> competences = new ArrayList<>();
        Competence competence = null;
        List<String> competenceLevels = List.of("универсальные", "общепрофессиональные", "профессиональные");
        try {
            for (var row: table.getRows()) {
                //Пропуск шапки
                if(row.getCell(0).getText().equals("Код компетенции")){
                    continue;
                }
                //Пропуск уровня компетенции
                if(competenceLevels.contains(row.getCell(0).getText().trim().toLowerCase())){
                    continue;
                }
                //Если строка с названием компетенции
                if(!row.getCell(0).getText().isBlank()){
                    if(competence!=null){
                        competences.add(competence);
                    }
                    competence = new Competence();
                    competence.setIndex(row.getCell(0).getText().trim());
                    competence.setName(row.getCell(1).getText().trim());
                }
                String idText = row.getCell(2).getText().trim();
                String name = row.getCell(3).getText().trim();
                Pattern patternId = Pattern.compile("(ИД\\s*-\\s*\\d)");
                Matcher matcherId = patternId.matcher(idText);
                if(matcherId.find()){
                    ID id = new ID(matcherId.group(1), name);
                    competence.getIds().add(id);
                }
            }
            //Добавляем последнюю
            competences.add(competence);
        }
        catch (NullPointerException e){
            return null;
        }

        return competences;
    }
    private FosData parseFosData(XWPFTable table, String name){
        FosData fosData = new FosData();
        fosData.setFosName(name.substring(0, name.length()-9));
        //Паттерн для парсинга Индекс-Компетенция
        Pattern pattern = Pattern.compile("(ИД\\s*-\\s*\\d\\s*(УК|ОПК|УКИ|ОПКИ|ПК)\\s*-\\s*\\d+)", Pattern.CASE_INSENSITIVE);

        List<String> appendixesToFind = new ArrayList<>();
        List<Evaluate> evaluates = new ArrayList<>();
        Iterator<XWPFTableRow> tableIterator = table.getRows().iterator();
        //Пропуск шапки
        tableIterator.next();
        //Ищем все оцен. средства
        while (tableIterator.hasNext()){
            XWPFTableRow row = tableIterator.next();
            try {
                appendixesToFind.add(row.getCell(2).getText());
                Evaluate evaluate = new Evaluate();
                evaluate.setEvaluateType(defineEvaluateType(row.getCell(0).getText()));
                evaluate.setName(row.getCell(0).getText());
                Matcher matcher = pattern.matcher(row.getCell(1).getText().toUpperCase());
                List<String> indexesCompetences = new ArrayList<>();
                //Cбор всех индекс-компетенция
                while (matcher.find()) {
                    indexesCompetences.add(matcher.group(1).toUpperCase());
                }
                evaluate.setIndexesCompetences(indexesCompetences);
                evaluates.add(evaluate);
            }
            catch (Exception ignored){}
        }
        fosData.setEvaluates(evaluates);
        appendixesToFind.forEach(appendix-> fosData.getAppendixesExisting().put(appendix, false));
        //Ищем все приложения и данные титула
        Iterator<XWPFParagraph> iterator = document.getParagraphs().iterator();
        while (iterator.hasNext()){
            var paragraph = iterator.next();
            String text = paragraph.getText();
            //Парсим инфу с титула
            if (text.contains("промежуточной аттестации обучающихся по дисциплине (модулю)") ||
                    text.contains("промежуточной аттестации обучающихся по практике")) {
                paragraph = iterator.next();
                text = paragraph.getText();
                fosData.setTitleEqualsDiscipline(text.trim());
            }
            else{
                for (var appendix:appendixesToFind) {
                    if (text.contains("Приложение " + appendix.trim())) {
                        fosData.getAppendixesExisting().put(appendix, true);
                    }
                }
            }
        }

        return fosData;
    }
    private EvaluateType defineEvaluateType(String string){
        string = string.toLowerCase();
        if(string.contains("тест")){
            return EvaluateType.TEST;
        } else if (string.contains("экзамен")) {
            return EvaluateType.EKZAMEN;
        }
        else if (string.contains("зачет") || string.contains("зачёт")) {
            return EvaluateType.ZACHET;
        }
        else if (string.contains("лабор")) {
            return EvaluateType.LAB;
        }
        else if (string.contains("практ")) {
            return EvaluateType.PRACTICE;
        }
        else if (string.contains("собес")) {
            return EvaluateType.SOBES;
        }
        else if (string.contains("курсов")) {
            return EvaluateType.KURS;
        }
        else if (string.contains("отчёт") || string.contains("отчет")) {
            return EvaluateType.REPORT;
        }
        else if (string.contains("реферат")) {
            return EvaluateType.REFERAT;
        }
        else {
            return EvaluateType.UNKNOWN;
        }

    }
    private CharacteristicParagraphData parseCharacteristicText(){
        CharacteristicParagraphData characteristicParagraphData = new CharacteristicParagraphData();

        Iterator<XWPFParagraph> iterator = document.getParagraphs().iterator();
        int appendixInd = 1;
        while (iterator.hasNext()){
            var paragraph = iterator.next();
            String text = paragraph.getText();
            if (text.equals("Направление подготовки")) {
                paragraph = iterator.next();
                text = paragraph.getText();
                characteristicParagraphData.setTitleEqualsSpeciality(text);
            }
            else if(text.equals("Программа подготовки")){
                paragraph = iterator.next();
                text = paragraph.getText();
                characteristicParagraphData.setTitleEqualsProfile(text);
            }
            else if(text.equals("Квалификация выпускника")){
                paragraph = iterator.next();
                text = paragraph.getText();
                characteristicParagraphData.setTitleEqualsQualification(text);
            }
            else if(text.equals("Форма(ы) обучения")){
                paragraph = iterator.next();
                text = paragraph.getText();
                characteristicParagraphData.setTitleEqualsEducationForm(text);
            }
            else if(text.startsWith("Основная профессиональная образовательная программа (ОПОП)" +
                    " разработана в соответствии с требованиями Федерального закона ")){

                characteristicParagraphData.setTitleContainsProfile(text);
                characteristicParagraphData.setTitleContainsSpeciality(text);
            }
            else if (text.startsWith("Образовательная программа разработана в соответствии" +
                    " с федеральным государственным образовательным стандартом")) {
                characteristicParagraphData.setSection11ContainsSpeciality(text);
            }
            else if (text.contains("Федеральный государственный образовательный стандарт" +
                    " по направлению подготовки")) {
                characteristicParagraphData.setSection12ContainsSpeciality(text);
                characteristicParagraphData.setSection12ContainsFgos(text);
            }
            else if (text.startsWith("Типы задач профессиональной деятельности выпускников")) {
                String taskTypesText = text.split(":")[1];
                if(text.charAt(text.length()-1)=='.'){
                    taskTypesText = taskTypesText.substring(0, taskTypesText.length() - 1);
                }

                characteristicParagraphData.setSection21ListOfTaskTypes(Arrays.stream(taskTypesText.split(",")).toList());
            }
            else if (text.startsWith("Перечень профессиональных стандартов, соотнесенных с образовательной программой," +
                    " из перечня ФГОС ВО, приведен в Приложении А")) {
                characteristicParagraphData.setSection22ContainsSpeciality(text);
            }
            else if (text.contains("Направленности (профили) образовательных программ в рамках направления подготовки")) {
                paragraph = iterator.next();
                text = paragraph.getText();

                characteristicParagraphData.setSection31ContainsProfile(text);
                characteristicParagraphData.setSection31ContainsSpeciality(text);
                characteristicParagraphData.setSection31ContainsAllTaskTypes(text);
            }
            else if (text.contains("Квалификация, присваиваемая выпускникам образовательных программ: ")) {
                characteristicParagraphData.setSection32ContainsQualification(text);
            }
            else if (text.contains("Формы обучения:")) {
                characteristicParagraphData.setSection34ContainsEducationForm(text);
            }
            else if (text.startsWith("Учебные планы подготовки")) {
                characteristicParagraphData.setSection541ContainsSpeciality(text);
                characteristicParagraphData.setSection541ContainsProfile(text);
            }
            else if (text.startsWith("Государственной итоговой (итоговой) аттестацией по направлению подготовки")) {
                characteristicParagraphData.setSection545ContainsSpeciality(text);
            }

            else if (text.equals("Перечень")) {
                if(appendixInd==1){
                    iterator.next();
                    paragraph = iterator.next();
                    text = paragraph.getText();
                    characteristicParagraphData.setAppendixAContainsSpeciality(text);
                    paragraph = iterator.next();
                    text = paragraph.getText();
                    characteristicParagraphData.setAppendixAContainsProfile(text);
                    appendixInd++;
                }
                else if (appendixInd ==2) {
                    paragraph = iterator.next();
                    text = paragraph.getText();
                    characteristicParagraphData.setAppendixBContainsSpeciality(text);
                    paragraph = iterator.next();
                    text = paragraph.getText();
                    characteristicParagraphData.setAppendixBContainsProfile(text);
                }
            }

        }


        return characteristicParagraphData;
    }
    private CharacteristicTableData parseCharacteristicTables(){
        List<XWPFTable> tables = document.getTables();
        final String U_COMPETENCE_HEADER = "Категория универсальных компетенций";
        final String OP_COMPETENCE_HEADER = "Код и наименование общепрофессиональной компетенции";
        final String P_COMPETENCE_HEADER = "Код и наименование профессиональной компетенции";
        final String P_COMPETENCE_TASK_TYPES_HEADER = "Задача ПД";

        CharacteristicTableData characteristicTableData = new CharacteristicTableData();

        int ind = 1;
        for (var table: tables) {
            //Если нашли таблицу сокращений
            if (table.getRow(0).getCell(1).getText().contains("зачетная единица")){
                for (var row: table.getRows()) {
                    if(row.getCell(0).getText().equals("ПООП")){
                        try {
                            characteristicTableData.setPoopContainsSpecialty(row.getCell(1).getText());
                        }
                        catch (Exception e){
                            characteristicTableData.setPoopContainsSpecialty(null);
                        }
                    }
                    if(row.getCell(0).getText().equals("ФГОС ВО")){
                        try {
                            characteristicTableData.setFgosContainsSpecialty(row.getCell(1).getText());
                        }
                        catch (Exception e){
                            characteristicTableData.setFgosContainsSpecialty(null);
                        }
                    }
                }
            }
            //Если нашли таблицу с обл. проф. деят.
            if (table.getRow(0).getCell(0)
                    .getText().contains("Область профессиональной деятельности")){
                try {
                    String[] taskTypesArray = table.getRow(1).getCell(1).getText().split(", ");
                    characteristicTableData.setTaskTypes(Arrays.stream(taskTypesArray).sorted().toList());
                }
                catch (Exception e){
                    characteristicTableData.setTaskTypes(null);
                }

            }
            //Если нашли таблицу УК
            else if (table.getRow(0).getCell(0).getText().equals(U_COMPETENCE_HEADER)){
                try {
                    List<Competence> uCompetences = parseCharacteristicCompetencesTable(1, 2,
                            U_COMPETENCE_HEADER, table);
                    characteristicTableData.setUCompetences(uCompetences);
                }
                catch (Exception e){
                    characteristicTableData.setUCompetences(null);
                }

            }
            //Если нашли таблицу ОПК
            else if(table.getRow(0).getCell(0).getText().contains(OP_COMPETENCE_HEADER)){
                try {
                    List<Competence> opCompetences = parseCharacteristicCompetencesTable(0, 1,
                            OP_COMPETENCE_HEADER, table);
                    characteristicTableData.setOpCompetences(opCompetences);
                }
                catch (Exception e){
                    characteristicTableData.setOpCompetences(null);
                }
            }
            //Если нашли таблицу ПК
            else if (table.getRow(0).getCell(0).getText().contains(P_COMPETENCE_HEADER)) {
                try {
                    List<Competence> pCompetences = parseCharacteristicCompetencesTable(0, 1,
                            P_COMPETENCE_HEADER, table);
                    characteristicTableData.setPCompetences(pCompetences);
                }
                catch (Exception e){
                    characteristicTableData.setPCompetences(null);
                }
            }
            //Если нашли таблицу соответствия компетенций и типов задач
            else if (table.getRow(0).getCell(0).getText().contains(P_COMPETENCE_TASK_TYPES_HEADER)) {
                try {
                    Map<String, List<Competence>> pCompetences = parseCharacteristicCompetencesTable(
                            table, characteristicTableData);
                    characteristicTableData.setPCompetencesTaskTypes(pCompetences);
                }
                catch (Exception e){
                    characteristicTableData.setPCompetencesTaskTypes(null);
                }

            }
            // Парсинг похожих таблиц с дисциплинами
            else if(table.getRow(0).getCell(0).getText().equals("Индекс")
                    && table.getRow(0).getCell(1).getText().equals("Наименование дисциплины")){
                if(ind==1){
                    try {
                        List<Competence> competencesDisciplineMatrix = parseCharacteristicMatrixCompetenceDisciplines(table);
                        characteristicTableData.setMatrixCompetenceDisciplines(competencesDisciplineMatrix);
                    }
                    catch (Exception e){
                        characteristicTableData.setMatrixCompetenceDisciplines(null);
                    }

                    ind++;
                }
                else if(ind==2){
                    try {
                        List<Discipline> baseDisciplines = parseCharacteristicDisciplines(table);
                        characteristicTableData.setBaseDisciplines(baseDisciplines);
                    }
                    catch (Exception e){
                        characteristicTableData.setBaseDisciplines(null);
                    }
                    ind++;
                }
                else if(ind==3){
                    try {
                        List<Discipline> varyDisciplines = parseCharacteristicDisciplines(table);
                        characteristicTableData.setVaryDisciplines(varyDisciplines);
                    }
                    catch (Exception e){
                        characteristicTableData.setVaryDisciplines(null);
                    }
                    ind++;
                }
                else{
                    List<Discipline> facDisciplines = parseCharacteristicDisciplines(table);
                    characteristicTableData.setFacDisciplines(facDisciplines);
                }
            }
            
            //Приложение А
            else if (table.getRow(0).getCell(0).getText().contains("№ п/п")) {
                List<Standard> standards = parseCharacteristicAppendixA(table);
                characteristicTableData.setAppendixAData(standards);
            }
            //Приложение А
            else if (table.getRow(0).getCell(0).getText().contains("Код и наименование профессионального стандарта")) {
                List<Standard> standards = parseCharacteristicAppendixB(table);
                characteristicTableData.setAppendixBData(standards);
            }

        }

        return characteristicTableData;
    }
    private List<Competence> parseCharacteristicCompetencesTable(int competenceTableIndex, int idTableIndex,
                                                                 String tableHeader, XWPFTable table){
        List<Competence> competences = new ArrayList<>();
        Competence competence = null;
        int idIndex = 1;

        for (var row: table.getRows()) {
            //Пропуск шапки
            if(row.getCell(0).getText().equals(tableHeader)){
                continue;
            }
            //Если строка с названием компетенции
            if(!row.getCell(competenceTableIndex).getText().isBlank()){
                if(competence!=null){
                    competences.add(competence);
                }
                competence = new Competence();
                idIndex = 1;
                // Паттерн для поиска индекса и названия компетенции
                Pattern pattern = Pattern.compile("^(УК-\\d+|ОПК-\\d+|УКи-\\d+|ОПКи-\\d+|ПК-\\d+)[\t\\. ]+(.*)$");

                Matcher matcher = pattern.matcher(row.getCell(competenceTableIndex).getText());
                if (matcher.find()) {
                    competence.setIndex(matcher.group(1));
                    competence.setName(matcher.group(2));

                }
            }
            //Между кодом индикатора и названием стоит индекс компетенции
            String idText = row.getCell(idTableIndex).getText().trim();
            //Удаляем идентификатор, индекс компетенции и пробелы
            Pattern patternNameInverse = Pattern.compile("(ИД\\s*-\\s*\\d\\s*(УК|ОПК|УКи|ОПКи|ПК)\\s*-\\s*\\d+)",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcherName = patternNameInverse.matcher(idText);
            if(matcherName.find()){
                String name = idText.replaceAll(matcherName.group(1), "").trim();
                Pattern patternId = Pattern.compile("(ИД\\s*-\\s*\\d)");
                Matcher matcherId = patternId.matcher(idText);
                if(matcherId.find()){
                    ID id = new ID(matcherId.group(1), name);
                    competence.getIds().add(id);
                    idIndex++;
                }
            }
        }
        //Добавляем последнюю
        competences.add(competence);

        return competences;
    }
    //Метод для таблицы соответствий
    private Map<String, List<Competence>> parseCharacteristicCompetencesTable(XWPFTable table,
                                                                              CharacteristicTableData tableData){
        Map<String, List<Competence>> taskTypesCompetences = new HashMap<>();
        Map<String, List<Standard>> taskTypeStandards = new HashMap<>();

        List<Competence> competences = new ArrayList<>();
        List<Standard> standards = new ArrayList<>();

        Competence competence = null;
        String taskTypeName = null;
        int idIndex = 1;
        //Все возможные типы задач для пропуска шапки
        List<String> taskTypesHeader = new ArrayList<>(List.of("научно-исследовательский", "педагогический",
                "организационно-управленческий", "технологический", "проектный"));
        for (var row: table.getRows()) {
            //Пропуск шапки
            if(row.getCell(0).getText().equals("Задача ПД") ||
                    row.getCell(0).getText().equals("Тип задач профессиональной деятельности")){
                continue;
            }
            //Если попали на тип деят.
            if(taskTypesHeader.contains(row.getCell(0).getText().toLowerCase())){
                if(taskTypeName!=null){
                    //Добавляем последнюю
                    competences.add(competence);
                    taskTypesCompetences.put(taskTypeName, competences);
                    taskTypeStandards.put(taskTypeName, standards);

                    competences.clear();
                    standards.clear();
                }
                taskTypeName = row.getCell(0).getText();
            }
            else{
                //Парсим стандарты
                if(!row.getCell(4).getText().isBlank()){
                    for (var par:row.getCell(4).getParagraphs()) {
                        String regex = "(\\d+\\.\\d+)\\s(.*)"; // регулярное выражение для поиска кода и названия

                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(par.getText());

                        if (matcher.find()) {
                            Standard standard = new Standard();
                            standard.setCode(matcher.group(1));
                            standard.setName(matcher.group(2));

                            standards.add(standard);
                        }
                    }
                }
                //Если строка с названием компетенции
                if(!row.getCell(2).getText().isBlank()){
                    if(competence!=null){
                        competences.add(competence);
                    }
                    competence = new Competence();
                    idIndex = 1;
                    // Паттерн для поиска индекса и названия компетенции
                    Pattern pattern = Pattern.compile("^(УК-\\d+|ОПК-\\d+|УКи-\\d+|ОПКи-\\d+|ПК-\\d+)[\t\\. ]+(.*)$");

                    Matcher matcher = pattern.matcher(row.getCell(2).getText());
                    if (matcher.find()) {
                        competence.setIndex(matcher.group(1));
                        competence.setName(matcher.group(2));

                    }
                }
                //Между кодом индикатора и названием стоит индекс компетенции
                String idText = row.getCell(3).getText();
                //Удаляем идентификатор, индекс компетенции и пробелы
                Pattern patternNameInverse = Pattern.compile("(ИД\\s*-\\s*\\d\\s*(УК|ОПК|УКи|ОПКи|ПК)\\s*-\\s*\\d+)",
                        Pattern.CASE_INSENSITIVE);
                Matcher matcherName = patternNameInverse.matcher(idText);
                if(matcherName.find()){
                    String name = idText.replaceAll(matcherName.group(1), "").trim();
                    Pattern patternId = Pattern.compile("(ИД\\s*-\\s*\\d)");
                    Matcher matcherId = patternId.matcher(idText);
                    if(matcherId.find()){
                        ID id = new ID(matcherId.group(1), name);
                        competence.getIds().add(id);
                        idIndex++;
                    }
                }
            }

        }
        //Добавляем последнюю
        competences.add(competence);
        taskTypesCompetences.put(taskTypeName, competences);
        taskTypeStandards.put(taskTypeName, standards);

        tableData.setTaskTypesStandards(taskTypeStandards);
        return taskTypesCompetences;
    }
    private List<Competence> parseCharacteristicMatrixCompetenceDisciplines(XWPFTable table){
        // Паттерн для поиска индекса и названия компетенции
        Pattern pattern = Pattern.compile("^(УК-\\d+|ОПК-\\d+|УКи-\\d+|ОПКи-\\d+|ПК-\\d+)*$");

        List<Competence> competences = new ArrayList<>();
        Competence competence = null;

        for (var row: table.getRows()){
            //Пропуск шапки
            if(row.getCell(0).getText().equals("Индекс")){
                continue;
            }
            Matcher matcher = pattern.matcher(row.getCell(0).getText());
            //Если строка с компетенцией
            if(matcher.find()){
                if(competence!=null){
                    competences.add(competence);
                }
                competence = new Competence();
                competence.setIndex(row.getCell(0).getText());
                competence.setName(row.getCell(1).getText());
            }
            else {
                Discipline discipline = new Discipline(row.getCell(0).getText(),
                        row.getCell(1).getText());

                competence.getDisciplines().add(discipline);
            }
        }
        //Добавляем последнее
        competences.add(competence);

        return competences;
    }

    private List<Discipline> parseCharacteristicDisciplines(XWPFTable table){
        List<Discipline> disciplines = new ArrayList<>();
        for (var row: table.getRows()){
            //Пропуск шапки
            if(row.getCell(0).getText().equals("Индекс")){
                continue;
            }
            Discipline discipline = new Discipline(row.getCell(0).getText(),
                    row.getCell(1).getText());
            disciplines.add(discipline);
        }

        return disciplines;
    }
    private List<Standard> parseCharacteristicAppendixA(XWPFTable table){
        List<Standard> standards = new ArrayList<>();
        for (var row: table.getRows()){
            //Пропуск шапки
            if(row.getCell(0).getText().contains("№ п/п")){
                continue;
            }
            Standard standard = new Standard();
            standard.setCode(row.getCell(1).getText());
            standard.setName(row.getCell(2).getText());
            standards.add(standard);
        }

        return standards;
    }
    private List<Standard> parseCharacteristicAppendixB(XWPFTable table){
        List<Standard> standards = new ArrayList<>();
        var iterator = table.getRows().iterator();
        iterator.next();

        Standard standard = new Standard();
        LaborFunction laborFunction = new LaborFunction();
        while(iterator.hasNext()){
            XWPFTableRow row = iterator.next();

            //Если новый стандарт
            if(!row.getCell(0).getText().isBlank()){

                String regex = "(\\d+\\.\\d+)\\s(.*)"; // регулярное выражение для поиска кода и названия

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(row.getCell(0).getText());

                if (matcher.find()) {
                    standard = new Standard();
                    laborFunction = new LaborFunction();
                    standard.setCode(matcher.group(1));
                    standard.setName(matcher.group(2));
                    standards.add(standard);
                }
            }
            //труд функ
            if(!row.getCell(1).getText().isBlank() && !row.getCell(1).getText().equals("код")){
                laborFunction.setCode(row.getCell(1).getText());
                laborFunction.setName(row.getCell(2).getText());
                standard.setLaborFunction(laborFunction);
            }
            //под труд функ
            if(!row.getCell(4).getText().isBlank() && !row.getCell(5).getText().equals("код")){
                LaborFunction subFunction = new LaborFunction();
                subFunction.setCode(row.getCell(5).getText());
                subFunction.setName(row.getCell(4).getText());
                laborFunction.getLaborFunctions().add(subFunction);
            }

        }

        return standards;
    }

    private int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseZe(String string){
        string = string.toLowerCase().replaceAll(" ", "");
        Pattern patternZe = Pattern.compile("(\\d+\\s*(з\\.е|зет|зачётныхединиц|зачетныхединиц|зачетныхед|зачётныеединицы|зачетныеединицы))", Pattern.CASE_INSENSITIVE);
        Pattern patternZeCount = Pattern.compile("(\\d+)");
        Matcher matcherZe = patternZe.matcher(string);
        if(matcherZe.find()){
            String group = matcherZe.group(1);
            Matcher matcherZeCount = patternZeCount.matcher(group);
            if(matcherZeCount.find()){
                return Integer.parseInt(matcherZeCount.group(1));
            }
        }
        return -1;
    }
    private int parseTotalHours(String string){
        string = string.toLowerCase().replaceAll(" ", "");
        Pattern patternTotalHours = Pattern.compile("(\\d+\\s*(часа|часов|ч|час))", Pattern.CASE_INSENSITIVE);
        Pattern patternTotalHoursCount = Pattern.compile("(\\d+)");
        Matcher matcher = patternTotalHours.matcher(string);
        if(matcher.find()){
            String group = matcher.group(1);
            Matcher matcherTotalHoursCount = patternTotalHoursCount.matcher(group);
            if(matcherTotalHoursCount.find()){
                return Integer.parseInt(matcherTotalHoursCount.group(1));
            }
        }
        return -1;
    }


}
