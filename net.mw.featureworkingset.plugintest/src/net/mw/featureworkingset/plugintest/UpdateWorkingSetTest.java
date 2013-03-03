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
import net.mw.featureworkingset.CreateFeatureWorkingSetHandler;

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

	private abstract static class ProjectBuilder {

		protected String name;

		public ProjectBuilder(String name) {
			this.name = name;
		}

		public IProject create() throws Exception {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(name);

			if (!project.exists()) {
				project.create(new NullProgressMonitor());
			}

			if (!project.isOpen()) {
				project.open(new NullProgressMonitor());
			}

			return project;
		}

		protected void setPluginNature(IProject project, String... natures)
				throws CoreException {
			IProjectDescription description = project.getDescription();
			description.setNatureIds(natures);
			project.setDescription(description, new NullProgressMonitor());
		}

	}

	private static class PluginProjectBuilder extends ProjectBuilder {

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
				manifest.setContents(stream, true, true,
						new NullProgressMonitor());
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

	private static class FeatureProjectBuilder extends ProjectBuilder {

		private Set<String> plugins = new HashSet<String>();
		private Set<String> features = new HashSet<String>();
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

		@SuppressWarnings("restriction")
		private void updateFeatureXml(IProject project) throws Exception {

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				writeFeatureXmlTo(stream);
			} finally {
				stream.close();
			}

			ByteArrayInputStream contents = new ByteArrayInputStream(
					stream.toByteArray());

			IFile file = PDEProject.getFeatureXml(project);
			if (file.exists()) {
				file.setContents(contents, true, true,
						new NullProgressMonitor());
			} else {
				file.create(contents, true, new NullProgressMonitor());
			}
		}

		@SuppressWarnings("restriction")
		private void writeFeatureXmlTo(ByteArrayOutputStream stream)
				throws Exception {
			XMLWriter writer = new XMLWriter(stream);

			HashMap<String, Object> featureParameters = new HashMap<String, Object>();
			featureParameters.put("id", name);
			featureParameters.put("label", label);
			writer.startTag("feature", featureParameters);
			for (String reference : plugins) {
				HashMap<String, Object> pluginParameters = new HashMap<String, Object>();
				pluginParameters.put("id", reference);
				writer.printTag("plugin", pluginParameters);
				writer.endTag("plugin");
			}
			for (String reference : features) {
				HashMap<String, Object> includedFeatureParameters = new HashMap<String, Object>();
				includedFeatureParameters.put("id", reference);
				writer.printTag("includes", includedFeatureParameters);
				writer.endTag("includes");
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

		public FeatureProjectBuilder addIncludedPlugin(String... pluginIds) {
			for (String pluginId : pluginIds) {
				plugins.add(pluginId);
			}
			return this;
		}

		public void update() throws Exception {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(name);
			updateFeatureXml(project);
		}

		public FeatureProjectBuilder removeIncludedPlugin(String... string) {
			for (String pluginId : string) {
				plugins.remove(pluginId);
			}
			return this;
		}

		public FeatureProjectBuilder addIncludedFeature(String... featureIds) {
			for (String featureId : featureIds) {
				features.add(featureId);
			}
			return this;
		}

	}

	private static class TestFixture {
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

		public static IProject createPluginProject(String name)
				throws Exception {
			return new PluginProjectBuilder(name).create();
		}
	}

	@BeforeClass
	public static void beforeClass() throws CoreException {
		TestFixture.disableAutoBuilding();

		// load bundle
		new CreateFeatureWorkingSetHandler();
	}

	@Before
	public void setUp() throws Exception {
		TestFixture.clearWorkspace();
		TestFixture.clearWorkingSetsFromManager();
	}

	@Test
	public void testInitialContents() throws Exception {
		// setup
		IProject includedPluginProject = TestFixture.createPluginProject("TestProject");
		IProject includedFeatureProject = new FeatureProjectBuilder("TestFeature").create();
	
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"ParentFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject").addIncludedFeature("TestFeature").create();
	
		// test
		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"ParentFeature", new IAdaptable[] {});
	
		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
		Assert.assertEquals(includedPluginProject, workingSet.getElements()[0]);
		Assert.assertEquals(includedFeatureProject, workingSet.getElements()[1]);
	}

	@Test
	public void testContentNotAvailableInWorkspace() throws Exception {
		// setup
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		featureProjectBuilder.addIncludedPlugin("UnavailablePlugin").addIncludedFeature("UnavailableFeature").create();
	
		// test
		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});
	
		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}

	@Test
	public void testAddIncludedPluginToFeature() throws Exception {
		// setup
		TestFixture.createPluginProject("TestProject");

		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject").create();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		TestFixture.createPluginProject("TestProject1");
		featureProjectBuilder.addIncludedPlugin("TestProject1").update();

		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}

	@Test
	public void testRemoveIncludedPluginFromFeature() throws Exception {
		// setup
		TestFixture.createPluginProject("TestProject");
		TestFixture.createPluginProject("TestProject1");
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject")
				.addIncludedPlugin("TestProject1").create();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		featureProjectBuilder.removeIncludedPlugin("TestProject1").update();

		// assert
		Assert.assertEquals(1, workingSet.getElements().length);
	}

	@Test
	public void testOnlyIncludedPluginsInWorkingSet() throws Exception {
		// setup
		TestFixture.createPluginProject("TestProject");
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject").create();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		IProject testProject = TestFixture.createPluginProject("TestProject1");
		workingSet.setElements(new IAdaptable[] { testProject });

		// assert
		Assert.assertEquals(1, workingSet.getElements().length);
	}

	@Test
	public void testWorkingSetNameChange() throws Exception {
		// setup
		TestFixture.createPluginProject("TestProject");
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject").create();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"NonExistingTestFeature", new IAdaptable[] {});

		// test
		workingSet.setName("TestFeature");

		// assert
		Assert.assertEquals(1, workingSet.getElements().length);
	}

	@Test
	public void testWorkingSetFeatureProjectClose() throws Exception {
		// setup
		TestFixture.createPluginProject("TestProject");
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		IProject featureProject = featureProjectBuilder.addIncludedPlugin(
				"TestProject").create();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		featureProject.close(new NullProgressMonitor());

		// assert
		Assert.assertEquals(0, workingSet.getElements().length);

	}

	@Test
	public void testWorkingSetFeatureProjectOpen() throws Exception {
		// setup
		TestFixture.createPluginProject("TestProject");
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		IProject featureProject = featureProjectBuilder.addIncludedPlugin(
				"TestProject").create();
		featureProject.close(new NullProgressMonitor());

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		featureProject.open(new NullProgressMonitor());

		// assert
		Assert.assertEquals(1, workingSet.getElements().length);

	}

	@Test
	public void testClosePluginInFeatureWorkingSet() throws Exception {
		// setup
		IProject pluginProject = TestFixture.createPluginProject("TestProject");
		TestFixture.createPluginProject("TestProject1");
		FeatureProjectBuilder featureProjectBuilder = new FeatureProjectBuilder(
				"TestFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject")
				.addIncludedPlugin("TestProject1").create();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		pluginProject.close(new NullProgressMonitor());

		Thread.sleep(1000);

		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}
}
