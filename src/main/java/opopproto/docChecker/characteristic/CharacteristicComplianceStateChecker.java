package opopproto.docChecker.characteristic;

import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.characteristic.CharacteristicParagraphData;
import opopproto.data.characteristic.CharacteristicTableData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.domain.ProfessionalArea;
import opopproto.domain.Standard;
import opopproto.util.StandardComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CharacteristicComplianceStateChecker {
    @Autowired
    private StandardComparator standardComparator;

    //Проверка характеристики
    public List<String> check(SyllabusData syllabusData,
                                                 CharacteristicData characteristicData){
        List<String> characteristicErrors = new ArrayList<>();
        //Проверка табличных данных характеристики
        CharacteristicTableData characteristicTableData = characteristicData.getTableData();
        String speciality = syllabusData.getSyllabusTitle().getSpecialty().trim().toLowerCase();
        if(!characteristicTableData.getPoopContainsSpecialty().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("<b>Не указано или неправильно указано направление в перечне сокращений. Строка 'ПООП'</b>");
        }
        if(!characteristicTableData.getFgosContainsSpecialty().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("<b>Не указано или неправильно указано направление в перечне сокращений. Строка 'ФГОС ВО'</b>");
        }
        //Проверка типов задач
        if(!prettyListString(characteristicTableData.getTaskTypes()).equals(prettyListString(syllabusData.getSyllabusTitle().getTaskTypes()))){

            String stringSyllabusTaskTypes = String.join(", ", prettyListString(syllabusData.getSyllabusTitle().getTaskTypes()));
            String stringCharacteristicTaskTypes = String.join(", ", prettyListString(characteristicTableData.getTaskTypes()));

            characteristicErrors.add("<b>Информация о типах задач несовпадает.</b> Типы задач в учебном плане: " +
                    stringSyllabusTaskTypes + ". Типы задач в характеристике: "+ stringCharacteristicTaskTypes);
        }
        //Проверка ун компетенций
        String error = checkCharacteristicCompetences(characteristicTableData.getUCompetences(), syllabusData.getUCompetences());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице универсальных компетенций.</b><br>"+error);
        }
        //Проверка общ проф компетенций
        error = checkCharacteristicCompetences(characteristicTableData.getOpCompetences(), syllabusData.getOpCompetences());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице профессиональных компетенций.</b><br>"+error);
        }
        //Проверка проф компетенций
        error = checkCharacteristicCompetences(characteristicTableData.getPCompetences(), syllabusData.getPCompetences());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице общепрофессиональных компетенций.</b><br>"+error);
        }
        //Проверка матрицы соответствия
        error = checkCharacteristicMatrix(characteristicTableData.getMatrixCompetenceDisciplines(), syllabusData.getCompetences());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в матрице соответствия компетенций и элементов учебного плана.</b><br>" + error);
        }
        //Проверка обяз дисциплин
        error = checkCharacteristicDisciplines(characteristicTableData.getBaseDisciplines(), syllabusData.getDisciplinesData().getBaseDisciplines());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице обязательных дисциплин.</b><br>"+error);
        }
        //Проверка вар дисциплин
        error = checkCharacteristicDisciplines(characteristicTableData.getVaryDisciplines(), syllabusData.getDisciplinesData().getVaryDisciplines());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице вариативных дисциплин.</b><br>"+error);
        }
        //Проверка факультативов
        error = checkCharacteristicDisciplines(characteristicTableData.getFacDisciplines(), syllabusData.getDisciplinesData().getBlock4DisciplineList());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице факультативов.</b><br>"+error);
        }

        //Проверка приложения А
        List<Standard> standardsASyllabus = syllabusData.getSyllabusTitle().getProfessionalAreas()
                .stream().map(ProfessionalArea::getStandards).toList().stream().flatMap(List::stream)
                .toList();
        List<Standard> standardsACharacteristic = characteristicTableData.getAppendixAData();
        error = checkCharacteristicAppendixA(standardsACharacteristic, standardsASyllabus);
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице Приложении А.</b><br>"+error);
        }

        //Проверка приложения Б
        List<Standard> standards = syllabusData.getSyllabusTitle().getProfessionalAreas()
                .stream().map(ProfessionalArea::getStandards).flatMap(List::stream).toList();
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
        List<Standard> standardsBCharacteristic = characteristicTableData.getAppendixBData();
        error = checkCharacteristicAppendixB(standardsBCharacteristic, standardsBSyllabus);
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице Приложении Б.</b><br>"+error);
        }

        //Проверка соответствия компетенций и типов задач профессиональной деятельности
        //Проверка тип задач - стандарты
        error = checkTaskTypesStandards(syllabusData.getSyllabusTitle().getTaskTypes(), standardsASyllabus,
                characteristicTableData.getTaskTypesStandards());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице соответствия компетенций и типов задач профессиональной деятельности.</b><br>"+error);
        }
        //Проверка тип задач - компетенции
        error = checkTaskTypesCompetences(syllabusData.getSyllabusTitle().getTaskTypes(), syllabusData.getPCompetences(),
                characteristicTableData.getPCompetencesTaskTypes());
        if(error!=null){
            characteristicErrors.add("<b>Ошибка в таблице соответствия компетенций и типов задач профессиональной деятельности.</b><br>"+error);
        }

        //Проверка текстовых данных характеристики
        CharacteristicParagraphData characteristicParagraphData = characteristicData.getParagraphData();
        String profile = syllabusData.getSyllabusTitle().getProfile().trim().toLowerCase();
        String qualification = syllabusData.getSyllabusTitle().getQualification().trim().toLowerCase();
        String educationForm = syllabusData.getSyllabusTitle().getEducationForm().trim().toLowerCase();
        String fgos = syllabusData.getSyllabusTitle().getFgos().trim().toLowerCase();
        if(!speciality.equals(characteristicParagraphData.getTitleEqualsSpeciality().trim().toLowerCase())){
            characteristicErrors.add("В титульном листе не указано/неправильно указано направление подготовки");
        }
        if(!profile.equals(characteristicParagraphData.getTitleEqualsProfile().trim().toLowerCase())){
            characteristicErrors.add("В титульном листе не указана/неправильно указана программа подготовки");
        }
        if(!qualification.equals(characteristicParagraphData.getTitleEqualsQualification().trim().toLowerCase())){
            characteristicErrors.add("В титульном листе не указана/неправильно указана квалификация");
        }
        if(!educationForm.equals(characteristicParagraphData.getTitleEqualsEducationForm().trim().toLowerCase())){
            characteristicErrors.add("В титульном листе не указана/неправильно указана форма/ы обучения");
        }
        if(!characteristicParagraphData.getTitleContainsProfile().trim().toLowerCase().contains(profile)){
            characteristicErrors.add("В титульном листе (стр.2) не указана/неправильно указана программа подготовки");
        }
        if(!characteristicParagraphData.getTitleContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В титульном листе (стр.2) не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getSection11ContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В разделе 1.1 не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getSection12ContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В разделе 1.2 не указано/неправильно указано направление подготовки");
        }
        if(!getNumberFgos(fgos).equals(getNumberFgos(characteristicParagraphData.getSection12ContainsFgos().trim().toLowerCase()))){
            characteristicErrors.add("В разделе 1.2 не указан/неправильно указан ФГОС");
        }
        if(!new HashSet<>(prettyListString(characteristicParagraphData.getSection21ListOfTaskTypes()))
                .containsAll(prettyListString(syllabusData.getSyllabusTitle().getTaskTypes()))){
            characteristicErrors.add("В разделе 2.1 не указаны/неправильно указаны типы задач профессиональной деятельности");
        }
        if(!characteristicParagraphData.getSection22ContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В разделе 2.2 не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getSection31ContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В разделе 3.1 не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getSection31ContainsProfile().trim().toLowerCase().contains(profile)){
            characteristicErrors.add("В разделе 3.1 не указана/неправильно указана программа подготовки");
        }
        for (var taskType:syllabusData.getSyllabusTitle().getTaskTypes()) {
            if(!characteristicParagraphData.getSection31ContainsAllTaskTypes().contains(taskType)){
                characteristicErrors.add("В разделе 3.1 не указаны/неправильно указаны типы задач профессиональной деятельности");
                break;
            }
        }
        if(!characteristicParagraphData.getSection32ContainsQualification().trim().toLowerCase().contains(qualification)){
            characteristicErrors.add("В разделе 3.2 не указана/неправильно указана квалификация");
        }
        if(!characteristicParagraphData.getSection34ContainsEducationForm().trim().toLowerCase().contains(educationForm)){
            characteristicErrors.add("В разделе 3.4 не указана/неправильно указана форма/ы обучения");
        }
        if(!characteristicParagraphData.getSection541ContainsProfile().trim().toLowerCase().contains(profile)){
            characteristicErrors.add("В разделе 5.4.1 не указана/неправильно указана программа подготовки");
        }
        if(!characteristicParagraphData.getSection541ContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В разделе 5.4.1 не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getSection545ContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В разделе 5.4.5 не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getAppendixAContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В Приложении А не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getAppendixAContainsProfile().trim().toLowerCase().contains(profile)){
            characteristicErrors.add("В Приложении А не указана/неправильно указана программа подготовки");
        }
        if(!characteristicParagraphData.getAppendixBContainsSpeciality().trim().toLowerCase().contains(speciality)){
            characteristicErrors.add("В Приложении Б не указано/неправильно указано направление подготовки");
        }
        if(!characteristicParagraphData.getAppendixBContainsProfile().trim().toLowerCase().contains(profile)){
            characteristicErrors.add("В Приложении Б не указана/неправильно указана программа подготовки");
        }

        //Проверка соответсвия индексов
        error = checkCharacteristicIndexes(characteristicTableData.getPCompetencesTaskTypes(),
                characteristicTableData.getPCompetences());
        if(error!=null){
            characteristicErrors.add("Ошибка соответствия индексов компетенций (Таблицы 4.3-4.4).<br>"+error);
        }

        return characteristicErrors;
    }
    private String checkCharacteristicCompetences(List<Competence> competencesCharacteristic, List<Competence> competencesSyllabus){
        List<String> errorIndexes = new ArrayList<>();
        String errorMes = "";
        for (int i = 0; i < competencesCharacteristic.size(); i++) {
            try {
                Competence competenceS = competencesSyllabus.get(i);
                Competence competenceC = competencesCharacteristic.get(i);
                if(!competenceC.equals(competenceS)){
                    errorIndexes.add(competenceC.getIndex());
                }
            }
            catch (IndexOutOfBoundsException exception){
                if(competencesCharacteristic.size()>competencesSyllabus.size()){
                    errorMes += "Обнаружены лишние" + (competencesCharacteristic.size()-competencesSyllabus.size()) + "компетенции в характеристике";
                }
                else{
                    errorMes += "В характеристике нехватает"+ (competencesSyllabus.size()-competencesCharacteristic.size())+ "компетенций";
                }
                break;
            }
        }
        if(!errorIndexes.isEmpty()){
            String stringCharacteristicCompetences = String.join(";\n",
                    competencesCharacteristic.stream().filter(competence -> errorIndexes.contains(competence.getIndex()))
                            .map(competence -> competence.getIndex().trim() + " " + competence.getName().trim()).toList());

            if(!errorMes.isBlank()){
                return errorMes+"<br>"+"Наименование или индекс компетенций в "+String.join(", ", errorIndexes)+ " не совпадают с учебным планом." +
                        " Возможен неправильный порядок компетенций или отсутствие пробелов между словами." +
                        "<br>Компетенции с ошибками:<br>"+ stringCharacteristicCompetences;
            }
            else{
                return "Наименование или индекс компетенций в "+String.join(", ", errorIndexes)+ " не совпадают с учебным планом." +
                        " Возможен неправильный порядок компетенций или отсутствие пробелов между словами." +
                        "<br>Компетенции с ошибками:<br>"+ stringCharacteristicCompetences;
            }

        }
        if(!errorMes.isBlank()){
            return errorMes;
        }
        return null;
    }


    private String checkCharacteristicDisciplines(List<Discipline> disciplinesCharacteristic, List<Discipline> disciplinesSyllabus){
        List<String> errorIndexes = new ArrayList<>();
        String errorMes = "";
        for (int i = 0; i < disciplinesCharacteristic.size(); i++) {
            try {
                Discipline disciplineS = disciplinesSyllabus.get(i);
                disciplineS = new Discipline(disciplineS.getIndex(), disciplineS.getName());
                Discipline disciplineC = disciplinesCharacteristic.get(i);
                if(!disciplineC.equals(disciplineS)){
                    errorIndexes.add(disciplineC.getIndex());
                }
            }
            catch (IndexOutOfBoundsException exception){
                if(disciplinesCharacteristic.size()>disciplinesSyllabus.size()){
                    errorMes += "Обнаружены лишние" + (disciplinesCharacteristic.size()-disciplinesSyllabus.size()) + "дисциплин в характеристике";
                }
                else{
                    errorMes += "В характеристике нехватает"+ (disciplinesSyllabus.size()-disciplinesCharacteristic.size())+ "дисциплин";
                }
                break;
            }
        }
        if(!errorIndexes.isEmpty()){
            String stringCharacteristicDisciplines = String.join(";\n",
                    disciplinesCharacteristic.stream().filter(discipline -> errorIndexes.contains(discipline.getIndex()))
                            .map(discipline -> discipline.getIndex().trim() + " " + discipline.getName().trim()).toList());

            if(!errorMes.isBlank()){
                return errorMes+"<br>"+"Наименование или индекс дисциплин в "+String.join(", ", errorIndexes)+ " не совпадают с учебным планом." +
                        " Возможен неправильный порядок дисциплин или отсутствие пробелов между словами." +
                        "<br>Дисциплины с ошибками:<br>"+ stringCharacteristicDisciplines;
            }
            else{
                return "Наименование или индекс дисциплин в "+String.join(", ", errorIndexes)+ " не совпадают с учебным планом." +
                        " Возможен неправильный порядок дисциплин или отсутствие пробелов между словами." +
                        "<br>Дисциплины с ошибками:<br>"+ stringCharacteristicDisciplines;
            }

        }
        if(!errorMes.isBlank()){
            return errorMes;
        }
        return null;
    }

    private String checkCharacteristicMatrix(List<Competence> characteristicCompetences, List<Competence> syllabusCompetences){
        String matrixError = "";
        ArrayList<String> matrixErrorIndexesDisciplines = new ArrayList<>();
        ArrayList<String> matrixErrorIndexesCompetence = new ArrayList<>();
        for (int i = 0; i < characteristicCompetences.size(); i++) {
            try {
                Competence competenceS = syllabusCompetences.get(i);
                Competence competenceC = characteristicCompetences.get(i);
                if(competenceC.equals(competenceS)){
                    List<Discipline> competenceSDisciplines = competenceS.getDisciplines().stream().map(d -> new Discipline(d.getIndex(), d.getName())).toList();
                    if(!competenceC.getDisciplines().equals(competenceSDisciplines)){
                        matrixErrorIndexesDisciplines.add(competenceC.getIndex());
                    }
                }
                else {
                    matrixErrorIndexesCompetence.add(competenceC.getIndex());
                }
            }
            catch (IndexOutOfBoundsException exception){
                if(characteristicCompetences.size()>syllabusCompetences.size()){
                    matrixError += "Обнаружены лишние" + (characteristicCompetences.size()
                            -syllabusCompetences.size()) + "компетенции в характеристике<br>";
                }
                else{
                    matrixError += "В характеристике нехватает"+ (syllabusCompetences.size()
                            -characteristicCompetences.size())+ "компетенций<br>";
                }
                break;
            }

        }
        if(!matrixErrorIndexesDisciplines.isEmpty()){
            matrixError+="Дисциплины в "+String.join(", ", matrixErrorIndexesDisciplines)+ " не совпадают с учебным планом.";
        }
        if(!matrixErrorIndexesCompetence.isEmpty()){
            matrixError+="Наименование или индекс компетенций в "+String.join(", ", matrixErrorIndexesCompetence)+ " не совпадают с учебным планом.";
        }
        if(!matrixError.isBlank()){
            return matrixError;
        }

        return null;
    }
    private String checkCharacteristicAppendixA(List<Standard> characteristicStandards, List<Standard> syllabusStandards){
        List<String> errorIndexes = new ArrayList<>();
        String errorMes = "";
        for (int i = 0; i < characteristicStandards.size(); i++) {
            try {
                Standard standardS = syllabusStandards.get(i);
                Standard standardC = characteristicStandards.get(i);
                if(!standardC.equals(standardS)){
                    errorIndexes.add(standardC.getCode());
                }
            }
            catch (IndexOutOfBoundsException exception){
                if(characteristicStandards.size()>syllabusStandards.size()){
                    errorMes += "Обнаружены лишние" + (characteristicStandards.size()-syllabusStandards.size()) + "проф. стандарты в характеристике (Приложение А)";
                }
                else{
                    errorMes += "В характеристике (Приложение А) нехватает"+ (syllabusStandards.size()-characteristicStandards.size())+ "проф. стандарта/ов";
                }
                break;
            }
        }
        if(!errorIndexes.isEmpty()){
            String stringCharacteristicStandards = String.join(";\n",
                    characteristicStandards.stream().filter(standard -> errorIndexes.contains(standard.getCode()))
                            .map(standard -> standard.getCode().trim() + " " + standard.getName().trim()).toList());

            if(!errorMes.isBlank()){
                return errorMes+"<br>"+"Наименование или код проф. стандарта в "+String.join(", ", errorIndexes)+ " не совпадают с учебным планом." +
                        " Возможен неправильный порядок или отсутствие пробелов между словами." +
                        "<br>Проф. стандарты с ошибками:<br>"+ stringCharacteristicStandards;
            }
            else{
                return "Наименование или код проф. стандарта в "+String.join(", ", errorIndexes)+ " не совпадают с учебным планом." +
                        " Возможен неправильный порядок или отсутствие пробелов между словами." +
                        "<br>Проф. стандарты с ошибками:<br>"+ stringCharacteristicStandards;
            }

        }
        if(!errorMes.isBlank()){
            return errorMes;
        }
        return null;
    }
    private String checkCharacteristicAppendixB(List<Standard> characteristicStandards, List<Standard> syllabusStandards){
        syllabusStandards = new ArrayList<>(syllabusStandards);
        characteristicStandards = new ArrayList<>(characteristicStandards);

        characteristicStandards.sort(standardComparator);
        syllabusStandards.sort(standardComparator);

        String errorMes = "";
        ArrayList<String> errorLaborFunc = new ArrayList<>();
        ArrayList<String> errorStandards = new ArrayList<>();
        ArrayList<String> errorSubFunc = new ArrayList<>();
        for (int i = 0; i < characteristicStandards.size(); i++) {
            try {
                Standard standardS = syllabusStandards.get(i);
                Standard standardC = characteristicStandards.get(i);
                if(standardC.equals(standardS)){
                    if(!standardC.getLaborFunction().equals(standardS.getLaborFunction())){
                        errorLaborFunc.add(standardC.getCode());
                    }
                    else if(!standardC.getLaborFunction().getLaborFunctions().
                            equals(standardS.getLaborFunction().getLaborFunctions())){
                        errorSubFunc.add(standardC.getCode());
                    }
                }
                else {
                    errorStandards.add(standardC.getCode());
                }
            }
            catch (IndexOutOfBoundsException exception){
                if(characteristicStandards.size()>syllabusStandards.size()){
                    errorMes += "Обнаружены лишние" + (characteristicStandards.size()
                            -syllabusStandards.size()) + "проф. стандарты в характеристике\n";
                }
                else{
                    errorMes += "В характеристике нехватает"+ (syllabusStandards.size()
                            -characteristicStandards.size())+ "проф. стандарта/ов\n";
                }
                break;
            }

        }
        if(!errorLaborFunc.isEmpty()){
            errorMes+="Обобщенная трудовая функция в "+String.join(", ", errorLaborFunc)+ " не совпадают с учебным планом. Возможно код трудовой функции написан кириллицей";
        }
        if(!errorStandards.isEmpty()){
            errorMes+="Наименование или код проф. стандартов в "+String.join(", ", errorStandards)+ " не совпадают с учебным планом.";
        }
        if(!errorSubFunc.isEmpty()){
            errorMes+="Трудовые функции в "+String.join(", ", errorSubFunc)+ " не совпадают с учебным планом.  Возможно код трудовых функций написан кириллицей";
        }
        if(!errorMes.isBlank()){
            return errorMes;
        }

        return null;
    }

    private String checkTaskTypesStandards(List<String> taskTypesSyllabus, List<Standard> standardsSyllabus,
                                           Map<String,List<Standard>> taskTypesStandardsCharacteristic){
        String errorMes = "";
        ArrayList<String> errorName = new ArrayList<>();
        ArrayList<String> errorStandards = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, List<Standard>> entry : taskTypesStandardsCharacteristic.entrySet()) {
            String taskType = entry.getKey();
            List<Standard> standards = entry.getValue();
            try {
                String syllabusTaskType = taskTypesSyllabus.get(i).trim().toLowerCase();
                String characteristicTaskType = taskType.trim().toLowerCase();
                if (syllabusTaskType.equals(characteristicTaskType)) {
                    if (!new HashSet<>(standards).containsAll(standardsSyllabus)) {
                        errorStandards.add(taskType);
                    }
                }
                else{
                    errorName.add(taskType);
                }
            }
            catch (IndexOutOfBoundsException exception){
                break;
            }
            i++;
        }
        if(!errorStandards.isEmpty()){
            errorMes+="Типы задач ("+String.join(", ", errorStandards)+") не содержут проф. стандарты указанные в учебном плане.";
        }
        if(!errorName.isEmpty()){
            errorMes+="Наименование типов задач ("+String.join(", ", errorName)+ ") не совпадают с учебным планом.";
        }
        if(!errorMes.isBlank()){
            return errorMes;
        }

        return null;
    }

    private String checkTaskTypesCompetences(List<String> taskTypesSyllabus, List<Competence> competencesSyllabus,
                                             Map<String,List<Competence>> taskTypesCompetencesCharacteristic){
        String errorMes = "";
        ArrayList<String> errorName = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, List<Competence>> entry : taskTypesCompetencesCharacteristic.entrySet()) {
            String taskType = entry.getKey();
            List<Competence> competences = entry.getValue();
            try {
                String syllabusTaskType = taskTypesSyllabus.get(i).trim().toLowerCase();
                String characteristicTaskType = taskType.trim().toLowerCase();
                if (syllabusTaskType.equals(characteristicTaskType)) {
                    String errorComp = checkCharacteristicCompetences(competences, competencesSyllabus);
                    if(errorComp!=null){
                        errorMes+="Ошибка в компетенциях типа задач '"+taskType+"':\n"+errorComp;
                    }
                }
                else{
                    errorName.add(taskType);
                }
            }
            catch (IndexOutOfBoundsException exception){
                break;
            }
            i++;
        }
        if(!errorName.isEmpty()){
            errorMes+="Наименование типов задач ("+String.join(", ", errorName)+ ") не совпадают с учебным планом.";
        }
        if(!errorMes.isBlank()){
            return errorMes;
        }

        return null;
    }

    private String checkCharacteristicIndexes(Map<String,List<Competence>> taskTypesCompetences, List<Competence> pCompetences){
        String errorMes = "";
        for (Map.Entry<String, List<Competence>> entry : taskTypesCompetences.entrySet()) {
            String taskType = entry.getKey();
            List<Competence> competences = entry.getValue();
            for (int j = 0; j < competences.size(); j++) {
                Competence pCompetence = pCompetences.get(j);
                Competence competence = competences.get(j);
                if(!competence.getIds().equals(pCompetence.getIds())){
                    errorMes+="Ошибка соответствия индексов в компетенции "+ pCompetence.getIndex() +" типа задач '"+taskType+"'\n";
                }
            }

        }

        if(!errorMes.isBlank()){
            return errorMes;
        }

        return null;
    }
    private Integer getNumberFgos(String string){
        Pattern pattern = Pattern.compile("№\\s?(\\d+)");
        Matcher matcher = pattern.matcher(string);

        if (matcher.find()) {
            String result = matcher.group(1);
            return Integer.valueOf(result);
        }
        return 0;
    }
    private List<String> prettyListString(List<String> list){
        return list.stream().map(el->el.trim().toLowerCase()).sorted().toList();
    }
}
