package org.vanillamodifier.struct;


public class PluginData {

	private final String main;
	private final String name;
	private final String version;

	public PluginData(String main, String name, String version) {
		this.main = main;
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public String getMain() {
		return main;
	}

	public String getVersion() {
		return version;
	}
}
