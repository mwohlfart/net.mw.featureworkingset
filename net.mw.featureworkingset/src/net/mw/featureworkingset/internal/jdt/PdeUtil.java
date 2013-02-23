/**
 * 
 */
package net.mw.featureworkingset.internal.jdt;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.natures.PDE;

/**
 * @author Michael
 *
 */
public class PdeUtil {

	@SuppressWarnings("restriction")
	public static boolean isFeatureProject(IProject project) throws CoreException {
		return project.hasNature(PDE.FEATURE_NATURE);
	}
}
