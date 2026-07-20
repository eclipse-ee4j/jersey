package org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.json;

import tools.jackson.core.Version;
import tools.jackson.core.Versioned;
import tools.jackson.core.util.VersionUtil;

/**
 * Automatically generated from PackageVersion.java.in during
 * packageVersion-generate execution of maven-replacer-plugin in
 * pom.xml.
 */
public final class PackageVersion implements Versioned {
    public static final Version VERSION = VersionUtil.parseVersion(
        "3.1.1", "tools.jackson.jakarta.rs", "jackson-jakarta-rs-json-provider");

    @Override
    public Version version() {
        return VERSION;
    }
}
