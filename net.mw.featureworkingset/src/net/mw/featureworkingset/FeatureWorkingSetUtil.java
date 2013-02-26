package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.List;

import net.mw.featureworkingset.FeatureProjectParser.IFeature;
import net.mw.featureworkingset.FeatureProjectParser.IFeature.IPluginEntry;
import net.mw.featureworkingset.internal.jdt.PdeUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.ui.IWorkingSet;

public class FeatureWorkingSetUtil {
	
	public static void checkFeatureProject(IProject project) throws CoreException {
		if (!PdeUtil.isFeatureProject(project)) {
			throw new IllegalArgumentException("project is not a feature project"); //$NON-NLS-1$
		}
	}
	
	public static IProject[] getFeatureWorkingsetContents(IProject featureProject) {
		if (!featureProject.exists()) {
			return new IProject[] {};
		}
		
		if (!featureProject.isOpen()) {
			return new IProject[] {};
		}
		
		try {
			return FeatureWorkingSetUtil.getIncludedPluginProjects(featureProject);
		} catch (CoreException e) {
			FeatureWorkingSetPlugin.getDefault().getLog().log(e.getStatus());
			return new IProject[] {};
		}
	}
	
	private static IProject[] getIncludedPluginProjects(IProject project) throws CoreException {
		checkFeatureProject(project);
		
		List<IProject> result = new ArrayList<IProject>();
		
		IFeature featureContent = FeatureProjectParser.parseFeature(project);
		
		for (IPluginEntry entry : featureContent.getPluginEntries()) {
			ModelEntry foundEntry = PluginRegistry.findEntry(entry.getId());
			
			if (foundEntry != null) {
				IPluginModelBase[] workspaceModels = foundEntry.getWorkspaceModels();
				
				for (IPluginModelBase workspaceModel : workspaceModels) {
					result.add(workspaceModel.getUnderlyingResource().getProject());
				}
				
			} else {
				IProject unavailableProject = ResourcesPlugin.getWorkspace().getRoot().getProject(entry.getId());
				if (unavailableProject.exists()) {
					result.add(unavailableProject);
				}
			}
			
		}
		return result.toArray(new IProject[result.size()]);
	}

	public static IProject getFeatureProject(final IWorkingSet workingSet) {
		String featureId = workingSet.getName();
		final IProject featureProject = ResourcesPlugin.getWorkspace().getRoot().getProject(featureId);
		return featureProject;
	}

	
}
