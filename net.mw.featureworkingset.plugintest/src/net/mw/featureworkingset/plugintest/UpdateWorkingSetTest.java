/**
 * 
 */
package net.mw.featureworkingset.plugintest;

import junit.framework.Assert;
import net.mw.featureworkingset.CreateFeatureWorkingSetHandler;
import net.mw.featureworkingset.plugintest.util.FeatureProjectBuilder;
import net.mw.featureworkingset.plugintest.util.PluginProjectBuilder;
import net.mw.featureworkingset.plugintest.util.ProjectBuilder;
import net.mw.featureworkingset.plugintest.util.TestFixture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkingSet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael
 * 
 */
public class UpdateWorkingSetTest {

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
		IProject includedPluginProject = PluginProjectBuilder//
				.create("TestProject")//
				.build();

		IProject includedFeatureProject = FeatureProjectBuilder//
				.create("TestFeature")//
				.build();

		FeatureProjectBuilder.create("ParentFeature")//
				.addIncludedPlugin("TestProject")//
				.addIncludedFeature("TestFeature")//
				.build();

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
		FeatureProjectBuilder.create("TestFeature")//
				.addIncludedPlugin("UnavailablePlugin") //
				.addIncludedFeature("UnavailableFeature")//
				.build();

		// test
		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}

	@Test
	public void testAddIncludedPluginToFeature() throws Exception {
		// setup
		PluginProjectBuilder.create("TestProject").build();

		FeatureProjectBuilder featureProjectBuilder = FeatureProjectBuilder
				.create("TestFeature");
		featureProjectBuilder.addIncludedPlugin("TestProject").build();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		PluginProjectBuilder.create("TestProject1").build();
		featureProjectBuilder.addIncludedPlugin("TestProject1").update();

		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}

	@Test
	public void testRemoveIncludedPluginFromFeature() throws Exception {
		// setup
		PluginProjectBuilder.create("TestProject").build();
		PluginProjectBuilder.create("TestProject1").build();

		FeatureProjectBuilder featureProjectBuilder = FeatureProjectBuilder
				.create("TestFeature");
		featureProjectBuilder//
				.addIncludedPlugin("TestProject")//
				.addIncludedPlugin("TestProject1")//
				.build();

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
		PluginProjectBuilder.create("TestProject").build();
		FeatureProjectBuilder.create("TestFeature")//
				.addIncludedPlugin("TestProject")//
				.build();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		IProject testProject = PluginProjectBuilder.create("TestProject1").build();
		workingSet.setElements(new IAdaptable[] { testProject });

		// assert
		Assert.assertEquals(1, workingSet.getElements().length);
	}

	@Test
	public void testWorkingSetNameChange() throws Exception {
		// setup
		PluginProjectBuilder.create("TestProject").build();
		FeatureProjectBuilder.create("TestFeature")//
				.addIncludedPlugin("TestProject")//
				.build();

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
		PluginProjectBuilder.create("TestProject").build();
		IProject featureProject = FeatureProjectBuilder.create("TestFeature")//
				.addIncludedPlugin("TestProject")//
				.build();

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
		PluginProjectBuilder.create("TestProject").build();
		IProject featureProject = FeatureProjectBuilder.create("TestFeature")//
				.addIncludedPlugin("TestProject")//
				.build();

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
		IProject projectToClose = PluginProjectBuilder.create("TestProject").build();
		PluginProjectBuilder.create("TestProject1").build();
		FeatureProjectBuilder.create("TestFeature")//
				.addIncludedPlugin("TestProject")//
				.addIncludedPlugin("TestProject1")//
				.build();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		// test
		projectToClose.close(new NullProgressMonitor());

		Thread.sleep(1000);

		// assert
		Assert.assertEquals(2, workingSet.getElements().length);
	}
}
