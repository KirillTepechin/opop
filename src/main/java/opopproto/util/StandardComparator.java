package opopproto.util;

import opopproto.domain.Standard;
import org.springframework.stereotype.Component;

import java.util.Comparator;
@Component
public class StandardComparator implements Comparator<Standard> {
    @Override
    public int compare(Standard o1, Standard o2) {
        return o1.getCode().compareTo(o2.getCode());
    }
}
