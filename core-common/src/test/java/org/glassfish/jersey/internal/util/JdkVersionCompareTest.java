package org.glassfish.jersey.internal.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JdkVersionCompareTest {

  @Test
  public void testCompareJava8WithJava9() {
    JdkVersion java8 = JdkVersion.parseVersion("1.8.0_141");
    JdkVersion java9 = JdkVersion.parseVersion("9");

    assertEquals(1, java9.compareTo(java8));
    assertEquals(-1, java8.compareTo(java9));
  }

  @Test
  public void testCompareJava8Updates() {
    JdkVersion java8u141 = JdkVersion.parseVersion("1.8.0_141");
    JdkVersion java8u152 = JdkVersion.parseVersion("1.8.0_152");

    assertEquals(1, java8u152.compareTo(java8u141));
    assertEquals(-1, java8u141.compareTo(java8u152));
  }

  @Test
  public void testCompareJava9Versions() {
    JdkVersion java900 = JdkVersion.parseVersion("9.0.0");
    JdkVersion java901 = JdkVersion.parseVersion("9.0.1");

    assertEquals(1, java901.compareTo(java900));
    assertEquals(-1, java900.compareTo(java901));
  }
}
