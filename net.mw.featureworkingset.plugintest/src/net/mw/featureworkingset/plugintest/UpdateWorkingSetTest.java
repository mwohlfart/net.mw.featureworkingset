/**
 * 
 */
package net.mw.featureworkingset.plugintest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import net.mw.featureworkingset.CreateWorkingSetsHandler;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael
 * 
 */
public class UpdateWorkingSetTest {

	public static class ClearWorkspaceJob extends WorkspaceJob {

		public ClearWorkspaceJob() {
			super("ClearWorkspaceJob");
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			ResourcesPlugin.getWorkspace().getRoot().delete(true, new NullProgressMonitor());
			return Status.OK_STATUS;
		}

		public static void run() throws InterruptedException {
			ClearWorkspaceJob clearWorkspaceJob = new ClearWorkspaceJob();
			clearWorkspaceJob.schedule();
			clearWorkspaceJob.join();
		}

	}

	public abstract static class ProjectBuilder {

		protected String name;

		public ProjectBuilder(String name) {
			this.name = name;
		}

		public IProject create() throws Exception {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);

			if (!project.exists()) {
				project.create(new NullProgressMonitor());
			}

			if (!project.isOpen()) {
				project.open(new NullProgressMonitor());
			}

			return project;
		}

		protected void setPluginNature(IProject project, String... natures) throws CoreException {
			IProjectDescription description = project.getDescription();
			description.setNatureIds(natures);
			project.setDescription(description, new NullProgressMonitor());
		}

	}

	public static class PluginProjectBuilder extends ProjectBuilder {

		public PluginProjectBuilder(String name) {
			super(name);
		}

		public IProject create() throws Exception {
			IProject project = super.create();

			setPluginNature(project, "org.eclipse.pde.PluginNature");

			updateManifest(project);

			return project;
		}

		private void updateManifest(IProject project) throws Exception {
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

	}

	public static class FeatureProjectBuilder extends ProjectBuilder {

		private Set<String> references = new HashSet<String>();
		private String label;

		public FeatureProjectBuilder(String name) {
			super(name);
		}

		public IProject create() throws Exception {
			IProject project = super.create();

			setPluginNature(project, "org.eclipse.pde.FeatureNature");

			updateFeatureXml(project);

			return project;
		}

		private void updateFeatureXml(IProject project) throws Exception {

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				writeFeatureXmlTo(stream);
			} finally {
				stream.close();
			}

			ByteArrayInputStream contents = new ByteArrayInputStream(stream.toByteArray());

			IFile file = PDEProject.getFeatureXml(project);
			if (file.exists()) {
				file.setContents(contents, true, true, new NullProgressMonitor());
			} else {
				file.create(contents, true, new NullProgressMonitor());
			}
		}

		private void writeFeatureXmlTo(ByteArrayOutputStream stream) throws Exception {
			XMLWriter writer = new XMLWriter(stream);

			HashMap<String, Object> featureParameters = new HashMap<String, Object>();
			featureParameters.put("id", name);
			featureParameters.put("label", label);
			writer.startTag("feature", featureParameters);
			for (String reference : references) {
				HashMap<String, Object> pluginParameters = new HashMap<String, Object>();
				pluginParameters.put("id", reference);
				writer.printTag("plugin", pluginParameters);
				writer.endTag("plugin");
			}
			writer.endTag("feature");

			writer.flush();
			writer.close();
			if (writer.checkError())
				throw new IOException();
		}

		public FeatureProjectBuilder label(String label) {
			this.label = label;
			return this;
		}

		public FeatureProjectBuilder addReference(String... pluginIds) {
			for (String pluginId : pluginIds) {
				references.add(pluginId);
			}
			return this;
		}

		public void update() throws Exception {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			updateFeatureXml(project);
		}

		public FeatureProjectBuilder removeReference(String... string) {
			for (String pluginId : string) {
				references.remove(pluginId);
			}
			return this;
		}

	}
	
	@BeforeClass
	public static void beforeClass() {
		// load bundle
		new CreateWorkingSetsHandler();
	}

	@Before
	public void setUp() throws Exception {
		ClearWorkspaceJob.run();
		clearWorkingSets();

		disableAutoBuilding();
	}

	private void clearWorkingSets() {
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = manager.getWorkingSets();
		
		for (IWorkingSet ws : workingSets) {
			manager.removeWorkingSet(ws);
		}
	}

	private void disableAutoBuilding() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			workspace.setDescription(description);
		}

	}

	@Test
	public void testPluginAdd() throws Exception {
		// setup
		IProject testProject = new PluginProjectBuilder("TestProject").create();
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder("TestFeature");
		IProject featureProject = featureProjectBuilder.addReference("TestProject").create();

		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = manager.createWorkingSet("TestWorkingSet", new IAdaptable[] { featureProject, testProject });
		workingSet.setId("net.mw.featureworkingset");
		manager.addWorkingSet(workingSet);

		// test
		new PluginProjectBuilder("TestProject1").create();
		featureProjectBuilder.addReference("TestProject1").update();

		// assert
		Assert.assertEquals(3, workingSet.getElements().length);
	}
	
	@Test
	public void testPluginRemove() throws Exception {
		// setup
		IProject testProject = new PluginProjectBuilder("TestProject").create();
		IProject testProject1 = new PluginProjectBuilder("TestProject1").create();
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder("TestFeature");
		IProject featureProject = featureProjectBuilder.addReference("TestProject").addReference("TestProject1").create();

		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = manager.createWorkingSet("TestWorkingSet", new IAdaptable[] { featureProject, testProject, testProject1 });
		workingSet.setId("net.mw.featureworkingset");
		manager.addWorkingSet(workingSet);

		// test
		featureProjectBuilder.removeReference("TestProject1").update();

		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}
}