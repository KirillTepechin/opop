package opopproto.data.rpd;

import lombok.Data;

@Data
public class RpdTitle {
    private String titleEqualsSpeciality;
    private String titleEqualsDiscipline;
    private String titleEqualsProfile;
    private String titleEqualsQualification;
    private String titleEqualsLevel;

    public void setTitleEqualsSpeciality(String titleEqualsSpeciality){
        this.titleEqualsSpeciality = titleEqualsSpeciality.replaceAll("[«»]", "");
    }
}
