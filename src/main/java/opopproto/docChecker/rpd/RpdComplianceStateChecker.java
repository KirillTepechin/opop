package opopproto.docChecker.rpd;

import opopproto.domain.Discipline;
import opopproto.util.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RpdComplianceStateChecker {
    @Autowired
    private Documents documents;
    public void removeB3(Map<String, File> rpd){
        List<String> keysToRemove = new ArrayList<>();
        for (var f :rpd.entrySet()) {
            if(f.getKey().startsWith("Б3")){
                keysToRemove.add(f.getKey());
            }
        }
        keysToRemove.forEach(key->documents.getRpd().remove(key));
    }
    public String checkNaming(Map<String, File> rpd, List<Discipline> syllabusDisciplines){
        String errorMes="";
        ArrayList<String> errorsIndDeficit = new ArrayList<>();
        Map<String, File> clearRpd = new HashMap<>();
        for (var disc: syllabusDisciplines) {
            String name = disc.getIndex()+" "+disc.getName()+".docx";
            if(!rpd.containsKey(name)){
                errorsIndDeficit.add(disc.getIndex());
            }
            else{
                clearRpd.put(name, rpd.get(name));
            }
        }
        if(!errorsIndDeficit.isEmpty()){
            errorMes+="В РПД пакете нехватает дисциплин с индексами ("+String.join(", ", errorsIndDeficit)+")\n";
            errorMes+="Проверьте индексы и именование дисциплин в пакете с РПД документами.\n";
        }
        if(!rpd.isEmpty()){
            documents.setRpd(clearRpd);
//            errorMes+="В РПД пакете обнаружены лишние дисциплины с индексами ("+String.join(", ",
//                    errorsIndRedundant.stream().map(key->key.split(" ")[0]).toList())+")";
        }
        if(!errorMes.isBlank()){
            return errorMes;
        }
        return null;
    }
}
