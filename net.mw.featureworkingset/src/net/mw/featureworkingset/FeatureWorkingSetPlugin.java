package net.mw.featureworkingset;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FeatureWorkingSetPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.mw.workingset.utils"; //$NON-NLS-1$
	
	public static final String FEATUREWORKINGSET_ID = "net.mw.featureworkingset";

	// The shared instance
	private static FeatureWorkingSetPlugin plugin;
	
	/**
	 * The constructor
	 */
	public FeatureWorkingSetPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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
