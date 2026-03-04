package org.example.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class ResourceResolver {

    private ResourceResolver() {}

    public static Resource resolve(String path) {
        return isExternalPath(path)
                ? new FileSystemResource(path)
                : new ClassPathResource(path);
    }

    private static boolean isExternalPath(String path) {
        return path.startsWith("/") || path.startsWith("./") || path.contains(":");
    }
}

