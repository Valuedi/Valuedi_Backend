package org.umc.valuedi.global.external.fss.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.global.external.fss.service.FssService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/external/fss")
@RequiredArgsConstructor
public class FssTestController {

    private final FssService fssService;

    @GetMapping("/savings")
    public Mono<String> testCall(@RequestParam(defaultValue = "020000") String topFinGrpNo) {
        return fssService.fetchRawData(topFinGrpNo);
    }

}
