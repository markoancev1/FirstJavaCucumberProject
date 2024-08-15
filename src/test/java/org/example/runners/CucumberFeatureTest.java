package org.example.runners;

import org.junit.platform.suite.api.*;
import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "org.example.hooks,org.example.steps")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "json:target/cucumber/cucumber.json")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME, value = "src/resources/features/home.feature")
@ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "true")
@ConfigurationParameter(key = "cucumber.execution.parallel.config.strategy", value = "fixed")
@ConfigurationParameter(key = "cucumber.execution.parallel.config.fixed.parallelism", value = "4")
public class CucumberFeatureTest {
}