package org.vanillamodifier.interfaces;

import org.vanillamodifier.struct.PluginData;

/**
 * 插件主类
 */
public interface IVMLoadingPlugin {

	ThreadLocal<PluginData> descriptionFile = new ThreadLocal<>();
	/**
	 * onLoad方法，加载时调用
	 */
	abstract void onLoad();

	/**
	 * unLoad方法，卸载时调用
	 */
	abstract void onUnload();

	default PluginData getDescriptionFile() {
		return descriptionFile.get();
	}

	default void setDescriptionFile(PluginData descriptionFile) {
		if(this.descriptionFile.get() != null) {
			throw new RuntimeException("Can't set the description file. Its already set!");
		}
		this.descriptionFile.set(descriptionFile);
	}

}