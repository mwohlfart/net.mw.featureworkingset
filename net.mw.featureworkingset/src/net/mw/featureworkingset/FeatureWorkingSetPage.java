/**
 * 
 */
package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.mw.featureworkingset.internal.jdt.AbstractWorkingSetWizardPage;
import net.mw.featureworkingset.internal.jdt.JdtUtil;
import net.mw.featureworkingset.internal.jdt.PdeUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
public class FeatureWorkingSetPage extends AbstractWorkingSetWizardPage
		implements IWorkingSetPage {

	public class WorkspaceFeatureProjectProvider implements
			ITreeContentProvider {

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

			List<IProject> result = new ArrayList<IProject>();
			
			if (inputElement instanceof IWorkspace) {

				IWorkspace workspace = (IWorkspace) inputElement;

				for (IProject project : workspace.getRoot().getProjects()) {
					if (project.isOpen()) {
						try {
							if (PdeUtil.isFeatureProject(project)) {
								result.add(project);
							}
						} catch (CoreException e) {
							FeatureWorkingSetPlugin.getDefault().getLog()
									.log(e.getStatus());
						}
					}
				}
			}
			
			return result.toArray();
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

	public FeatureWorkingSetPage() {
		super("featureWorkingSetPage", "Feature Working Set", null);
	}

	@Override
	protected String getPageId() {
		return FeatureWorkingSetPlugin.FEATUREWORKINGSET_ID;
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
//		IAdaptable[] elements = workingSet.getElements();
//
//		for (IAdaptable adaptable : elements) {
//			IProject project = (IProject) adaptable.getAdapter(IProject.class);
//
//			if (project != null
//					&& FeatureWorkingSetUtil.isFeatureProject(project)) {
//				result.add(adaptable);
//			}
//		}

		return result.toArray(new Object[result.size()]);
	}

	@Override
	protected Set<Object> getWorkingSetElements(Object[] selection) {
		Set<Object> result = new HashSet<Object>();

		try {
			for (Object o : selection) {
				if (o instanceof IProject) {
					IProject project = (IProject) o;

					if (PdeUtil.isFeatureProject(project)) {
						result.add(project);

						IProject[] includedPluginProjects = FeatureWorkingSetUtil
								.getIncludedPluginProjects(project);
						result.addAll(Arrays.asList(includedPluginProjects));
					}
				}
			}
		} catch (CoreException e) {
			FeatureWorkingSetPlugin.getDefault().getLog().log(e.getStatus());
			return Collections.emptySet();
		}

		return result;
	}

}
