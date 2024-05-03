package opopproto.util;


import lombok.Data;
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

    private File characteristics;

    private File syllabus;

    private Map<String, File> fos = new HashMap<>();

    private Map<String, File> rpd = new HashMap<>();

}
