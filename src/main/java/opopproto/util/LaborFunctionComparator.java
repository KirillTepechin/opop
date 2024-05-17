package opopproto.util;

import opopproto.domain.LaborFunction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
@Component
public class LaborFunctionComparator implements Comparator<LaborFunction> {
    @Override
    public int compare(LaborFunction o1, LaborFunction o2) {
        String[] s1Parts = o1.getCode().split("/");
        String[] s2Parts = o2.getCode().split("/");

        String s1Prefix = s1Parts[0];
        String s2Prefix = s2Parts[0];

        if (!s1Prefix.equals(s2Prefix)) {
            return s1Prefix.compareTo(s2Prefix);
        } else {
            double s1Value = Double.parseDouble(s1Parts[1]);
            double s2Value = Double.parseDouble(s2Parts[1]);

            return Double.compare(s1Value, s2Value);
        }
    }
}
