package net.mw.featureworkingset;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.mw.featureworkingset.FeatureProjectParser.IFeature;
import net.mw.featureworkingset.FeatureProjectParser.IFeature.IContainedFeature;
import net.mw.featureworkingset.FeatureProjectParser.IFeature.IPluginEntry;
import net.mw.featureworkingset.internal.jdt.PdeUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.ui.IWorkingSet;

import sun.net.ProgressMonitor;

public class FeatureWorkingSetUtil {

	public static void updateFeatureWorkingSet(
			IFeatureWorkingSet featureWorkingSet) {

		traceUpdatingWorkingSet(featureWorkingSet);

		IProject[] children = featureWorkingSet.getChildren();
		featureWorkingSet.getWorkingSet().setElements(children);
	}

	private static void traceUpdatingWorkingSet(
			IFeatureWorkingSet featureWorkingSet) {
		String message = "Updating feature working set (name={0})";
		message = MessageFormat.format(message,
				featureWorkingSet.getWorkingSetName());
		FeatureWorkingSetPlugin.getDefault().getDebugTrace()
				.trace("/debug", message);
	}

	private static void traceProjectNotFound(String id) {
		String message = "Project not found (name={0})";
		message = MessageFormat.format(message, id);
		FeatureWorkingSetPlugin.getDefault().getDebugTrace()
				.trace("/debug", message);
	}

	public static IFeatureWorkingSet createFeatureWorkingSet(
			final IWorkingSet workingSet) {

		traceCreatingWorkingSet(workingSet);

		IFeatureWorkingSet result = new IFeatureWorkingSet() {

			@Override
			public IWorkingSet getWorkingSet() {
				return workingSet;
			}

			@Override
			public String getWorkingSetName() {
				return workingSet.getName();
			}

			@Override
			public IProject getFeatureProject() {
				return FeatureWorkingSetUtil.getFeatureProject(workingSet);
			}

			@Override
			public IProject[] getChildren() {
				IProject featureProject = getFeatureProject();
				return FeatureWorkingSetUtil
						.getFeatureWorkingSetContents(featureProject);
			}

			@Override
			public boolean contains(IProject project) {
				return Arrays.asList(getChildren()).contains(project);
			}
		};

		return result;
	}

	private static void traceCreatingWorkingSet(IWorkingSet workingSet) {
		String message = "Updating feature working set (name={0})";
		message = MessageFormat.format(message, workingSet.getName());
		FeatureWorkingSetPlugin.getDefault().getDebugTrace()
				.trace("/debug", message);
	}

	private static void checkFeatureProject(IProject project)
			throws CoreException {
		if (!PdeUtil.isFeatureProject(project)) {
			throw new IllegalArgumentException(
					"project is not a feature project"); //$NON-NLS-1$
		}
	}

	public static IProject[] getFeatureWorkingSetContents(
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
			IFeature featureContent) throws CoreException {
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

	private static IProject findProject(final String id) {
		ModelEntry foundEntry = PluginRegistry.findEntry(id);

		if (foundEntry != null) {
			IPluginModelBase[] workspaceModels = foundEntry
					.getWorkspaceModels();

			for (IPluginModelBase workspaceModel : workspaceModels) {
				return workspaceModel.getUnderlyingResource().getProject();
			}

		}

		IProject featureOrUnavailableProject = ResourcesPlugin.getWorkspace()
				.getRoot().getProject(id);

		if (!featureOrUnavailableProject.isAccessible()) {
			try {
				featureOrUnavailableProject.close(new NullProgressMonitor());
			} catch (CoreException e) {
				traceProjectNotFound(id);
			}
		}


		return featureOrUnavailableProject;
	}

	private static IProject getFeatureProject(final IWorkingSet workingSet) {
		String featureId = workingSet.getName();
		final IProject featureProject = ResourcesPlugin.getWorkspace()
				.getRoot().getProject(featureId);
		return featureProject;
	}

	public static class UnresolvedBundle extends PlatformObject {

		private String id;

		public UnresolvedBundle(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return id;
		}
	}

}
