package net.mw.featureworkingset.plugintest.util;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class TestFixture {
	public static void clearWorkingSetsFromManager() {
		IWorkingSetManager manager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		IWorkingSet[] workingSets = manager.getWorkingSets();

		for (IWorkingSet ws : workingSets) {
			manager.removeWorkingSet(ws);
		}
	}

	public static void disableAutoBuilding() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			workspace.setDescription(description);
		}
	}

	public static void clearWorkspace() throws InterruptedException {
		ClearWorkspaceJob.run();
	}

	public static IWorkingSet createFeatureWorkingSet(String name,
			IAdaptable[] elements) {
		IWorkingSetManager manager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		IWorkingSet workingSet = manager.createWorkingSet(name, elements);
		workingSet.setId("net.mw.featureworkingset");
		manager.addWorkingSet(workingSet);

		workingSet.setElements(new IAdaptable[0]);

		return workingSet;
	}

}