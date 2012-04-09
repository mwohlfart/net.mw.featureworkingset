package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.List;

import net.mw.featureworkingset.FeatureProjectParser.IFeature;
import net.mw.featureworkingset.FeatureProjectParser.IFeature.IPluginEntry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.ui.IWorkingSet;

public class FeatureWorkingSetUtil {
	
	public static void checkFeatureProject(IProject project) {
		if (!isFeatureProject(project)) {
			throw new IllegalArgumentException("feature.xml not found");
		}
	}
	
	public static boolean isFeatureProject(IProject project) {
		return project.exists(new Path("feature.xml"));
	}
	
	public static IProject[] getReferencedPluginProjects(IProject project) throws CoreException {
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
				
			}
			
		}
		return result.toArray(new IProject[result.size()]);
	}

	public static List<IProject> getFeatureProjects(IWorkingSet workingSet) {
		List<IProject> result = new ArrayList<IProject>();
		
		for (IAdaptable adaptable : workingSet.getElements()) {
			IProject project = (IProject) adaptable.getAdapter(IProject.class);
			
			if (project != null && isFeatureProject(project)) {
				result.add(project);
			}
		}
		
		return result;
	}
}
