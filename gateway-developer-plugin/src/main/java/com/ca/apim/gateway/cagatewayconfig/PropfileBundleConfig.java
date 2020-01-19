package com.ca.apim.gateway.cagatewayconfig;

public class PropfileBundleConfig {
    String bundleDirPath = "bundle";
    String configDirPath = "config";
    String templatizedDirPath = "templatized";
	public String getBundleDirPath() {
		return bundleDirPath;
	}
	public void setBundleDirPath(String bundleDirPath) {
		this.bundleDirPath = bundleDirPath;
	}
	public String getConfigDirPath() {
		return configDirPath;
	}
	public void setConfigDirPath(String configDirPath) {
		this.configDirPath = configDirPath;
	}
	public String getTemplatizedDirPath() {
		return templatizedDirPath;
	}
	public void setTemplatizedDirPath(String templatizedDirPath) {
		this.templatizedDirPath = templatizedDirPath;
	}
	@Override
	public String toString() {
		return "PropfileBundleConfig [bundleDirPath=" + bundleDirPath + ", configDirPath=" + configDirPath
				+ ", templatizedDirPath=" + templatizedDirPath + "]";
	}
}
