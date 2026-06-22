package com.bibliotheque.bibliotheque.controllers;

import com.bibliotheque.bibliotheque.dtos.InscriptionRequest;
import com.bibliotheque.bibliotheque.services.InscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @PostMapping("/inscription")
    public ResponseEntity<String> inscription(@RequestBody InscriptionRequest inscriptionRequest){
        inscriptionService.inscrire(inscriptionRequest);
        return ResponseEntity.ok("inscription reussie avec succes!!");


    }
}
