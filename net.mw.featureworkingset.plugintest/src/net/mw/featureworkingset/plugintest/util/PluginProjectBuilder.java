package net.mw.featureworkingset.plugintest.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.internal.core.project.PDEProject;

public class PluginProjectBuilder extends ProjectBuilder {

	public static PluginProjectBuilder create(String name) {
		return new PluginProjectBuilder(name);
	}
	
	public static PluginProjectBuilder create(String name, URI locationUri) {
		return new PluginProjectBuilder(name, locationUri);
	}

	private PluginProjectBuilder(String name) {
		super(name);
	}

	public PluginProjectBuilder(String name, URI locationUri) {
		super(name, locationUri);
	}

	public IProject build() throws Exception {
		IProject project = super.build();

		updateManifest(project);

		return project;
	}

	private void updateManifest(IProject project) throws Exception {
		@SuppressWarnings("restriction")
		IFile manifest = PDEProject.getManifest(project);

		ensureExists(manifest.getParent());

		InputStream stream = createManifestStream();

		if (manifest.exists()) {
			manifest.setContents(stream, true, true, new NullProgressMonitor());
		} else {
			manifest.create(stream, true, new NullProgressMonitor());
		}

	}

	private void ensureExists(IContainer container) throws Exception {
		if (!(container instanceof IFolder)) {
			return;
		} else {
			ensureExists(container.getParent());
		}

		IFolder folder = (IFolder) container;

		if (!folder.exists()) {
			folder.create(true, true, new NullProgressMonitor());
		}

	}

	private InputStream createManifestStream() {
		String manifest = new StringWriter(). //
				append("Manifest-Version: 1.0\n"). //
				append("Bundle-ManifestVersion: 2\n"). //
				append("Bundle-SymbolicName:" + name + ";singleton:=true"). //
				toString();

		return new ByteArrayInputStream(manifest.getBytes());
	}

	@Override
	protected void fillProjectDescription(IProjectDescription projectDescription) {
		projectDescription.setNatureIds(new String[] { "org.eclipse.pde.PluginNature" });
	}

}