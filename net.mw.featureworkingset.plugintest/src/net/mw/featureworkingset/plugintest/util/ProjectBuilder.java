package net.mw.featureworkingset.plugintest.util;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;

public abstract class ProjectBuilder {

	protected String name;
	private URI locationUri;

	public ProjectBuilder(String name) {
		this.name = name;
	}
	
	public ProjectBuilder(String name, URI locationUri) {
		this.name = name;
		this.locationUri = URI.create(locationUri.toString() + name);
	}

	public IProject build() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(name);
		
		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(name);
		
		if (locationUri != null) {
			projectDescription.setLocationURI(locationUri);
		}
		
		fillProjectDescription(projectDescription);

		if (!project.exists()) {
			project.create(projectDescription, new NullProgressMonitor());
		}

		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}

		return project;
	}
	
	protected abstract void fillProjectDescription(IProjectDescription projectDescription);

}