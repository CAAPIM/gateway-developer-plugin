package com.ca.apim.gateway.cagatewayconfig;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class BuildPropfileBundleTask extends DefaultTask {
	@TaskAction
	public void perform () {
		System.out.println("build propfile bundle: perform()");
	}
}
