package opopproto.controller;

import opopproto.docChecker.ErrorState;
import opopproto.service.UploadService;
import opopproto.docChecker.ValidationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload-documents")
public class OpopUploadRestController {
    @Autowired
    private UploadService uploadService;

    @PostMapping
    public ErrorState upload(@RequestParam("file") MultipartFile file) throws Exception {
        return uploadService.upload(file);
    }
}
