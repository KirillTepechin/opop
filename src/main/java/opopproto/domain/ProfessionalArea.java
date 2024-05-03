package opopproto.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProfessionalArea {
    private String code;
    private String name;
    private List<Standard> standards = new ArrayList<>();
}
