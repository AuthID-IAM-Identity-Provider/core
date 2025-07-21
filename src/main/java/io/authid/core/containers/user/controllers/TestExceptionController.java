package io.authid.core.containers.user.controllers;

import io.authid.core.shared.components.exception.GlobalTranslatableException;
import io.authid.core.shared.components.i18n.extractors.I18n;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestExceptionController {
    @GetMapping("/user-not-found/{userId}")
    public ResponseEntity<Void> testUserNotFoundException(@PathVariable String userId) {
        log.info("Testing user not found exception...");
        log.info("Testing user not found exception... test");
        I18n.setSourceClass(TestExceptionController.class);
        String key = I18n.extract("user.not.found");
        throw new GlobalTranslatableException(key, new Object[]{userId});
    }
}
