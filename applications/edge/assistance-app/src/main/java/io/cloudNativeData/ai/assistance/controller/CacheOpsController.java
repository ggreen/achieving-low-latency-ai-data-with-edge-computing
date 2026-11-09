package io.cloudNativeData.ai.assistance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.cloudNativeData.ai.assistance.services.search.ClearCacheService;

@RestController
@RequestMapping("cache/ops")
@RequiredArgsConstructor
public class CacheOpsController {

    private final ClearCacheService cacheService;

    @DeleteMapping
    public void clearCache() {

        cacheService.clearCache();
    }
}
