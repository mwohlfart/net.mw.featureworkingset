package net.mw.featureworkingset;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FeatureWorkingSetPlugin extends AbstractUIPlugin {

	
	public class MyDebugOptionsListener implements DebugOptionsListener {

		@Override
		public void optionsChanged(DebugOptions options) {
			debugTrace = options.newDebugTrace(PLUGIN_ID);
			DEBUG = options.getBooleanOption(PLUGIN_ID + "/debug", false);
		}

	}

	// The plug-in ID
	public static final String PLUGIN_ID = "net.mw.featureworkingset"; //$NON-NLS-1$
	
	public static final String FEATUREWORKINGSET_ID = "net.mw.featureworkingset";

	public static boolean DEBUG;
	
	// The shared instance
	private static FeatureWorkingSetPlugin plugin;
	
	private DebugTrace debugTrace;
	
	public DebugTrace getDebugTrace() {
		return debugTrace;
	}

	/**
	 * The constructor
	 */
	public FeatureWorkingSetPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		registerDebugOptionsListener(context);
		
		debugTrace.trace("/debug", "TestTrace");
		
		plugin = this;
	}

	private void registerDebugOptionsListener(BundleContext context) {
		final Hashtable<String, String> properties = new Hashtable<String, String>(4);
		properties.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), new MyDebugOptionsListener(), properties );
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static FeatureWorkingSetPlugin getDefault() {
		return plugin;
	}

}
