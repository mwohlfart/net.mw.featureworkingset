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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
			if (element instanceof IProject) {
				IProject project = (IProject) element;
				
				if (project.getName().equals(fWorkingSet.getName())) {
					return true;
				} else {
					IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
					return workingSetManager.getWorkingSet(project.getName()) == null;
				}
			}
			return false;
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

	private TableViewer fTable;

	public FeatureWorkingSetPage() {
		super("featureWorkingSetPage", Messages.FeatureWorkingSetPage_title, null); //$NON-NLS-1$
		
		setDescription(Messages.FeatureWorkingSetPage_workingset_page_description);
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
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		
		Composite leftComposite= new Composite(composite, SWT.NONE);
		GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint= convertWidthInCharsToPixels(40);
		leftComposite.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight= 0;
		gridLayout.marginWidth= 0;
		leftComposite.setLayout(gridLayout);
		
		Composite rightComposite= new Composite(composite, SWT.NONE);
		gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint= convertWidthInCharsToPixels(40);
		rightComposite.setLayoutData(gridData);
		gridLayout= new GridLayout(1, false);
		gridLayout.marginHeight= 0;
		gridLayout.marginWidth= 0;
		rightComposite.setLayout(gridLayout);

		Label label = new Label(leftComposite, SWT.WRAP);
		label.setText(Messages.FeatureWorkingSetPage_workingset_selection_label);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		
		label= new Label(rightComposite, SWT.WRAP);
		label.setText(Messages.FeatureWorkingSetPage_workingset_content_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		createTree(leftComposite);
		createTable(rightComposite);

		initializeSelectedElements();
		validateInput();

		if (selectedFeatureProject != null) {
			fTree.setSelection(new StructuredSelection(selectedFeatureProject));
		}
		
		fTree.refresh(true);
		fTree.getTree().setFocus();

		Dialog.applyDialogFont(composite);

	}

	private void createTable(Composite parent) {
		fTable= new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		fTable.getTable().setEnabled(false);

		GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
		fTable.getControl().setLayoutData(gd);

		fTable.setUseHashlookup(true);
		
		configureTable(fTable);
		
		fTable.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return FeatureWorkingSetUtil.getFeatureWorkingSetContents(selectedFeatureProject);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}

		});
		
	}

	private static void configureTable(TableViewer table) {
		table.setLabelProvider(JdtUtil.getTreeLabelProvider());
		table.setComparator(new JavaElementComparator());
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
				
				fTable.setInput(selectedFeatureProject);
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
			infoMessage = Messages.FeatureWorkingSetPage_no_project_selected_error;

		setMessage(infoMessage, INFORMATION);
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	private boolean hasSelectedElement() {
		return selectedFeatureProject != null;
	}

}
