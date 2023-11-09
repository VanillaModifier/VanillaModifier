package org.vanillamodifier.plugin;

import org.vanillamodifier.interfaces.IVMLoadingPlugin;
import org.vanillamodifier.struct.PluginData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModLoader {
	private final HashMap<File, IVMLoadingPlugin> map = new HashMap<>();
	private final List<ClassLoader> classLoaderList = new ArrayList<>();

	public Class<?> getClassInLoader(String name){
		for(ClassLoader loader : classLoaderList){
			try {
				return Class.forName(name, true, loader);
			} catch (ClassNotFoundException e) {
			}
		}
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public IVMLoadingPlugin load(File file) {
		if (!(file.getName().endsWith(".jar"))) throw new RuntimeException("File have to be a Jar! " + file.getName());
		try {
			if(map.containsKey(file)) {
				throw new RuntimeException(file.getName() + " " + "Plugin already loaded.");
			}
			PluginData dataFile = new InfoParser(file).getObject();
			ClassLoader loader = URLClassLoader.newInstance( new URL[] { file.toURI().toURL() }, getClass().getClassLoader() );
			classLoaderList.add(loader);
			Class<?> clazz = Class.forName(dataFile.getMain(), true, loader);
			Class<? extends IVMLoadingPlugin> instanceClass = clazz.asSubclass(IVMLoadingPlugin.class);
			Constructor<? extends IVMLoadingPlugin> instanceClassConstructor = instanceClass.getConstructor();
			IVMLoadingPlugin plugin = instanceClassConstructor.newInstance();
			plugin.setDescriptionFile(dataFile);
			map.put(file, plugin);
			plugin.onLoad();
			return plugin;
		}
		catch(MalformedURLException e) {
			throw new RuntimeException("Failed to convert the file path to a URL.", e);
		}
		catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Failed to create a new instance of the mod.", e);
		}
	}


	public IVMLoadingPlugin update(File file) {
		if (!(file.getName().endsWith(".jar"))) throw new RuntimeException("File have to be a Jar! " + file.getName());
		try {

			if(!map.containsKey(file)) {
				throw new RuntimeException(file.getName() + " " + "mod isn´t loaded!");
			}
			PluginData dataFile = new InfoParser(file).getObject();
			ClassLoader loader = URLClassLoader.newInstance( new URL[] { file.toURI().toURL() }, getClass().getClassLoader() );
			Class<?> clazz = Class.forName(dataFile.getMain(), true, loader);
			Class<? extends IVMLoadingPlugin> instanceClass = clazz.asSubclass(IVMLoadingPlugin.class);
			Constructor<? extends IVMLoadingPlugin> instanceClassConstructor = instanceClass.getConstructor();
			IVMLoadingPlugin mod = instanceClassConstructor.newInstance();
			mod.setDescriptionFile(dataFile);
			return mod;
		}
		catch(MalformedURLException e) {
			throw new RuntimeException("Failed to convert the file path to a URL.", e);
		}
		catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Failed to create a new instance of the mod.", e);
		}
	}

	public IVMLoadingPlugin loadFromURL(final String url, final File filePath) {
		InputStream in;
		try {
			in = new URL(url).openStream();
			Files.copy(in, Paths.get(filePath.getPath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = Paths.get(filePath.getPath()).toFile();
		if (!(file.getName().endsWith(".jar"))) throw new RuntimeException("File have to be a Jar! " + file.getName());
		try {
			if(map.containsKey(file)) {
				throw new RuntimeException(file.getName() + " " + "mod already loaded.");
			}
			PluginData dataFile = new InfoParser(file).getObject();
			ClassLoader loader = URLClassLoader.newInstance( new URL[] { file.toURI().toURL() }, getClass().getClassLoader() );
			Class<?> clazz = Class.forName(dataFile.getMain(), true, loader);
			Class<? extends IVMLoadingPlugin> instanceClass = clazz.asSubclass(IVMLoadingPlugin.class);
			Constructor<? extends IVMLoadingPlugin> instanceClassConstructor = instanceClass.getConstructor();
			IVMLoadingPlugin mod = instanceClassConstructor.newInstance();
			mod.setDescriptionFile(dataFile);
			map.put(file, mod);
			mod.onLoad();
			return mod;
		}
		catch(MalformedURLException e) {
			throw new RuntimeException("Failed to convert the file path to a URL.", e);
		}
		catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Failed to create a new instance of the mod.", e);
		}
	}

	public IVMLoadingPlugin unload(File file) {
		if (!(file.getName().endsWith(".jar"))) throw new RuntimeException("File have to be a Jar! " + file.getName());
		if(!map.containsKey(file)) {
			throw new RuntimeException("Can't unload a mod that wasn't loaded in the first place.");
		}
		IVMLoadingPlugin mod = map.get(file);
		mod.onUnload();
		map.remove(file);
		return mod;
	}

	public void reload(File file) {
		unload(file);
		load(file);
	}
}
