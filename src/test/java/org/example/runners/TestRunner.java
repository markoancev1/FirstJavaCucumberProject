package org.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"json:target/cucumber/cucumber.json"},
        features = "src/resources/features",
        glue = {"org.example.steps"}
)
public class TestRunner {

}