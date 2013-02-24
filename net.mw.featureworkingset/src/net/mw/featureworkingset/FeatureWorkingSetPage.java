/**
 * 
 */
package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.List;

import net.mw.featureworkingset.internal.jdt.JdtUtil;
import net.mw.featureworkingset.internal.jdt.PdeUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * @author Michael
 * 
 */
public class FeatureWorkingSetPage extends WizardPage implements
		IWorkingSetPage {
	
	private final class WorkingSetExistsFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			// TODO: implement
			return true;
		}

	}

	private static class WorkspaceFeatureProjectProvider implements
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

	private TreeViewer fTree;

	private IProject selectedFeatureProject;
	private IWorkingSet fWorkingSet;

	public FeatureWorkingSetPage() {
		super("featureWorkingSetPage", "Feature Working Set", null);
	}

	private void configureTree(TreeViewer tree) {

		tree.setLabelProvider(JdtUtil.getTreeLabelProvider());
		tree.setComparator(new JavaElementComparator());

		tree.setContentProvider(new WorkspaceFeatureProjectProvider());
		tree.setInput(ResourcesPlugin.getWorkspace());

	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label = new Label(composite, SWT.WRAP);
		label.setText("Select Feature Project from Workspace:");
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		createTree(composite);

		initializeSelectedElements();
		validateInput();

		if (selectedFeatureProject != null) {
			fTree.setSelection(new StructuredSelection(selectedFeatureProject));
		}
		
		fTree.refresh(true);
		fTree.getTree().setFocus();

		Dialog.applyDialogFont(composite);

	}

	private void initializeSelectedElements() {
		if (fWorkingSet != null) {
			String workingSetName = fWorkingSet.getName();
			selectedFeatureProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(workingSetName);
		}
	}

	private void createTree(Composite parent) {

		fTree = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE);
		fTree.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		fTree.setFilters(new ViewerFilter[] { new WorkingSetExistsFilter() } );
		fTree.setUseHashlookup(true);

		configureTree(fTree);

		fTree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = fTree.getSelection();
				IStructuredSelection ssel = (IStructuredSelection) selection;
				selectedFeatureProject = (IProject) ssel.getFirstElement();
			}
		});
	}

	@Override
	public void finish() {
		if (fWorkingSet == null) {
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
					.getWorkingSetManager();

			String workingSetName = selectedFeatureProject.getName();

			fWorkingSet = workingSetManager.createWorkingSet(workingSetName,
					new IAdaptable[] {});
			fWorkingSet.setId(FeatureWorkingSetPlugin.FEATUREWORKINGSET_ID);
		} else {
			fWorkingSet.setName(selectedFeatureProject.getName());
		}

	}

	@Override
	public IWorkingSet getSelection() {
		return fWorkingSet;
	}

	@Override
	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null"); //$NON-NLS-1$
		fWorkingSet = workingSet;
		
		initializeSelectedElements();
		
		if (getContainer() != null && getShell() != null) {
			validateInput();
		}
	}

	private void validateInput() {
		String errorMessage = null;
		String infoMessage = null;

		if (!hasSelectedElement())
			infoMessage = "Feature Project must be selected";

		setMessage(infoMessage, INFORMATION);
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	private boolean hasSelectedElement() {
		return selectedFeatureProject != null;
	}

}
