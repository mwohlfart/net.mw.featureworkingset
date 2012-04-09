package net.mw.featureworkingset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.mw.featureworkingset.FeatureProjectParser.IFeature.IPluginEntry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FeatureProjectParser {

	public interface IFeature {

		public interface IPluginEntry {
			String getId();
		}

		String getFeatureId();

		String getFeatureLabel();

		IPluginEntry[] getPluginEntries();
	}
	
	public static IFeature parseFeature(IProject featureProject)
			throws CoreException {
		
		checkFeatureProject(featureProject);
		
		IFile file = featureProject.getFile(new Path("feature.xml"));
		
		return parseFeature(file);
	}
	
	private static void checkFeatureProject(IProject featureProject) {
		if (!featureProject.exists(new Path("feature.xml"))) {
			throw new IllegalArgumentException("feature.xml not found");
		}
	}


	private static IFeature parseFeature(IFile featureXmlFile)
			throws CoreException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			FeatureXmlHandler featureXmlHandler = new FeatureXmlHandler();
			parser.parse(featureXmlFile.getContents(), featureXmlHandler);
			return featureXmlHandler.getContent();
		} catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					FeatureWorkingSetPlugin.PLUGIN_ID, "Error parsing feature.xml", e));
		} catch (SAXException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					FeatureWorkingSetPlugin.PLUGIN_ID, "Error parsing feature.xml", e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					FeatureWorkingSetPlugin.PLUGIN_ID, "Error parsing feature.xml", e));
		}
	}

	public static class FeatureXmlHandler extends DefaultHandler {

		private String id;
		private String label;
		private List<IPluginEntry> pluginEntries = new ArrayList<IPluginEntry>();

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if ("plugin".equals(qName)) {
				final String pluginId = attributes.getValue("id");
				pluginEntries.add(new IPluginEntry() {

					@Override
					public String getId() {
						return pluginId;
					}
				});
			}

			if ("feature".equals(qName)) {
				id = attributes.getValue("id");
				label = attributes.getValue("label");
			}
		}

		public IFeature getContent() {
			return new IFeature() {


				@Override
				public IPluginEntry[] getPluginEntries() {
					return pluginEntries.toArray(new IPluginEntry[pluginEntries
							.size()]);
				}

				@Override
				public String getFeatureLabel() {
					return label;
				}

				@Override
				public String getFeatureId() {
					return id;
				}
			};
		}

	}

}
