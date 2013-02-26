package net.mw.featureworkingset;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.mw.featureworkingset.messages"; //$NON-NLS-1$
	public static String FeatureWorkingSetPage_no_project_selected_error;
	public static String FeatureWorkingSetPage_title;
	public static String FeatureWorkingSetPage_workingset_content_label;
	public static String FeatureWorkingSetPage_workingset_page_description;
	public static String FeatureWorkingSetPage_workingset_selection_label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
