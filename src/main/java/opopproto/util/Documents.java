package opopproto.util;


import lombok.Data;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope(value =
        org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST,
        proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class Documents {
    @Getter
    private final String TMP_PATH = "src/main/resources/tmp";
    @Getter
    private final String DOCS_PATH = "src/main/resources/opopDocs";
    @Getter
    private final String DOCS_GEN_PATH = "src/main/resources/generateDocs";
    @Getter
    private final String ZIP_GEN_PATH = "src/main/resources/generateZip";
    @Getter
    private final String ZIP_INS_PATH = "src/main/resources/inspectionZip";
    @Getter
    private final String RPD_TEMPLATE_PATH = "src/main/resources/docTemplates/МакетРпд.docx";
    @Getter
    private final String RPD_PRACTICE_TEMPLATE_PATH = "src/main/resources/docTemplates/МакетРпдПрактика.docx";
    @Getter
    private final String CHARACTERISTIC_TEMPLATE_PATH = "src/main/resources/docTemplates/МакетХарактеристики.docx";

    private File characteristics;

    private File syllabus;

    private Map<String, File> fos = new HashMap<>();

    private Map<String, File> rpd = new HashMap<>();

}
