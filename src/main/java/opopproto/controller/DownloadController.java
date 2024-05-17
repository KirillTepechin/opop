package opopproto.controller;

import opopproto.model.Head;
import opopproto.repository.InspectionResultRepository;
import opopproto.util.Documents;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/download")
public class DownloadController {
    @Autowired
    private Documents documents;
    @GetMapping(value = "/gen", produces = "application/zip")
    public byte[] downloadGeneration(@RequestParam UUID uuid, @AuthenticationPrincipal Head head){
        try(InputStream in = new FileInputStream(documents.getZIP_GEN_PATH()+ "/" +
                head.getLogin() + "/" + uuid.toString() + ".zip")) {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/ins", produces = "application/zip")
    public byte[] downloadInspection(@RequestParam UUID uuid, @AuthenticationPrincipal Head head){
        try(InputStream in = new FileInputStream(documents.getZIP_INS_PATH()+ "/" +
                head.getLogin() + "/" +uuid.toString()+".zip")) {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
