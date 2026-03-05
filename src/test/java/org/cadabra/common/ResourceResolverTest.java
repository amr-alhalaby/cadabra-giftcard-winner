package org.cadabra.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceResolverTest {

    @Test
    void shouldReturnClassPathResourceForRelativePath() {
        var resource = ResourceResolver.resolve("data/purchases.csv");
        assertThat(resource).isInstanceOf(ClassPathResource.class);
    }

    @Test
    void shouldReturnFileSystemResourceForAbsolutePath() {
        var resource = ResourceResolver.resolve("/tmp/purchases.csv");
        assertThat(resource).isInstanceOf(FileSystemResource.class);
    }

    @Test
    void shouldReturnFileSystemResourceForRelativePathWithDotSlash() {
        var resource = ResourceResolver.resolve("./data/purchases.csv");
        assertThat(resource).isInstanceOf(FileSystemResource.class);
    }

    @Test
    void shouldReturnFileSystemResourceForWindowsPath() {
        var resource = ResourceResolver.resolve("C:/data/purchases.csv");
        assertThat(resource).isInstanceOf(FileSystemResource.class);
    }
}

