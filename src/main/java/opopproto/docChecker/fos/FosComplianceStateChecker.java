package opopproto.docChecker.fos;

import opopproto.data.fos.Evaluate;
import opopproto.data.fos.FosData;
import opopproto.data.rpd.EvaluateCompetences;
import opopproto.data.rpd.RpdData;
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
public class FosComplianceStateChecker {
    @Autowired
    private Documents documents;
    public List<String> check(List<FosData> fosDataList, List<RpdData> rpdDataList ){
        List<String> fosErrors = new ArrayList<>();
        String selfFosErrors = checkSelf(fosDataList);
        String fosRpdErrors = checkToRpd(fosDataList, rpdDataList);
        String fosTitleErrors = checkTitle(fosDataList);

        if(selfFosErrors!=null)
            fosErrors.add(selfFosErrors);
        if(fosRpdErrors!=null)
            fosErrors.add(fosRpdErrors);
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
                appendixesErrors.add("В документе " + fos.getFosName()+", Приложения :"+ String.join(", ",appendixErrors));
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
                    .map(EvaluateCompetences::getIndexCompetence).toList().size();

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
                                    if(indexCompetence.split(" ").length!=2){
                                        throw new IllegalArgumentException();
                                    }
                                    boolean idEquals = evaluateCompetence.getIndexCompetence().split(" ")[0].equals(indexCompetence.split(" ")[0]);
                                    boolean compEquals = evaluateCompetence.getCompetenceIndex().equals(indexCompetence.split(" ")[1]);
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
            return "<b>Ошибки в таблице оценочных средств</b>.<br><br>"+ String.join("<br><br>",errors);
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
