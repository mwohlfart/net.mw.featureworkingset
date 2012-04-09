/**
 * 
 */
package net.mw.featureworkingset;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

/**
 * @author Michael
 *
 */
public class FeatureProjectTester extends PropertyTester {

	/**
	 * 
	 */
	public FeatureProjectTester() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof IProject) {
			IProject project = (IProject) receiver;
			IFile featureFile = project.getFile(new Path("feature.xml"));
			return featureFile.exists();
		}
		return false;
	}

}
