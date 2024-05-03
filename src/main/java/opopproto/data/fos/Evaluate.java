package opopproto.data.fos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Evaluate {
    String name;
    EvaluateType evaluateType;
    List<String> indexesCompetences = new ArrayList<>();
}
