package net.mw.featureworkingset.plugintest.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.mw.featureworkingset.plugintest.UpdateWorkingSetTest;

import org.eclipse.core.internal.resources.XMLWriter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.internal.core.project.PDEProject;

public class FeatureProjectBuilder extends ProjectBuilder {

	public static FeatureProjectBuilder create(String name) {
		return new FeatureProjectBuilder(name);
	}

	public static FeatureProjectBuilder create(String name, URI locationUri) {
		return new FeatureProjectBuilder(name, locationUri);
	}

	private Set<String> plugins = new HashSet<String>();
	private Set<String> features = new HashSet<String>();
	private String label;

	private FeatureProjectBuilder(String name) {
		super(name);
	}

	public FeatureProjectBuilder(String name, URI locationUri) {
		super(name, locationUri);
	}

	@Override
	public IProject build() throws Exception {
		IProject project = super.build();

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
			file.setContents(contents, true, true, new NullProgressMonitor());
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

	@Override
	protected void fillProjectDescription(IProjectDescription projectDescription) {
		projectDescription.setNatureIds(new String[] { "org.eclipse.pde.FeatureNature" });
	}

}