/**
 * 
 */
package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.mw.featureworkingset.internal.jdt.AbstractWorkingSetWizardPage;
import net.mw.featureworkingset.internal.jdt.JdtUtil;
import net.mw.featureworkingset.internal.jdt.PdeUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * @author Michael
 * 
 */
public class FeatureWorkingSetPage extends AbstractWorkingSetWizardPage implements IWorkingSetPage {

	public class FeatureProjectVisitor implements IResourceVisitor {

		private List<IProject> featureProjects = new ArrayList<IProject>();

		public IProject[] getFeatureProjects() {
			return featureProjects.toArray(new IProject[featureProjects.size()]);
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {

			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				if (PdeUtil.isFeatureProject(project)) {
					featureProjects.add(project);
				}
				return false;
			}

			return true;
		}

	}

	public class WorkspaceFeatureProjectProvider implements ITreeContentProvider {

		@Override
		public void dispose() {
			// nothing to do here
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do here
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IWorkspace) {
				IWorkspace workspace = (IWorkspace) inputElement;

				FeatureProjectVisitor visitor = new FeatureProjectVisitor();

				try {
					workspace.getRoot().accept(visitor, IResource.DEPTH_ONE, false);
					return visitor.getFeatureProjects();
				} catch (CoreException e) {
					// do nothing here
				}

			}
			return null;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			// no children
			return null;
		}

		@Override
		public Object getParent(Object element) {
			// no parents
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}

	private static final String ID = "net.mw.featureworkingset";

	public FeatureWorkingSetPage() {
		super("featureWorkingSetPage", "Feature Working Set", null);
	}

	@Override
	protected String getPageId() {
		return ID;
	}

	@Override
	protected void configureTree(TreeViewer tree) {

		tree.setLabelProvider(JdtUtil.getTreeLabelProvider());
		tree.setComparator(new JavaElementComparator());

		tree.setContentProvider(new WorkspaceFeatureProjectProvider());
		tree.setInput(ResourcesPlugin.getWorkspace());

	}

	@Override
	protected void configureTable(TableViewer table) {
		table.setLabelProvider(JdtUtil.getTableLabelProvider());
	}

	@Override
	protected Object[] getInitialWorkingSetElements(IWorkingSet workingSet) {

		List<Object> result = new ArrayList<Object>();
		IAdaptable[] elements = workingSet.getElements();

		for (IAdaptable adaptable : elements) {
			IProject project = (IProject) adaptable.getAdapter(IProject.class);

			if (project != null && FeatureWorkingSetUtil.isFeatureProject(project)) {
				result.add(adaptable);
			}
		}

		return result.toArray(new Object[result.size()]);
	}

	@Override
	protected Set<Object> getWorkingSetElements(Object[] selection) {
		Set<Object> result = new HashSet<Object>();

		for (Object o : selection) {
			if (o instanceof IProject) {
				IProject project = (IProject) o;
				result.add(project);
				addFeaturePluginsTo(result, project);
			}
		}

		return result;
	}

	private void addFeaturePluginsTo(Set<Object> result, IProject featureProject) {

		try {
			IProject[] referencedPluginProjects = FeatureWorkingSetUtil.getReferencedPluginProjects(featureProject);
			result.addAll(Arrays.asList(referencedPluginProjects));
		} catch (CoreException e) {
			FeatureWorkingSetPlugin.getDefault().getLog().log(e.getStatus());
		}
	}


}
