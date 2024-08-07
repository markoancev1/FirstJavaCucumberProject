package org.example.runners;

import org.example.utils.tests.FileReaderUtilTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({FileReaderUtilTest.class})
public class UnitTestsSuite {
}