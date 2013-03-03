package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.mw.featureworkingset.FeatureProjectParser.IFeature;
import net.mw.featureworkingset.FeatureProjectParser.IFeature.IContainedFeature;
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

	public static void checkFeatureProject(IProject project)
			throws CoreException {
		if (!PdeUtil.isFeatureProject(project)) {
			throw new IllegalArgumentException(
					"project is not a feature project"); //$NON-NLS-1$
		}
	}

	public static IProject[] getFeatureWorkingsetContents(
			IProject featureProject) {

		if (!featureProject.exists()) {
			return new IProject[] {};
		}

		if (!featureProject.isOpen()) {
			return new IProject[] {};
		}

		List<IProject> result = new ArrayList<IProject>();

		try {
			checkFeatureProject(featureProject);

			IFeature featureContent = FeatureProjectParser
					.parseFeature(featureProject);

			result.addAll(FeatureWorkingSetUtil
					.getIncludedPluginProjects(featureContent));
			result.addAll(FeatureWorkingSetUtil
					.getIncludedFeatureProjects(featureContent));

			return result.toArray(new IProject[result.size()]);
		} catch (CoreException e) {
			FeatureWorkingSetPlugin.getDefault().getLog().log(e.getStatus());
			return new IProject[] {};
		}
	}

	private static List<IProject> getIncludedFeatureProjects(
			IFeature featureContent) {
		List<IProject> result = new ArrayList<IProject>();

		for (IContainedFeature entry : featureContent.getContainedFeatures()) {
			IProject project = findProject(entry.getId());
			if (project != null) {
				result.add(project);
			}
		}

		return result;
	}

	private static List<IProject> getIncludedPluginProjects(
			IFeature featureContent) throws CoreException {

		List<IProject> result = new ArrayList<IProject>();

		for (IPluginEntry entry : featureContent.getPluginEntries()) {
			IProject project = findProject(entry.getId());
			if (project != null) {
				result.add(project);
			}
		}

		return result;
	}

	private static IProject findProject(String id) {
		ModelEntry foundEntry = PluginRegistry.findEntry(id);

		if (foundEntry != null) {
			IPluginModelBase[] workspaceModels = foundEntry
					.getWorkspaceModels();

			for (IPluginModelBase workspaceModel : workspaceModels) {
				return workspaceModel.getUnderlyingResource().getProject();
			}

		} else {
			IProject unavailableProject = ResourcesPlugin.getWorkspace()
					.getRoot().getProject(id);
			return unavailableProject;
		}
		return null;
	}

	public static IProject getFeatureProject(final IWorkingSet workingSet) {
		String featureId = workingSet.getName();
		final IProject featureProject = ResourcesPlugin.getWorkspace()
				.getRoot().getProject(featureId);
		return featureProject;
	}

}
