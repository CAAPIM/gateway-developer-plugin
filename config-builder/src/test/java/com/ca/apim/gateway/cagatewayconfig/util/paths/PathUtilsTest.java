/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.paths;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathUtilsTest {

    @Test
    void unixPath() {
        assertEquals("test", PathUtils.unixPath("test"));
        assertEquals("test/path", PathUtils.unixPath("test", "path"));
        assertEquals("test/path", PathUtils.unixPath("test\\path"));
        assertEquals("test/path/subpath", PathUtils.unixPath("test", "path", "subpath"));
        assertEquals("test/path/subpath", PathUtils.unixPath("test\\path\\subpath"));
    }

    @Test
    void unixPathFromPathObject() {
        assertEquals("test", PathUtils.unixPath(Paths.get("test")));
        assertEquals("test/path", PathUtils.unixPath(Paths.get("test", "path")));
        assertEquals("test/path/subpath", PathUtils.unixPath(Paths.get("test", "path", "subpath")));
    }

    @Test
    void unixPathEndingWithSeparator() {
        assertEquals("test/", PathUtils.unixPathEndingWithSeparator("test"));
        assertEquals("test/path/", PathUtils.unixPathEndingWithSeparator("test", "path"));
        assertEquals("test/path/", PathUtils.unixPathEndingWithSeparator("test\\path"));
        assertEquals("test/path/subpath/", PathUtils.unixPathEndingWithSeparator("test", "path", "subpath"));
        assertEquals("test/path/subpath/", PathUtils.unixPathEndingWithSeparator("test\\path\\subpath"));
    }

    @Test
    void unixPathEndingWithSeparatorFromPathObject() {
        assertEquals("test/", PathUtils.unixPathEndingWithSeparator(Paths.get("test")));
        assertEquals("test/path/", PathUtils.unixPathEndingWithSeparator(Paths.get("test", "path")));
        assertEquals("test/path/subpath/", PathUtils.unixPathEndingWithSeparator(Paths.get("test", "path", "subpath")));
    }

    @Test
    void testExtractName() {
        assertEquals("test", PathUtils.extractName("test"));
        assertEquals("test", PathUtils.extractName("path/test"));
        assertEquals("test", PathUtils.extractName("/path/subpath/test"));
    }

    @Test
    void testExtractPath() {
        assertEquals("", PathUtils.extractPath("test"));
        assertEquals("path/", PathUtils.extractPath("path/test"));
        assertEquals("/path/subpath/", PathUtils.extractPath("/path/subpath/test"));
    }
}