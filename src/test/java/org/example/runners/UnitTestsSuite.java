package org.example.runners;

import org.example.utils.tests.FileReaderUtilTest;
import org.example.utils.tests.RestApiLoadUtilsTest;
import org.example.utils.tests.RestApiUtilsTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({FileReaderUtilTest.class, RestApiLoadUtilsTest.class, RestApiUtilsTest.class})
public class UnitTestsSuite {
}