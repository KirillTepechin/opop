package opopproto.data.rpd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import opopproto.data.fos.Evaluate;
import opopproto.data.fos.EvaluateType;

@Data
@AllArgsConstructor
public class EvaluateCompetences {
    private String competenceIndex;
    private String indexCompetence;
    private String evaluateName;

    public boolean containsEvaluate(EvaluateType evaluateType){
        String string = evaluateName.toLowerCase();
        if(EvaluateType.EKZAMEN.equals(evaluateType)){
            return string.contains("экзамен");
        }
        if(EvaluateType.TEST.equals(evaluateType)){
            return string.contains("тест");
        }
        if(EvaluateType.ZACHET.equals(evaluateType)){
           return string.contains("зачет") || string.contains("зачёт");
        }
        if(EvaluateType.LAB.equals(evaluateType)){
            return string.contains("лабор");
        }
        if(EvaluateType.SOBES.equals(evaluateType)){
            return string.contains("собес");
        }
        if(EvaluateType.KURS.equals(evaluateType)){
            return string.contains("курсов");
        }
        if(EvaluateType.PRACTICE.equals(evaluateType)){
            return string.contains("практ");
        }
        if(EvaluateType.REPORT.equals(evaluateType)){
            return string.contains("отчёт") || string.contains("отчет");
        }
        if(EvaluateType.REFERAT.equals(evaluateType)){
            return string.contains("реферат");
        }
        else {
            return false;
        }
    }
}
