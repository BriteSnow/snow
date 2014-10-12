package com.britesnow.snow.web.renderer.less;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.tools.shell.Global;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;


/**
 * Most of the code comes from https://github.com/asual/lesscss-engine
 *
 * If you plan to take or look at some lesscss rhino code,
 * look at the https://github.com/asual/lesscss-engine. This is just
 * a quick copy of some of the files. (planning to use lesscss-engine soon)
 *
 */

public class LessProcessor {

	private static final LessProcessor instance   = new LessProcessor();

	public static final FileFilter     fileFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			String pathname = file.getName();
			if (pathname.endsWith(".less")) {
				return true;
			} else {
				return false;
			}
		}

	};

	private Scriptable                 scope;
	private ClassLoader                classLoader;

	//private static final String        JS_ROOT    = "jchruncher/less/";
	private static final String JS_ROOT = "com/britesnow/snow/web/less/";

	// private static final String JS_ROOT = "META-INF/";
	private static final String        CHARSET    = "UTF-8";

	private Function compile;

	private ResourceLoader loader = new FilesystemResourceLoader();

	public LessProcessor() {
		try {
			// logger.debug("Initializing LESS Engine.");
			classLoader = getClass().getClassLoader();
			URL less = classLoader.getResource(JS_ROOT + "less.js");
			URL env = classLoader.getResource(JS_ROOT + "env.js");
			URL engine = classLoader.getResource(JS_ROOT + "engine.js");
			Context cx = Context.enter();
			// logger.debug("Using implementation version: " + cx.getImplementationVersion());
			cx.setOptimizationLevel(9);
			Global global = new Global();
			global.init(cx);
			scope = cx.initStandardObjects(global);
			cx.evaluateReader(scope, new InputStreamReader(env.openConnection().getInputStream()), env.getFile(), 1, null);
			cx.evaluateString(scope, "lessenv.charset = '" + CHARSET + "';", "charset", 1, null);
			cx.evaluateString(scope, "lessenv.css = " + "false" + ";", "css", 1, null);
			cx.evaluateReader(scope, new InputStreamReader(less.openConnection().getInputStream()), less.getFile(), 1, null);
			cx.evaluateReader(scope, new InputStreamReader(engine.openConnection().getInputStream()), engine.getFile(), 1, null);
			Scriptable lessEnv = (Scriptable) scope.get("lessenv", scope);
			lessEnv.put("loader", lessEnv, Context.javaToJS(loader, scope));
			//compileString = (Function) scope.get("compileString", scope);
			//compileFile = (Function) scope.get("compileFile", scope);
			compile = (Function) scope.get("compile", scope);
			Context.exit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot intialize LessProcessor:\n" + e.getMessage());
		}
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

	public static LessProcessor instance() {
		return instance;
	}

	public void process(List<File> sourceFiles, File dest, boolean asNeeded) {
		List<Item> items = new ArrayList<Item>();

		// --------- Build the Item List --------- //
		if (dest.isDirectory()) {
			for (File source : sourceFiles) {
				String baseName = Files.getNameWithoutExtension(source.getName());
				items.add(new Item(source, new File(dest, baseName + ".css")));
			}
		} else {
			items.add(new Item(sourceFiles, dest));
		}
		// --------- /Build the Item List --------- //

		// --------- Process and Save --------- //
		try {
			for (Item item : items) {
				if (!asNeeded || doesItemNeedRefresh(item)) {
					processItem(item);
				}
			}
		} catch (Exception e) {
			System.out.println("\nERROR: less - cannot process because " + e.getMessage());
			e.printStackTrace();
		}
		// --------- Process and Save --------- //
	}

	private void processItem(Item item) throws IOException {
		StringBuilder contentSB = new StringBuilder();
		System.out.print("less - processing to " + item.dest.getName() + " (");
		int c = 0;
		boolean printFileName = true;
		if (item.sources.size() > 4){
			System.out.print(item.sources.size() + " files");
			printFileName = false;
		}
		for (File file : item.sources) {
			String content = compile(file);
			if (printFileName){
				System.out.print( ((c > 0)?",":"") + file.getName());
			}
			contentSB.append(content).append("\n");
			c++;
		}

		if (item.dest.getParentFile() != null && !item.dest.getParentFile().exists()){
			item.dest.getParentFile().mkdirs();
		}
		Files.write(contentSB.toString(), item.dest, Charsets.UTF_8);
		System.out.println(") DONE");
	}

	private boolean doesItemNeedRefresh(Item item) {
		for (File source : item.sources) {
			if (item.dest.lastModified() < source.lastModified()) {
				return true;
			}
		}
		return false;
	}

	public String compile(File input) {
		try {
			String result = null;
			// long time = System.currentTimeMillis();
			// logger.debug("Compiling File: " + "file:" + input.getAbsolutePath());
			//result = call(compileFile, new Object[] { "file:" + input.getAbsolutePath(), classLoader });

			// logger.debug("The compilation of '" + input + "' took " + (System.currentTimeMillis () - time) + " ms.");
			String location = input.getAbsolutePath();
			String source = Files.toString(input, Charsets.UTF_8);
			boolean compress = false;
			result = call(compile, new Object[] { source, location,
					compress });
			return result;
		} catch (Exception e) {
			try {
				throw Throwables.propagate(parseLessException(e));
			} catch (LessException e1) {
				throw Throwables.propagate(e1);
			}
		}
	}

	private synchronized String call(Function fn, Object[] args) {
		Context.enter();
		String result = (String) Context.call(null, fn, scope, scope, args);
		Context.exit();
		return result;
	}

	private boolean hasProperty(Scriptable value, String name) {
		Object property = ScriptableObject.getProperty(value, name);
		return property != null && !property.equals(UniqueTag.NOT_FOUND);
	}

	private LessException parseLessException(Exception root)
			throws LessException {
		if (root instanceof JavaScriptException) {
			Scriptable value = (Scriptable) ((JavaScriptException) root)
					.getValue();
			String type = ScriptableObject.getProperty(value, "type")
					.toString() + " Error";
			String message = ScriptableObject.getProperty(value, "message")
					.toString();
			String filename = "";
			if (hasProperty(value, "filename")) {
				filename = ScriptableObject.getProperty(value, "filename")
						.toString();
			}
			int line = -1;
			if (hasProperty(value, "line")) {
				line = ((Double) ScriptableObject.getProperty(value, "line"))
						.intValue();
			}
			int column = -1;
			if (hasProperty(value, "column")) {
				column = ((Double) ScriptableObject
						.getProperty(value, "column")).intValue();
			}
			List<String> extractList = new ArrayList<String>();
			if (hasProperty(value, "extract")) {
				NativeArray extract = (NativeArray) ScriptableObject
						.getProperty(value, "extract");
				for (int i = 0; i < extract.getLength(); i++) {
					if (extract.get(i, extract) instanceof String) {
						extractList.add(((String) extract.get(i, extract))
								.replace("\t", " "));
					}
				}
			}
			throw new LessException(message, type, filename, line, column,
					extractList);
		}
		throw new LessException(root);
	}

}

class Item {

	List<File> sources;
	File       dest;   // fileOnly, cannot be folder

	public Item(File source, File dest) {
		this.sources = new ArrayList<File>();
		this.sources.add(source);
		this.dest = dest;
	}

	public Item(List<File> sources, File dest) {
		this.sources = sources;
		this.dest = dest;
	}
}