package net.mw.featureworkingset.plugintest.util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

public class ClearWorkspaceJob extends WorkspaceJob {

	public ClearWorkspaceJob() {
		super("ClearWorkspaceJob");
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot()
				.delete(true, new NullProgressMonitor());
		return Status.OK_STATUS;
	}

	public static void run() throws InterruptedException {
		ClearWorkspaceJob clearWorkspaceJob = new ClearWorkspaceJob();
		clearWorkspaceJob.schedule();
		clearWorkspaceJob.join();
	}

}