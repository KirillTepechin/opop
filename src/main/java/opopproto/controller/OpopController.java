package opopproto.controller;

import opopproto.docChecker.ErrorState;
import opopproto.service.OpopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/opop")
public class OpopController {
    @Autowired
    private OpopService opopService;

    @PostMapping("/check-documents")
    public ErrorState checkDocuments(@RequestParam("file") MultipartFile file) throws Exception {
        return opopService.upload(file);
    }

    @PostMapping(value = "/generate", produces="application/zip")
    public byte[] generateDocuments(@RequestParam("file") MultipartFile file, Boolean charCheckbox,
                                    Boolean rpdCheckbox, Boolean fosCheckbox) throws Exception {
        return opopService.generate(file, charCheckbox, rpdCheckbox, fosCheckbox);
    }
}
