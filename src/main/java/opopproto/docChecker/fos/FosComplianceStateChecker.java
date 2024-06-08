package opopproto.docChecker.fos;

import lombok.RequiredArgsConstructor;
import opopproto.data.characteristic.CharacteristicData;
import opopproto.data.fos.Evaluate;
import opopproto.data.fos.FosData;
import opopproto.data.rpd.EvaluateCompetences;
import opopproto.data.rpd.RpdData;
import opopproto.data.syllabus.SyllabusData;
import opopproto.domain.Competence;
import opopproto.domain.Discipline;
import opopproto.util.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class FosComplianceStateChecker {
    private final Documents documents;
    public List<String> check(List<FosData> fosDataList, List<RpdData> rpdDataList,
                              SyllabusData syllabusData, CharacteristicData characteristicData){
        List<String> fosErrors = new ArrayList<>();
        String selfFosErrors = checkSelf(fosDataList);
        String fosEvaluateRpdErrors = checkToRpd(fosDataList, rpdDataList);
        String fosEvaluateOverallErrors = checkToSyllabusAndCharacteristic(fosDataList, syllabusData, characteristicData);
        String fosTitleErrors = checkTitle(fosDataList);
        if(selfFosErrors!=null)
            fosErrors.add(selfFosErrors);
        if(fosEvaluateRpdErrors!=null)
            fosErrors.add(fosEvaluateRpdErrors);
        if(fosEvaluateOverallErrors!=null)
            fosErrors.add(fosEvaluateOverallErrors);
        if(fosTitleErrors!=null)
            fosErrors.add(fosTitleErrors);


        return fosErrors;
    }
    private String checkSelf(List<FosData> fosData){
        ArrayList<String> appendixesErrors = new ArrayList<>();
        for (var fos:fosData) {
            ArrayList<String> appendixErrors = new ArrayList<>();
            for (var appendix: fos.getAppendixesExisting().entrySet()) {
                if(appendix.getValue().equals(false)){
                    appendixErrors.add("'"+appendix.getKey()+"'");
                }
            }
            if(!appendixErrors.isEmpty()){
                appendixesErrors.add("В документе " + fos.getFosName()+", Приложения : "+ String.join(", ",appendixErrors));
            }
        }
        if(!appendixesErrors.isEmpty()){
            return "<b>Отсутствующие приложения в ФОС документах:</b><br><br>"+ String.join("<br><br>",appendixesErrors);
        }
        return null;
    }
    private String checkTitle(List<FosData> fosDataList ){
        ArrayList<String> titleErrors = new ArrayList<>();
        for (var fosData: fosDataList){
            String disciplineName = fosData.getFosName().substring(fosData.getFosName().indexOf(" ") + 1);

            if(isPractice(fosData.getFosName())){
                String correctTitle;
                if(isEduPractice(fosData.getFosName())){
                    correctTitle = "Учебная практика: " + disciplineName;
                }
                else {
                    correctTitle = "Производственная практика: " + disciplineName;
                }
                if(!correctTitle.equalsIgnoreCase(fosData.getTitleEqualsDiscipline())){
                    titleErrors.add(fosData.getFosName());
                }
            }
            else{
                if(!disciplineName.equals(fosData.getTitleEqualsDiscipline())){
                    titleErrors.add(fosData.getFosName());
                }
            }
        }
        if(!titleErrors.isEmpty()){
            return "<b>Неправильное название дисциплины в титульном листе в документах:</b><br><br>"+ String.join(", ",titleErrors);
        }
        return null;
    }
    private String checkToRpd(List<FosData> fosDataList, List<RpdData> rpdDataList){
        List<String> errors = new ArrayList<>();
        for (var fosData: fosDataList) {
            RpdData rpdData;
            try {
                rpdData = rpdDataList.stream()
                        .filter(rpd->rpd.getRpdName().equals(fosData.getFosName())).toList().get(0);
            }
            catch (IndexOutOfBoundsException e){
                errors.add("Не найден РПД документ для дисциплины '" + fosData.getFosName()+"'");
                continue;
            }

            int fosListSize = fosData.getEvaluates().stream()
                    .flatMap(evaluate -> evaluate.getIndexesCompetences().stream()).distinct().toList().size();

            int rpdListSize = rpdData.getEvaluateCompetences().stream()
                    .map(EvaluateCompetences::getIdCompetence).toList().size();

            if(fosListSize>rpdListSize){
                errors.add("Количество индикаторов в ФОС документе дисциплины '" + fosData.getFosName() +  "' больше количества индикаторов в РПД документе");
            }
            else if (fosListSize<rpdListSize){
                errors.add("Количество индикаторов в РПД документе дисциплины '" + fosData.getFosName() +  "'  больше количества индикаторов в ФОС документе");
            }

            List<Evaluate> fosEvaluates = fosData.getEvaluates();
            List<EvaluateCompetences> evaluateCompetencesRpd = rpdData.getEvaluateCompetences();
            for (var evaluate: fosEvaluates) {
                for (var indexCompetence: evaluate.getIndexesCompetences()) {
                    try {
                        evaluateCompetencesRpd.stream()
                                .filter(evaluateCompetence -> {
                                    Pattern patternId = Pattern.compile("(ИД\\s*-\\s*\\d)");
                                    Matcher matcherFos = patternId.matcher(indexCompetence);
                                    String idFos = "";
                                    String compFos = "";
                                    if(matcherFos.find()){
                                        idFos = matcherFos.group(1).trim();
                                        compFos = indexCompetence.substring(idFos.length()).trim();
                                    }
                                    Matcher matcherRpd = patternId.matcher(evaluateCompetence.getIdCompetence());
                                    String idRpd = "";
                                    String compRpd = evaluateCompetence.getCompetenceIndex();
                                    if(matcherRpd.find()){
                                        idRpd = matcherRpd.group(1).trim();
                                    }
                                    boolean idEquals = idRpd.replaceAll(" ", "").equalsIgnoreCase(idFos.replaceAll(" ", ""));
                                    boolean compEquals = compRpd.replaceAll(" ", "").equalsIgnoreCase(compFos.replaceAll(" ", ""));
                                    boolean typeEquals = evaluateCompetence.containsEvaluate(evaluate.getEvaluateType());
                                    return idEquals && compEquals && typeEquals;

                                }).toList().get(0);

                    }
                    catch (IllegalArgumentException e) {
                        errors.add("ФОС документ '" + rpdData.getRpdName() + "' содержит невалидный индикатор '" +
                                indexCompetence + "' для оценочного средства '" + evaluate.getName() + "'");
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        errors.add("В РПД документе '" + rpdData.getRpdName() + "' не найден индикатор '" +
                                indexCompetence + "' для оценочного средства '" + evaluate.getName() + "'");
                    }

                }
            }
        }

        if(!errors.isEmpty()){
            return "<b>Ошибки в таблице оценочных средств (Несоответствия с РПД)</b>.<br><br>"+ String.join("<br><br>",errors);
        }
        return null;
    }
    private String checkToSyllabusAndCharacteristic(List<FosData> fosDataList, SyllabusData syllabusData,
                                                    CharacteristicData characteristicData){
        List<String> evaluatesErrors = new ArrayList<>();
        for (var fosData:fosDataList) {
            List<Competence> syllabusDisciplineCompetences = syllabusData.getDisciplinesData().getAllDisciplines().stream()
                    .filter(discipline -> (discipline.getIndex() + " " + discipline.getName())
                            .equals(fosData.getFosName())).toList().get(0).getCompetences();

            if(fosData.getEvaluates() == null){
                evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() + "' отсутствуют оценочные средства");
                continue;
            }
            Pattern patternId = Pattern.compile("(ИД\\s*-\\s*\\d)");
            int fosEvalCompSize = fosData.getEvaluates().stream()
                    .flatMap(evaluate -> evaluate.getIndexesCompetences().stream())
                    .map(string -> {
                        Matcher matcher = patternId.matcher(string);
                        if(matcher.find()){
                            return string.substring(matcher.group(1).length()).trim().replaceAll(" ", "");
                        }
                        else {
                            return null;
                        }
                    }).distinct().toList().size();


            if(fosEvalCompSize > syllabusDisciplineCompetences.size()){
                evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() + "' количество компетенций больше чем в плане");
            }
            if(fosEvalCompSize < syllabusDisciplineCompetences.size()){
                evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() + "' количество компетенций меньше чем в плане");
            }

            for (var syllabusDisComp: syllabusDisciplineCompetences) {
                var fosDisCompetences = fosData.getEvaluates().stream()
                        .flatMap(evaluate -> evaluate.getIndexesCompetences().stream())
                        .filter(str -> {
                            Matcher matcher = patternId.matcher(str);
                            if(matcher.find()){
                                return str.substring(matcher.group(1).length()).trim().replaceAll(" ", "")
                                        .equalsIgnoreCase(syllabusDisComp.getIndex().toUpperCase().replaceAll(" ", ""));
                            }
                            return false;
                        }).distinct().toList();

                if(fosDisCompetences.isEmpty()){
                    evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() +
                            "' не найдена компетенция " + syllabusDisComp.getIndex());
                    continue;
                }
                Competence characteristicDisComp;
                try {
                    List<Competence> characteristicCompetences = new ArrayList<>();
                    characteristicCompetences.addAll(characteristicData.getTableData().getUCompetences());
                    characteristicCompetences.addAll(characteristicData.getTableData().getPCompetences());
                    characteristicCompetences.addAll(characteristicData.getTableData().getOpCompetences());

                    characteristicDisComp = characteristicCompetences.stream()
                            .filter(competence ->  {
                                Matcher matcher = patternId.matcher(fosDisCompetences.get(0));
                                if(matcher.find()){
                                    return fosDisCompetences.get(0).substring(matcher.group(1).length()).trim().replaceAll(" ", "")
                                            .equalsIgnoreCase(competence.getIndex().toUpperCase().replaceAll(" ", ""));
                                }
                                return false;
                            }).toList().get(0);
                }
                catch (IndexOutOfBoundsException e){
                    continue;
                }
                if(fosDisCompetences.size() > characteristicDisComp.getIds().size()){
                    evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() +
                            "' количество индикаторов компетенции "+ characteristicDisComp.getIndex() +
                            " больше чем в характеристике");
                }
                if(fosDisCompetences.size() < characteristicDisComp.getIds().size()){
                    evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() +
                            "' количество индикаторов компетенции "+ characteristicDisComp.getIndex() +
                            " меньше чем в характеристике");
                }

                for (var characteristicId: characteristicDisComp.getIds()) {
                    try {
                        fosDisCompetences.stream()
                                .filter(id -> id.startsWith(characteristicId.getIndex())).toList().get(0);
                    }
                    catch (IndexOutOfBoundsException e){
                        evaluatesErrors.add("В ФОС документе '" + fosData.getFosName() +
                                "' не найден индикатор компетенции " + characteristicDisComp.getIndex() +
                                " " + characteristicId.getIndex());
                    }
                }

            }
        }

        if(!evaluatesErrors.isEmpty()){
            return "<b>Ошибки в таблице оценочных средств (Несоответствия с планом и характеристикой)</b>.<br><br>"+ String.join("<br><br>",evaluatesErrors);
        }
        return null;
    }
    public String checkNaming(Map<String, File> fos, List<Discipline> syllabusDisciplines){
        String errorMes="";
        ArrayList<String> errorsIndDeficit = new ArrayList<>();

        Map<String, File> clearFos = new HashMap<>();
        for (var disc: syllabusDisciplines) {
            String name = disc.getIndex()+" "+disc.getName()+" ФОС.docx";
            if(!fos.containsKey(name)){
                if(!disc.isB3())
                    errorsIndDeficit.add(disc.getIndex());
            }
            else{
                clearFos.put(name, fos.get(name));
            }
        }
        if(!errorsIndDeficit.isEmpty()){
            errorMes+="<b>В ФОС пакете нехватает дисциплин</b> с индексами ("+String.join(", ", errorsIndDeficit)+")<br>";
            errorMes+="Проверьте индексы и именование дисциплин в пакете с ФОС документами.<br>";
        }
        if(!fos.isEmpty()){
            documents.setFos(clearFos);
//            errorMes+="В ФОС пакете обнаружены лишние дисциплины с индексами ("+String.join(", ",
//                    errorsIndRedundant.stream().map(key->key.split(" ")[0]).toList())+")";
        }
        if(!errorMes.isBlank()){
            return errorMes;
        }
        return null;
    }
    //Удаление Б3, с ним пока не работаем
    public void removeB3(Map<String, File> fos){
        List<String> keysToRemove = new ArrayList<>();
        for (var f :fos.entrySet()) {
            if(f.getKey().startsWith("Б3")){
                keysToRemove.add(f.getKey());
            }
        }
        keysToRemove.forEach(key->documents.getFos().remove(key));
    }

    private boolean isPractice(String disciplineName){
        Pattern pattern = Pattern.compile("\\(П\\)|\\(У\\)");
        Matcher matcher = pattern.matcher(disciplineName);

        return matcher.find();
    }
    private boolean isEduPractice(String disciplineName){
        Pattern pattern = Pattern.compile("\\(У\\)");
        Matcher matcher = pattern.matcher(disciplineName);

        return matcher.find();
    }

}
