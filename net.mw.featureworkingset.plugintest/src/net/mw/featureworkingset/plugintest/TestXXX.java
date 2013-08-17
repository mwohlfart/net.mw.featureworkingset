package net.mw.featureworkingset.plugintest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import net.mw.featureworkingset.CreateFeatureWorkingSetHandler;
import net.mw.featureworkingset.plugintest.util.FeatureProjectBuilder;
import net.mw.featureworkingset.plugintest.util.PluginProjectBuilder;
import net.mw.featureworkingset.plugintest.util.TestFixture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkingSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestXXX {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@BeforeClass
	public static void beforeClass() throws CoreException {
		TestFixture.disableAutoBuilding();

		// load bundle
		new CreateFeatureWorkingSetHandler();
	}

	@Before
	public void setUp() throws InterruptedException {
		TestFixture.clearWorkspace();
		TestFixture.clearWorkingSetsFromManager();
	}

	@Ignore
	@Test
	public void testProjectRemovedFromDisk() throws Exception {
		// setup
		URI locationUri = folder.getRoot().toURI();

		IProject project = PluginProjectBuilder.create("TestPlugin", locationUri)//
				.build();
		
		FeatureProjectBuilder.create("TestFeature", locationUri)//
				.addIncludedPlugin("TestPlugin")//
				.build();

		IWorkingSet workingSet = TestFixture.createFeatureWorkingSet(
				"TestFeature", new IAdaptable[] {});

		Assert.assertEquals(1, workingSet.getElements().length);
		Assert.assertTrue(workingSet.getElements()[0] instanceof IProject);

		// test
		IPath path = project.getLocation();
		recursiveDelete(path.toFile());
		try {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			Assert.fail();
		} catch (Exception e) {
			// OK
		}
		

		Assert.assertEquals(1, workingSet.getElements().length);
		Assert.assertTrue(workingSet.getElements()[0] instanceof IProject);
		Assert.assertFalse(((IProject)workingSet.getElements()[0]).isOpen());

	}
	
	private void recursiveDelete(File file) {
		File[] files= file.listFiles();
		if (files != null)
			for (File each : files)
				recursiveDelete(each);
		file.delete();
	}

}
