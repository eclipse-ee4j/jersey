package org.glassfish.jersey.internal.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class JdkVersionParseTest {

  @Parameterized.Parameter
  public String rawVersionString;
  @Parameterized.Parameter(1)
  public int expectedMajorVersion;
  @Parameterized.Parameter(2)
  public int expectedMinorVersion;
  @Parameterized.Parameter(3)
  public int expectedMaintenanceVersion;
  @Parameterized.Parameter(4)
  public int expectedUpdateVersion;

  @Parameterized.Parameters
  public static Collection<Object[]> provideVersions() {
    return Arrays.asList(new Object[][]{
        // Java 8
        {"1.8.0_141-b15", 1, 8, 0, 141},
        {"1.8.0_141", 1, 8, 0, 141},

        // Java 9 and above
        {"9", 9, 0, 0, 0},
        {"9.0.3", 9, 0, 3, 0},
        {"11", 11, 0, 0, 0},

        // malformed version
        {"invalid version", -1, -1, -1, -1}
    });
  }

  @Test
  public void testParseVersion() {
    JdkVersion version = JdkVersion.parseVersion(rawVersionString);

    assertEquals(expectedMajorVersion, version.getMajor());
    assertEquals(expectedMinorVersion, version.getMinor());
    assertEquals(expectedMaintenanceVersion, version.getMaintenance());
    assertEquals(expectedUpdateVersion, version.getUpdate());
  }
}
