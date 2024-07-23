package org.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:src/test/java//org/example/report/cucumber-reports.html"},
        features = "src/resources/features",
        glue = {"org.example.steps"}
)
public class TestRunner {

}